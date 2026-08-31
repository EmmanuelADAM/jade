package monitoring;

import jade.core.behaviours.CyclicBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

/**
 * Modern, drop-in replacement for {@code jade.tools.DummyAgent.DummyAgent}:
 * an agent that opens a window to freely compose and send an ACL message to
 * any other agent, and displays every sent/received message in a styled
 * history.
 * <p>
 * Launch it exactly like the original tool, e.g. from a Jade profile:
 * {@code "myDummy:monitoring.ModernDummyAgent"}.
 *
 * @author emmanuel adam
 * @version 1
 */
public class ModernDummyAgent extends GuiAgent {

    private ModernDummyAgentGui gui;

    @Override
    protected void setup() {
        gui = new ModernDummyAgentGui(this);

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive();
                if (msg == null) {
                    block();
                    return;
                }
                gui.addMessage("RECEIVED", msg);
            }
        });
    }

    @Override
    protected void onGuiEvent(GuiEvent ev) {
        switch (ev.getType()) {
            case ModernDummyAgentGui.SEND -> sendComposedMessage();
            case ModernDummyAgentGui.CLEAR -> gui.clearHistory();
            case ModernDummyAgentGui.QUIT -> doDelete();
            default -> { /* nothing else to handle */ }
        }
    }

    private void sendComposedMessage() {
        ACLMessage msg = gui.getComposedMessage();
        if (!msg.getAllReceiver().hasNext()) {
            return;
        }
        send(msg);
        gui.addMessage("SENT", msg);
    }

    @Override
    protected void takeDown() {
        if (gui != null) {
            gui.dispose();
        }
    }
}
