package pingPlouf;

import jade.core.Runtime;
import jade.core.*;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.ExtendedProperties;

import java.util.Properties;

import static java.lang.System.out;

/**
 * Agent class to allow exchange of messages between an agent named ping, that initiates the 'dialog', and an agent
 * named 'tzoing'; the problem being that this agent doesn't exist *
 * @author emmanueladam
 */
public class AgentPingPlouf extends Agent {

    /**
     * agent setup, adds its behaviours
     * */
    @Override
    protected void setup() {
        println(getLocalName() + " -> Hello, my address is " + getAID());

        // if the agent names "ping"
        // add a behaviour that will send the first "ball" msg in 10 sec. to "pong" agent that doesn't exist
        if (getLocalName().equals("ping")) {
            println(getLocalName() + " -> I send a message to a bad address. ");
            println(getLocalName() + " -> In good multiagent platform, there is no crash..  ");
            println(getLocalName() + " -> I can listen if the AMS agent inform me about an eventual problem.");
            long temps = 15000;
            out.println(getLocalName() + " -> I start in " + temps + " ms");
            addBehaviour(new WakerBehaviour(this, temps) {
                protected void onWake() {
                    var msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver("tzoing"); //BAD AGENT NAME
                    msg.setContent("ball");
                    myAgent.send(msg);
                    println(getLocalName() + " -> I launch the ball to tzoing.");
                }
            });
        }


        // add a behaviour that wait for an eventual failure msg
        var failureMsgTemplate = MessageTemplate.MatchPerformative(ACLMessage.FAILURE);
        addBehaviour(new ReceiverBehaviour(this,  -1, failureMsgTemplate,true, (a, msg) ->
                println(getLocalName() + " -> I received an error msg from " + msg.getSender().getLocalName() + " : " + msg.getContent())
        ));

    }

    /**I inform the user when I leave the platform*/
    @Override
    protected void takeDown() {
        println("Moi, Agent " + getLocalName() + " je quitte la plateforme ! ");
    }

    public static void main(String[] args) {
        // prepare argument for the JADE container
        Properties prop = new ExtendedProperties();
        // display a control/debug window
        prop.setProperty(Profile.GUI, "true");
        // declare the agents
        prop.setProperty(Profile.AGENTS, "ping:pingPlouf.AgentPingPlouf;pong:pingPlouf.AgentPingPlouf");
        // create the ain container
        ProfileImpl profMain = new ProfileImpl(prop);
        // launch it !
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }
}
