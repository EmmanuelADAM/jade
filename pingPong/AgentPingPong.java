package pingPong;

import jade.core.Runtime;
import jade.core.*;
import jade.core.behaviours.Behaviour;
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
public class AgentPingPong extends Agent {
    public static void main(String[] args) {
        // preparer les arguments pout le conteneur JADE
        Properties prop = new ExtendedProperties();
        // demander la fenetre de controle
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

        out.println("De l'agent " + getLocalName() + " : " + texteHello);
        out.println("Mon adresse est " + getAID());
        // si l'agent s'appelle ping,
        // ajout d'un comportement qui enverra le texte 'balle' à l'agent pong dans 10 secondes
        if (getLocalName().equals("ping")) {
            long temps = 10000;
            out.println("agent " + getLocalName() + " : je commence dans "+temps+" ms");
            addBehaviour(new WakerBehaviour(this, temps) {
                protected void onWake() {
                    var msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver(new AID("pong", AID.ISLOCALNAME));
                    msg.setContent("balle");
                    myAgent.send(msg);
                    out.println("moi, " + getLocalName() + " je lance la balle");
                }
            });
        }
        // ajout d'un comportement à 30 itérations qui attend un msg contenant la balle et la retourne à l'envoyeur après 300ms
        addBehaviour(new Behaviour(this) {
            int step = 0;

            public void action() {
                var msg = receive();
                if (msg != null) {
                    step++;
                    var content = msg.getContent();
                    var sender = msg.getSender();
                    out.println("agent " + getLocalName() + " : j'ai recu " + content + " de " + sender);
                    myAgent.doWait(300);
                    var reply = msg.createReply();
                    reply.setContent("balle-" + step);
                    myAgent.send(reply);
                } else block();
            }

            public boolean done() {
                if(step==30)
                    out.println("agent " + getLocalName() + " : je ne joue plus");
                return step == 30;
            }
        });
    }

    // 'Nettoyage' de l'agent
    @Override
    protected void takeDown() {
        out.println("Moi, Agent " + getLocalName() + " je quitte la plateforme ! ");
    }
}
