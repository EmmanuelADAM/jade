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
        println("De l'agent " + getLocalName() + " : " + texteHello + ", mon adresse est " + getAID());

        println(getLocalName() + " -> J'envoie un message a une mauvaise adresse. ");
        println(getLocalName() + " -> En multiagent, ca ne plante pas..  ");
        println(getLocalName() + " -> Mais si je veux, je peux être a l'écoute de l'agent AMS pour verifier s'il n'y " +
                "a pas d'erreur d'adressage  ");

        println(getLocalName() + " -> Envoi dans 15 seconde....");

        //envoi d'un message a un agent n'existant pas dans 1 seconde
        addBehaviour(new WakerBehaviour(this, 15000){
            @Override
            public void onWake() {
                // envoie du texte 'texteHello' à l'agent tzoing
                String nameOther = "tzoing";
                println(getLocalName() + " ->  : j'envoie un message a " + nameOther);
                var msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(nameOther);
                msg.setContent(texteHello);
                send(msg);
            }});

        // ajout d'un comportement qui attend des messages et les affiche le cas échéant
        addBehaviour(new CyclicBehaviour(this){
            @Override
            public void action() {
                ACLMessage msg = receive();
                if(msg != null) {
                    println(getLocalName() + " -> j'ai reçu ceci de la part de " + msg.getSender().getLocalName() + " : ");
                    println(msg.toString());
                    println(getLocalName() + " -> je suis donc au courant par les pages blanches du mauvais adressage ....");
                }
                else block();
            }
        });

    }

    // 'Nettoyage' de l'agent
    @Override
    protected void takeDown() {
        println("Moi, Agent " + getLocalName() + " je quitte la plateforme ! ");
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
