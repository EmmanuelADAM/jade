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
 * Agent class to allow exchange of messages between an agent named ping, that initiates the 'dialog', and an agent
 * named 'pong'
 *
 * @author emmanueladam
 */
public class AgentPingPong extends Agent {

    /**
     * agent setup, adds its behaviours
     */
    @Override
    protected void setup() {

        println(getLocalName() + " -> Hello, my address is " + getAID());
        // if the agent names "ping"
        // add a behaviour that will send the first "ball" msg in 10 sec. to "pong" agent
        if (getLocalName().equals("ping")) {
            long temps = 10000;
            out.println(getLocalName() + " -> I start in" + temps + " ms");
            addBehaviour(new WakerBehaviour(this, temps) {
                protected void onWake() {
                    var msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver("pong");
                    msg.setContent("ball");
                    msg.setConversationId("SPORT");
                    myAgent.send(msg);
                    println(getLocalName() + " -> I launch the ball");
                }
            });
        }

        var modele = MessageTemplate.and(
                MessageTemplate.MatchConversationId("SPORT"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        // add a behavior, with 20 iterations, that wait for a 'INFORM' msg about 'SPORT' and replies to it after 300ms
        addBehaviour(new Behaviour(this) {
            int step = 0;

            public void action() {
                var msg = receive(modele);
                if (msg != null) {
                    step++;
                    var content = msg.getContent();
                    var sender = msg.getSender();
                    println("%s -> I received \"%s\" from '%s'".formatted(getLocalName(),content,sender.getLocalName()));
                    myAgent.doWait(300);
                    var reply = msg.createReply();
                    reply.setContent("ball-" + step);
                    myAgent.send(reply);
                } else block();
            }

            public boolean done() {
                if (step == 20)
                    println(getLocalName() + " -> I don't play anymore");
                return step == 20;
            }
        });

        var modele2 = MessageTemplate.MatchPerformative(ACLMessage.FAILURE);
        // add a behaviour that wait for an eventual failure msg
        addBehaviour(new ReceiverBehaviour(this,  -1, modele2,true, (a, msg) ->
            println(getLocalName() + " -> I received an error msg from " + msg.getSender().getLocalName() + " : " + msg.getContent())
        ));
    }

    /**I inform the user when I leave the platform*/
    @Override
    protected void takeDown() {
        out.println(getLocalName() + " -> I leave the plateform ! ");
    }

    public static void main(String[] args) {
        // prepare argument for the JADE container
        Properties prop = new ExtendedProperties();
        // display a control/debug window
        prop.setProperty(Profile.GUI, "true");
        // declare the agents
        prop.setProperty(Profile.AGENTS, "ping:pingPong.AgentPingPong;pong:pingPong.AgentPingPong");
        // create the ain container
        ProfileImpl profMain = new ProfileImpl(prop);
        // launch it !
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }
}
