package fenetre.agents;


import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;

/**
 * agent lié à une fenêtre, qui attend des messages et les affiche
 *
 * @author eadam
 */
@SuppressWarnings("serial")
public class AgentReceptionClassique extends AgentWindowed {

    /**
     * initialise l'agent
     * et ajoute un comportement cylcique d'attente de messages
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent  " + getAID().getName() + " is ready. ");


        // comportement cyclique d'affichage de messages de tous types
        addBehaviour(new ReceiverBehaviour(this, -1, null, true, (a,msg)->
                    println("recu " + msg.getContent() + ", de " + msg.getSender().getLocalName())));
    }


}
