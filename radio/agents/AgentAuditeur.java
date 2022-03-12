package radio.agents;


import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.behaviours.CyclicBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * agent lié à une fenêtre qui ecoute en boucle des messages sur un topic
 *
 * @author eadam
 */
public class AgentAuditeur extends AgentWindowed {
    AID topic = null;


    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent  " + getAID().getName() + " is ready. ");
        //recherche d'un "canal radio" de nom InfoRadio
        topic = AgentServicesTools.generateTopicAID(this, "InfoRadio");

        //ecoute cyclique sur le canal radio
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive(MessageTemplate.MatchTopic(topic));
                while (msg != null) {
                    println("recu " + msg.getContent() + " du canal " + topic.getLocalName() + ", emis par " + msg.getSender().getLocalName());
                    msg = myAgent.receive(MessageTemplate.MatchTopic(topic));
                }
                block();
            }
        });

    }


    /**
     * @return the window
     */
    public SimpleWindow4Agent getWindow() {
        return window;
    }


    /**
     * @param window the window to set
     */
    public void setWindow(SimpleWindow4Agent window) {
        this.window = window;
    }


}
