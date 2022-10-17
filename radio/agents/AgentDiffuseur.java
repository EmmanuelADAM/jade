package radio.agents;

import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;

import java.awt.*;


/**
 * agents associé à une fenêtre, envoie un message radio sur un canal
 *
 * @author eadam
 */
@SuppressWarnings("serial")
public class AgentDiffuseur extends AgentWindowed {
    /**
     * adresse du topic radio
     */
    AID topic;
    /**
     * no du msg envoyé
     */
    int i = 0;

    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setButtonActivated(true);
        window.setBackgroundTextColor(Color.YELLOW);
        //Creation d'un "canal radio" de nom InfoRadio
        topic = AgentServicesTools.generateTopicAID(this, "TNSID");
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
     * send messages to the topic
     */
    private void sendMessages() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(topic);
        msg.setContent("hello " + i);
        i++;
        send(msg);
    }

}
