package monitoring;

import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPANames;
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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Modern, drop-in replacement for {@code jade.tools.sniffer.Sniffer}: opens
 * a monitoring window showing every message exchanged by watched agents,
 * as a live table and a sequence diagram - exactly what the original tool
 * offers.
 * <p>
 * Type an agent's local name in the "Watch an agent" field: this sends the
 * real FIPA/JADE {@code SniffOn} request to the AMS, the same mechanism
 * {@code jade.tools.sniffer.Sniffer} itself uses, so the platform starts
 * forwarding a copy of every message that agent sends/receives. No change
 * is needed to the watched agent's code.
 * <p>
 * Launch it like the original tool, e.g. from a Jade profile:
 * {@code "mySniffer:monitoring.ModernSnifferAgent"}.
 *
 * @author emmanuel adam
 * @version 1
 */
public class ModernSnifferAgent extends GuiAgent {

    private ModernSnifferGui gui;
    private final Set<AID> watchedAgents = new LinkedHashSet<>();

    @Override
    protected void setup() {
        getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL0);
        getContentManager().registerOntology(JADEManagementOntology.getInstance());
        getContentManager().registerOntology(IntrospectionOntology.getInstance());

        gui = new ModernSnifferGui(this);

        // Keeping an AMS platform-events subscription active is what makes the
        // platform actually deliver SniffOn events for every agent class
        // (including jade.gui.GuiAgent/AgentWindowed subclasses) - without it,
        // the events are silently dropped for those agents.
        addBehaviour(new AMSSubscriber() {
            @Override
            protected void installHandlers(Map<String, EventHandler> handlersTable) {
                // no handlers needed: only the subscription itself matters here
            }
        });

        addBehaviour(new PlatformSniffListener());
    }

    @Override
    protected void onGuiEvent(GuiEvent ev) {
        switch (ev.getType()) {
            case ModernSnifferGui.WATCH -> watch((String) ev.getParameter(0));
            case ModernSnifferGui.UNWATCH -> unwatch((String) ev.getParameter(0));
            case ModernSnifferGui.CLEAR -> gui.clearAll();
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
                    gui.showError("The platform refused to " + (on ? "watch" : "stop watching") + " " + target.getLocalName());
                }

                @Override
                protected void handleFailure(ACLMessage reply) {
                    gui.showError("Could not " + (on ? "watch" : "stop watching") + " " + target.getLocalName()
                            + ": " + reply.getContent());
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
                Event event = occurred.getWhat().getWhat();

                String payload;
                Envelope envelope;

                if (event instanceof SentMessage sent) {
                    payload = sent.getMessage().getPayload();
                    envelope = sent.getMessage().getEnvelope();
                    gui.addTrace("SENT", decode(payload, envelope));
                } else if (event instanceof PostedMessage posted) {
                    // already shown as a SentMessage event if the sender is itself watched
                    if (!watchedAgents.contains(posted.getSender())) {
                        payload = posted.getMessage().getPayload();
                        envelope = posted.getMessage().getEnvelope();
                        gui.addTrace("RECEIVED", decode(payload, envelope));
                    }
                }
            } catch (Exception e) {
                gui.showError("Lost a sniffed message: could not decode it (" + e.getMessage() + ")");
            }
        }

        private ACLMessage decode(String payload, Envelope envelope) throws ACLCodec.CodecException, java.io.UnsupportedEncodingException {
            String charset = (envelope != null && envelope.getPayloadEncoding() != null)
                    ? envelope.getPayloadEncoding() : ACLCodec.DEFAULT_CHARSET;
            ACLMessage decoded = new StringACLCodec().decode(payload.getBytes(charset), charset);
            decoded.setEnvelope(envelope);
            return decoded;
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
        if (gui != null) {
            gui.dispose();
        }
    }
}
