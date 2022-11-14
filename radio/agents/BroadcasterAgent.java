package radio.agents;

import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;

import java.awt.*;


/**
 * agents linked to a window, sends a radio message on a channel
 *
 * @author eadam
 */
@SuppressWarnings("serial")
public class BroadcasterAgent extends AgentWindowed {
    /**
     * address of the radio topic
     */
    AID topic;
    /**
     * no of the sent msg
     */
    int i = 0;

    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! I'm ready, my address is " + this.getAID().getName());
        window.setButtonActivated(true);
        window.setBackgroundTextColor(Color.YELLOW);
        //Create a "radio channel" with the name 'BestAgentsCharts'
        topic = AgentServicesTools.generateTopicAID(this, "BestAgentsCharts");
    }

    /**
     * reaction to a gui event
     */
    protected void onGuiEvent(GuiEvent ev) {
        if (ev.getType() == SimpleWindow4Agent.OK_EVENT) {
            sendMessages();
        }
    }

    /**
     * send messages on the "radio channel"
     */
    private void sendMessages() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(topic);
        msg.setContent("hello " + i);
        i++;
        send(msg);
    }

}
