package pingPong;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

import static java.lang.System.out;

/**
 * classe d'agent pour échange entre 2 agents de cette classe. l'un s'appelle ping et initie un échange avec l'agent pong.
 *
 * @author emmanueladam
 * */
public class AgentPingPong extends Agent {
    /**
     * Initialisation de l'agent
     */
    @Override
    protected void setup() {
        String texteHello = "Bonjour à toutezétatousse";

        out.println("De l'agent " + getLocalName() + " : " + texteHello);
        out.println("Mon adresse est " + getAID());
        // si l'agent s'appelle ping,
        // ajout d'un comportement qui enverra le texte 'balle' à l'agent pong dans 10 secondes
        if(getLocalName().equals("ping")) {
            addBehaviour(new WakerBehaviour(this, 10000) {
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
                        int step=0;
                         public void action() {
                             var msg = receive();
                             if(msg!=null) {
                                 step++;
                                 var content = msg.getContent();
                                 var sender = msg.getSender();
                                 out.println("agent " + getLocalName() + " : j'ai reçu " + content + " de " + sender);
                                 myAgent.doWait(300);
                                 var reply = msg.createReply();
                                 reply.setContent("balle-"+step);
                                 myAgent.send(reply);
                             }  else block();
                         }
                         public boolean done() { return step==30; }
                         });
    }

    // 'Nettoyage' de l'agent
    @Override
    protected void takeDown() {
        out.println("Moi, Agent " + getLocalName() + " je quitte la plateforme ! ");
    }

    public static void main(String[] args) {
        String[] jadeArgs = new String[2];
        StringBuilder sbAgents = new StringBuilder();
        sbAgents.append("ping:pingPong.AgentPingPong").append(";");
        sbAgents.append("pong:pingPong.AgentPingPong").append(";");
        jadeArgs[0] = "-gui";
        jadeArgs[1] = sbAgents.toString();
        jade.Boot.main(jadeArgs);
    }
}
