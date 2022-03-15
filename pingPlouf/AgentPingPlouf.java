package pingPlouf;

import jade.core.Runtime;
import jade.core.*;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.ExtendedProperties;

import java.util.Properties;

import static java.lang.System.out;

/**
 * classe d'agent pour échange entre 2 agents de cette classe. l'un s'appelle ping et initie un échange avec l'agent pong.
 *
 * @author emmanueladam
 */
public class AgentPingPlouf extends Agent {

    /**
     * Initialisation de l'agent
     */
    @Override
    protected void setup() {
        String texteHello = "Bonjour a toutezetatousse";
        out.println("De l'agent " + getLocalName() + " : " + texteHello);
        out.println("Mon adresse est " + getAID());

        out.println("J'envoie un message a une mauvaise adresse. ");
        out.println("En multiagent, ca ne plante pas..  ");
        out.println("Mais si je veux, je peux etre a l'ecoute de l'agent AMS pour verifier s'il n'y a pas d'erreur d'adressage  ");

        out.println("Envoie dans 1 seconde....");

        //envoi d'un message a un agent n'existant pas dans 1 seconde
        addBehaviour(new WakerBehaviour(this, 1000){
            @Override
            public void onWake() {
                // envoie du texte 'texteHello' à l'agent tzoing
                String nameOther = "tzoing";
                out.println("agent " + getLocalName() + " : j'envoie un message a " + nameOther);
                var msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(new AID("nameOther", AID.ISLOCALNAME));
                msg.setContent(texteHello);
                send(msg);
            }});

        // ajout d'un comportement qui attend des messages et les affiche le cas échéant
        addBehaviour(new CyclicBehaviour(this){
            @Override
            public void action() {
                ACLMessage msg = receive();
                if(msg != null) {
                    out.println("j'ai recu ceci de la part de " + msg.getSender().getLocalName() + " : ");
                    out.println(msg);
                    out.println("je suis donc au courant par les pages blanches du mauvais adressage ....");
                }
                else block();
            }
        });

    }

    // 'Nettoyage' de l'agent
    @Override
    protected void takeDown() {
        out.println("Moi, Agent " + getLocalName() + " je quitte la plateforme ! ");
    }

    public static void main(String[] args) {
        // preparer les arguments pout le conteneur JADE
        Properties prop = new ExtendedProperties();
        // demander la fenetre de controle
        prop.setProperty(Profile.GUI, "true");
        // nommer les agents
        prop.setProperty(Profile.AGENTS, "ping:pingPlouf.AgentPingPlouf");
        // creer le profile pour le conteneur principal
        ProfileImpl profMain = new ProfileImpl(prop);
        // lancer le conteneur principal
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }
}
