package td.englishAuctionOpenCry.agents;

import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.SenderBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.awt.*;


/**
 * agents linked to a window, sends a radio message on a channel
 *
 * @author eadam
 */
@SuppressWarnings("serial")
public class AuctioneerAgent extends AgentWindowed {
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
        topic = AgentServicesTools.generateTopicAID(this, "Auction");
    }

    /**
     * reaction to a gui event
     */
    protected void onGuiEvent(GuiEvent ev) {
        if (ev.getType() == SimpleWindow4Agent.OK_EVENT) {
            startAuction();
        }
    }

    /**
     * send messages on the "radio channel"
     */
    private void startAuction() {
        //START AUCTION
        ACLMessage firstMsg = new ACLMessage(ACLMessage.INFORM);
        firstMsg.addReceiver(topic);
        firstMsg.setContent("book, 100");
        send(firstMsg);
        addBehaviour(new WakerBehaviour(this, 100, (a)->a.send(firstMsg)));
        //WAIT 4 BID
        addBehaviour(new ReceiverBehaviour(this, -1, MessageTemplate.MatchTopic(topic), true,(a,msg)->{
            println("received \"%s\" on the topic channel '%s', sent by %s".
                    formatted(msg.getContent(), topic.getLocalName(),msg.getSender().getLocalName()));
        }));
    }

}
