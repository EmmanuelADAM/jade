package protocoles.negociation;

import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.ExtendedProperties;

import java.awt.*;
import java.util.Properties;

import static java.lang.System.out;

/**
 * classe d'agent pour échange entre 2 agents de cette classe. l'un s'appelle ping et initie un échange avec l'agent pong.
 *
 * @author emmanueladam
 */
public class Negociateur extends AgentWindowed {

    double seuil;
    double prixSouhaite = 100;
    /**
     * Initialisation de l'agent
     */
    @Override
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setButtonActivated(true);
        // si l'agent s'appelle vendeur,
        // ajout d'un comportement qui enverra le texte 'balle' à l'agent pong dans 10 secondes
        if (getLocalName().equals("vendeur")) {
            window.setBackgroundTextColor(Color.lightGray);
            println("Cliquez pour lancer la négociation...");

            var modele =  MessageTemplate.MatchConversationId("MARCHE");
            // ajout d'un comportement à 30 itérations qui attend un msg contenant la balle et la retourne à l'envoyeur après 300ms
            addBehaviour(new CompVendeur(this, modele));
        }

        if (getLocalName().equals("acheteur")) {
            var modele =  MessageTemplate.MatchConversationId("MARCHE");
            // ajout d'un comportement à 30 itérations qui attend un msg contenant la balle et la retourne à l'envoyeur après 300ms
            addBehaviour(new CompAcheteur(this, modele));
        }

    }

    protected void println(String str){super.println(str);}

    // 'Nettoyage' de l'agent
    @Override
    protected void takeDown() {
        out.println("Moi, Agent " + getLocalName() + " je quitte la plateforme ! ");
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
     * send messages to agents b, c & d
     */
    private void sendMessages() {
        var msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver("acheteur");
        msg.setContent(String.valueOf(prixSouhaite));
        msg.setConversationId("MARCHE");
        send(msg);
        println("moi, " + getLocalName() + " je lance la négociation");
    }


    public static void main(String[] args) {
        // preparer les arguments pout le conteneur JADE
        Properties prop = new ExtendedProperties();
        // demander la fenetre de controle
        prop.setProperty(Profile.GUI, "true");
        // nommer les agents
        prop.setProperty(Profile.AGENTS, "acheteur:protocoles.negociation.Negociateur;vendeur:protocoles.negociation" +
                ".Negociateur");
        // creer le profile pour le conteneur principal
        ProfileImpl profMain = new ProfileImpl(prop);
        // lancer le conteneur principal
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }

}
