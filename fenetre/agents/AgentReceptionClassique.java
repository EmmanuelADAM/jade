package fenetre.agents;


import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
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


        // comportement cyclique d'affichage de messages
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    // recuperation du contenu :
                    String contenu = msg.getContent();
                    // recuperation de l'adresse de l'emetteur
                    AID adresseEmetteur = msg.getSender();
                    // recuperation du nom déclaré en local de l'emetteur
                    String nomEmetteur = adresseEmetteur.getLocalName();
                    println("recu " + contenu + ", de " + nomEmetteur);
                }
                //mise en pause du comportement jusqu'au reveil force ou a l'arrivee d'un message
                block();
            }
        });
    }


}
