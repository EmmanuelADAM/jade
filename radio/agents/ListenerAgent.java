package radio.agents;


import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.behaviours.ReceiverBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.MessageTemplate;

/**
 * agent linked to a window that listens continuously for messages on a topic
 *
 * @author eadam
 */
public class ListenerAgent extends AgentWindowed {
    AID topic = null;


    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! I'm ready. ");
        //Search a "radio channel" with the name 'BestAgentsCharts'
        topic = AgentServicesTools.generateTopicAID(this, "BestAgentsCharts");
        //cyclic listening on the channel
        final MessageTemplate mt = MessageTemplate.MatchTopic(topic);
        addBehaviour(new ReceiverBehaviour(this, -1, mt, true,
                (a,msg)-> println("received \"%s\" on the topic channel '%s', sent by %s".
                        formatted(msg.getContent(), topic.getLocalName(),msg.getSender().getLocalName()))));

    }


}
