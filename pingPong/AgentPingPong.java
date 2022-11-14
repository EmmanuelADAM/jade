package pingPong;

import jade.core.Runtime;
import jade.core.*;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.ExtendedProperties;

import java.util.Properties;

import static java.lang.System.out;

/**
 * classe d'agent pour échange entre 2 agents de cette classe.
 * L'un s'appelle ping et initie un échange avec l'agent Pong.
 *
 * @author emmanueladam
 */
public class AgentPingPong extends Agent {
    public static void main(String[] args) {
        // preparer les arguments pout le conteneur JADE
        Properties prop = new ExtendedProperties();
        // demander la fenêtre de contrôle
        prop.setProperty(Profile.GUI, "true");
        // nommer les agents
        prop.setProperty(Profile.AGENTS, "ping:pingPong.AgentPingPong;pong:pingPong.AgentPingPong");
        // creer le profile pour le conteneur principal
        ProfileImpl profMain = new ProfileImpl(prop);
        // lancer le conteneur principal
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }

    /**
     * Initialisation de l'agent
     */
    @Override
    protected void setup() {
        String texteHello = "Bonjour a toutezetatousse";

        println("De l'agent " + getLocalName() + " : " + texteHello);
        println("Mon adresse est " + getAID());
        // si l'agent s'appelle ping,
        // ajout d'un comportement qui enverra le texte 'balle' à l'agent pong dans 10 secondes
        if (getLocalName().equals("ping")) {
            long temps = 10000;
            out.println("agent " + getLocalName() + " : je commence dans " + temps + " ms");
            addBehaviour(new WakerBehaviour(this, temps) {
                protected void onWake() {
                    var msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver("pong");
                    msg.setContent("balle");
                    msg.setConversationId("SPORT");
                    myAgent.send(msg);
                    println("moi, " + getLocalName() + " je lance la balle");
                }
            });
        }

        var modele = MessageTemplate.and(
                MessageTemplate.MatchConversationId("SPORT"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        // ajout d'un comportement à 30 itérations qui attend un msg de type INFORM sur le sujet "Sport" et contenant
        // la balle et qu'il retourne à l'envoyeur après 300ms
        addBehaviour(new Behaviour(this) {
            int step = 0;

            public void action() {
                var msg = receive(modele);
                if (msg != null) {
                    step++;
                    var content = msg.getContent();
                    var sender = msg.getSender();
                    println("agent %s : j'ai reçu \"%s\" de '%s'".formatted(getLocalName(),content,
                            sender.getLocalName()));
                    myAgent.doWait(300);
                    var reply = msg.createReply();
                    reply.setContent("balle-" + step);
                    myAgent.send(reply);
                } else block();
            }

            public boolean done() {
                if (step == 20)
                    println("agent " + getLocalName() + " : je ne joue plus");
                return step == 20;
            }
        });

        var modele2 = MessageTemplate.MatchPerformative(ACLMessage.FAILURE);
        // ajout d'un comportement à 20 itérations qui attend un msg contenant la balle et la retourne à l'envoyeur
        // après 300ms
        addBehaviour(new ReceiverBehaviour(this,  -1, modele2,true, (a, msg) ->
            println("agent " + getLocalName() + " : j'ai recu un msg d erreur   de " + msg.getSender().getLocalName() + " : " + msg.getContent())
        ));
    }

    // 'Nettoyage' de l'agent
    @Override
    protected void takeDown() {
        out.println("Moi, Agent " + getLocalName() + " je quitte la plateforme ! ");
    }
}
