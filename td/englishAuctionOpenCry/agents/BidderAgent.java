package td.englishAuctionOpenCry.agents;


import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.behaviours.ReceiverBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * agent linked to a window that listens continuously for messages on a topic
 *
 * @author eadam
 */
public class BidderAgent extends AgentWindowed {
    AID topic = null;
    int[] receivedValue = {0};
    int proposedValue = 0;


    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! I'm ready. ");
        //Search a "radio channel" with the name 'BestAgentsCharts'
        topic = AgentServicesTools.generateTopicAID(this, "Auction");
        //cyclic listening on the channel
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchTopic(topic), MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        addBehaviour(new ReceiverBehaviour(this, -1, mt, true,
                (a,msg)->
                {
                    var content = msg.getContent().split(":");
                    if(receivedValue[0]==0) {
                        receivedValue[0] = Integer.parseInt(content[0]);
                        println("~".repeat(30));
                        println("~".repeat(30));
                        println("an auction is launched for '%s' with a starting price of  \"%s\" on the topic channel '%s', sent by %s".
                                formatted(content[1], content[0], topic.getLocalName(), msg.getSender().getLocalName()));
                        window.setButtonActivated(true);
                    }
                    else
                    {
                        receivedValue[0] = Integer.parseInt(content[0]);
                        println("auctionner " + msg.getSender().getLocalName() + " : " + content[1]);

                    }
                }));
        mt = MessageTemplate.and(MessageTemplate.MatchTopic(topic), MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
        addBehaviour(new ReceiverBehaviour(this, -1, mt, true,
                (a,msg)->
                {
                    receivedValue[0] = Integer.parseInt(msg.getContent());
                    println("received \"%d\" on the topic channel '%s', sent by %s".
                            formatted(receivedValue[0], topic.getLocalName(), msg.getSender().getLocalName()));
                }));
        mt = MessageTemplate.and(MessageTemplate.MatchTopic(topic), MessageTemplate.MatchPerformative(ACLMessage.CONFIRM));
        addBehaviour(new ReceiverBehaviour(this, -1, mt, true,
                (a,msg)->
                {
                    println("received '%s' on the topic channel '%s', sent by %s".
                            formatted(msg.getContent(), topic.getLocalName(), msg.getSender().getLocalName()));
                    window.setButtonActivated(false);
                    receivedValue[0] =0;
                }));
    }

    void startBid() {
        if (receivedValue[0] > 0) {
            ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
            msg.addReceiver(topic);
            proposedValue = (int)((Math.random()*0.3 + 1)*receivedValue[0]);
            msg.setContent(String.valueOf(proposedValue));
            send(msg);
            println("%s\t-> I proposed %d".formatted(getLocalName(), proposedValue));
        }

    }

    protected void onGuiEvent(GuiEvent ev) {
        if (ev.getType() == SimpleWindow4Agent.OK_EVENT) {
            startBid();
        }
    }

}
