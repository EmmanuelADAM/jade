package monitoring.agents;

import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.SniffOff;
import jade.domain.JADEAgentManagement.SniffOn;
import jade.domain.introspection.*;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLCodec;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.StringACLCodec;
import jade.proto.SimpleAchieveREInitiator;
import monitoring.MessageBus;
import monitoring.gui.ModernSnifferGui;
import monitoring.io.MessageLogFile;
import monitoring.ui.ModernTheme;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Modern, drop-in replacement for {@code jade.tools.sniffer.Sniffer}: opens
 * a monitoring window showing every exchanged message as a live, filterable
 * table and a sequence diagram, fed by {@link MessageBus}.
 * <p>
 * Two ways for a message to reach it:
 * <ol>
 * <li><b>Zero instrumentation</b> - type an agent's local name in the
 * "Watch an agent" field. This sends the real FIPA/JADE {@code SniffOn}
 * request to the AMS (the exact mechanism {@code jade.tools.sniffer.Sniffer}
 * uses), so the platform itself starts forwarding a copy of every message
 * that agent sends/receives - no change needed to that agent's code, and it
 * works even for an agent on a remote container.</li>
 * <li><b>In-process report</b> - an agent calls
 * {@code monitoring.Monitor.send(...)}/{@code Monitor.received(...)} itself;
 * see {@code monitoring/README.md}.</li>
 * </ol>
 * Launch it like the original tool, e.g. from a Jade profile:
 * {@code "mySniffer:monitoring.agents.ModernSnifferAgent"}.
 *
 * @author emmanuel adam
 * @version 1
 */
public class ModernSnifferAgent extends GuiAgent {

    private ModernSnifferGui gui;
    private MessageBus.Listener listener;
    private final Set<AID> watchedAgents = new LinkedHashSet<>();
    private File currentLogDir;

    @Override
    protected void setup() {
        getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL0);
        getContentManager().registerOntology(JADEManagementOntology.getInstance());
        getContentManager().registerOntology(IntrospectionOntology.getInstance());

        gui = new ModernSnifferGui(this);
        for (MessageBus.Trace trace : MessageBus.history()) {
            gui.addTrace(trace);
        }
        listener = gui::addTrace;
        MessageBus.subscribe(listener);

        addBehaviour(new PlatformSniffListener());
    }

    @Override
    protected void onGuiEvent(GuiEvent ev) {
        switch (ev.getType()) {
            case ModernSnifferGui.CLEAR -> {
                MessageBus.clearHistory();
                gui.clearAll();
            }
            case ModernSnifferGui.TOGGLE_THEME -> ModernTheme.toggleDark();
            case ModernSnifferGui.WATCH -> watch((String) ev.getParameter(0));
            case ModernSnifferGui.UNWATCH -> unwatch((String) ev.getParameter(0));
            case ModernSnifferGui.SAVE_LOG -> saveLog();
            case ModernSnifferGui.QUIT -> doDelete();
            default -> { /* nothing else to handle */ }
        }
    }

    private void watch(String localName) {
        if (localName == null || localName.isBlank()) {
            return;
        }
        AID target = new AID(localName, AID.ISLOCALNAME);
        if (!watchedAgents.add(target)) {
            return;
        }
        gui.addWatchedAgent(localName);
        requestSniff(target, true);
    }

    private void unwatch(String localName) {
        AID target = new AID(localName, AID.ISLOCALNAME);
        if (watchedAgents.remove(target)) {
            requestSniff(target, false);
        }
    }

    private void requestSniff(AID target, boolean on) {
        try {
            ACLMessage request = buildSniffRequest(target, on);
            addBehaviour(new SimpleAchieveREInitiator(this, request) {
                @Override
                protected void handleRefuse(ACLMessage reply) {
                    gui.showError("The platform refused to " + (on ? "watch" : "stop watching")
                            + " " + target.getLocalName() + ": " + reply.getContent());
                }

                @Override
                protected void handleFailure(ACLMessage reply) {
                    gui.showError("Could not " + (on ? "watch" : "stop watching")
                            + " " + target.getLocalName() + ": " + reply.getContent());
                }

                @Override
                protected void handleNotUnderstood(ACLMessage reply) {
                    gui.showError("The platform did not understand the request to "
                            + (on ? "watch" : "stop watching") + " " + target.getLocalName());
                }

                @Override
                protected void handleOutOfSequence(ACLMessage reply) {
                    gui.showError("Unexpected reply while trying to " + (on ? "watch" : "stop watching")
                            + " " + target.getLocalName() + ": " + reply);
                }

                @Override
                protected void handleInform(ACLMessage reply) {
                    println((on ? "Now watching " : "Stopped watching ") + target.getLocalName());
                }
            });
        } catch (Exception e) {
            gui.showError("Could not build the sniff request for " + target.getLocalName() + ": " + e.getMessage());
        }
    }

    private ACLMessage buildSniffRequest(AID target, boolean on) throws Exception {
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.setSender(getAID());
        request.addReceiver(getAMS());
        request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        request.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
        request.setOntology(JADEManagementOntology.NAME);

        Action actionWrapper = new Action();
        actionWrapper.setActor(getAMS());
        if (on) {
            SniffOn sniffOn = new SniffOn();
            sniffOn.setSniffer(getAID());
            sniffOn.addSniffedAgents(target);
            actionWrapper.setAction(sniffOn);
        } else {
            SniffOff sniffOff = new SniffOff();
            sniffOff.setSniffer(getAID());
            sniffOff.addSniffedAgents(target);
            actionWrapper.setAction(sniffOff);
        }
        getContentManager().fillContent(request, actionWrapper);
        return request;
    }

    private void saveLog() {
        JFileChooser chooser = new JFileChooser();
        if (currentLogDir != null) {
            chooser.setCurrentDirectory(currentLogDir);
        }
        if (chooser.showSaveDialog(gui) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        currentLogDir = chooser.getCurrentDirectory();
        List<MessageLogFile.Entry> entries = new ArrayList<>();
        for (MessageBus.Trace trace : gui.traces()) {
            entries.add(new MessageLogFile.Entry(trace.direction().name(), trace.owner(), trace.time(), trace.message()));
        }
        try {
            MessageLogFile.writeLog(entries, chooser.getSelectedFile());
        } catch (IOException e) {
            gui.showError("Could not save the log: " + e.getMessage());
        }
    }

    /** Decodes messages the platform forwards for every currently watched agent. */
    private class PlatformSniffListener extends CyclicBehaviour {

        private final MessageTemplate template = MessageTemplate.MatchConversationId(getName() + "-event");

        @Override
        public void action() {
            ACLMessage current = receive(template);
            if (current == null) {
                block();
                return;
            }
            try {
                Occurred occurred = (Occurred) getContentManager().extractContent(current);
                EventRecord record = occurred.getWhat();
                Event event = record.getWhat();

                String payload;
                Envelope envelope;
                AID owner;
                MessageBus.Direction direction;

                if (event instanceof SentMessage sent) {
                    payload = sent.getMessage().getPayload();
                    envelope = sent.getMessage().getEnvelope();
                    owner = sent.getSender();
                    direction = MessageBus.Direction.SENT;
                } else if (event instanceof PostedMessage posted) {
                    // already shown as a SentMessage event if the sender is itself watched
                    if (watchedAgents.contains(posted.getSender())) {
                        return;
                    }
                    payload = posted.getMessage().getPayload();
                    envelope = posted.getMessage().getEnvelope();
                    owner = posted.getReceiver();
                    direction = MessageBus.Direction.RECEIVED;
                } else {
                    return;
                }

                String charset = (envelope != null && envelope.getPayloadEncoding() != null)
                        ? envelope.getPayloadEncoding() : ACLCodec.DEFAULT_CHARSET;
                StringACLCodec codec = new StringACLCodec();
                ACLMessage decoded = codec.decode(payload.getBytes(charset), charset);
                decoded.setEnvelope(envelope);

                MessageBus.publish(owner == null ? "?" : owner.getLocalName(), direction, decoded);
            } catch (Exception e) {
                gui.showError("Lost a sniffed message: could not decode it (" + e.getMessage() + ")");
            }
        }
    }

    @Override
    protected void takeDown() {
        for (AID target : new ArrayList<>(watchedAgents)) {
            try {
                send(buildSniffRequest(target, false));
            } catch (Exception ignored) {
                // the platform is going away anyway
            }
        }
        if (listener != null) {
            MessageBus.unsubscribe(listener);
        }
        if (gui != null) {
            gui.dispose();
        }
    }
}
