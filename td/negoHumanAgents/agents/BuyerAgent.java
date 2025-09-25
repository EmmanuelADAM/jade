package td.negoHumanAgents.agents;


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
public class BuyerAgent extends AgentWindowed {
    AID topic = null;
    double receivedValueSeller = 0.;
    double receivedValueBuyer = 0.;
    double proposedValue = 0;
    double maximumValue = 200;
    String object;


    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! I'm ready. ");
        //Search a "radio channel" with the name 'BestAgentsCharts'
        topic = AgentServicesTools.generateTopicAID(this, "Negociation");
        //cyclic listening on the channel
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchTopic(topic), MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
        mt = MessageTemplate.and(mt, MessageTemplate.or(
                MessageTemplate.MatchConversationId("INIT-NEGO"),
                MessageTemplate.MatchConversationId("SELLER-NEGO")
        ));
        addBehaviour(new ReceiverBehaviour(this, -1, mt, true,
                (a,msg)->
                {
                    if (msg.getConversationId().equalsIgnoreCase("INIT-NEGO"))
                    {
                        var content = msg.getContent().split(":");
                        receivedValueSeller = Double.parseDouble(content[1]);
                        object = content[0];
                        println("~".repeat(30));
                        println("~".repeat(30));
                        println("a negociation is launched for '%s' with a starting price of  \".%2f\" on the topic channel '%s', sent by %s".
                                formatted(object, receivedValueSeller, topic.getLocalName(), msg.getSender().getLocalName()));
                    }
                    else {
                        receivedValueSeller = Double.parseDouble(msg.getContent());
                        println("~".repeat(30));
                        println("%s propose this price %.2f for \"%s\" on the topic channel '%s'.".
                                formatted( msg.getSender().getLocalName(), receivedValueSeller, object, topic.getLocalName()));
                    }
                        window.setButtonActivated(true);
                }));


        mt = MessageTemplate.and(MessageTemplate.MatchTopic(topic), MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
        mt = MessageTemplate.and(mt, MessageTemplate.MatchConversationId("BUYER-NEGO"));
        addBehaviour(new ReceiverBehaviour(this, -1, mt, true,
                (a,msg)->
                {
                    receivedValueBuyer = Double.parseDouble(msg.getContent());
                    println("received \"%.2f\" on the topic channel '%s', sent by %s".
                            formatted(receivedValueBuyer, topic.getLocalName(), msg.getSender().getLocalName()));
                }));

        mt = MessageTemplate.and(MessageTemplate.MatchTopic(topic), MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL));
        addBehaviour(new ReceiverBehaviour(this, -1, mt, true,
                (a,msg)->
                {
                    println("received '%s' on the topic channel '%s', sent by %s".
                            formatted(msg.getContent(), topic.getLocalName(), msg.getSender().getLocalName()));
                    var content = msg.getContent().split(":");
                    if (content[0].equalsIgnoreCase(getLocalName())) {
                        println("~".repeat(30));
                        println("%s -> I won the negociation for \"%s\" at the price of %s".
                                formatted(getLocalName(), object, content[1]));
                        println("~".repeat(30));
                    } else {
                        println("~".repeat(30));
                        println("%s -> I lost the negociation for \"%s\" at the price of %s".
                                formatted(getLocalName(), object, content[1]));
                        println("~".repeat(30));
                    }
                    window.setButtonActivated(false);
                    receivedValueBuyer = receivedValueSeller =0;
                }));
    }

    void startBid() {
        if (receivedValueSeller > 0) {
            ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
            msg.setConversationId("BUYER-NEGO");
            msg.addReceiver(topic);
            if (receivedValueBuyer==0)
                proposedValue = (Math.random()*50 + 50);
            else proposedValue = (int)((Math.random()*0.05 + 1.01)*receivedValueBuyer);
            msg.setContent(String.valueOf(proposedValue));
            send(msg);
            println("%s\t-> I proposed %.2f".formatted(getLocalName(), proposedValue));
        }

    }

    protected void onGuiEvent(GuiEvent ev) {
        if (ev.getType() == SimpleWindow4Agent.OK_EVENT) {
            startBid();
        }
    }

}
