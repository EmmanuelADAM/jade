package window.agents;

import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;

import java.awt.*;


/**
 * agent associated with a window, sends a direct message to agents b,c,d when the window informs him that the button
 * has been clicked
 *
 * @author emmanueladam
 */
@SuppressWarnings("serial")
public class SenderAgentWithWindow extends AgentWindowed {

    /**
     * agent set-up
     * */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! I'm ready, my address is " + this.getAID().getName());
        window.setBackgroundTextColor(Color.LIGHT_GRAY);
        window.setButtonActivated(true);
    }

    /**
     * reaction to a gui event
     */
    protected void onGuiEvent(GuiEvent ev) {
        switch (ev.getType()) {
            case SimpleWindow4Agent.OK_EVENT -> sendMessages();
        }
    }

    /**
     * send a message to agents b, c & d
     */
    private void sendMessages() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent("Hello !");
        msg.addReceivers("b", "c", "d");
        send(msg);
    }


}
