package testComportement;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;

import static java.lang.System.out;

/**
 * class of an agent that contains 2 endless behaviors, one displaying 'hello', the other 'hi'.
 * the agent also has a delayed behavior that removes it from the platform
 *
 * @author emmanueladam
 */
public class AgentHelloSalut extends Agent {
    /**
     * main function.
     * launch 2 agents that acts "in parallel"
     */
    public static void main(String[] args) {
        String[] jadeArgs = new String[2];
        StringBuilder sbAgents = new StringBuilder();
        sbAgents.append("a1:testComportement.AgentHelloSalut").append(";");
        sbAgents.append("a2:testComportement.AgentHelloSalut").append(";");
        jadeArgs[0] = "-gui";
        jadeArgs[1] = sbAgents.toString();
        jade.Boot.main(jadeArgs);
    }

    /**
     * agent set-up
     */
    @Override
    protected void setup() {
        out.println("Me, Agent " + getLocalName() + ", my address is " + getAID());

        // add an "eternal" behavior which, on each pass, displays hello and pauses for at most 200 ms
        //(should be replaced by cyclic behavior, see next behavior)
        addBehaviour(new Behaviour(this) {
            public void action() {
                println("From agent " + getLocalName() + " : Hello everybody and especially you!");
                //pause at most for 200ms, or less if the agent receives a message
                block(200);
            }

            /**this behavior never ends*/
            public boolean done() {
                return false;
            }
        });

        // add a cyclic behavior that on each pass displays hi and pauses for 300ms
        addBehaviour(new TickerBehaviour(this, 300, a->{println("From agent " + a.getLocalName() + " : Hi !!!");}));

        // add a delayed behavior that demands the platform to remove the agent in 1000 ms
        addBehaviour(new WakerBehaviour(this, 1000, a->{
            out.println("From agent " + a.getLocalName() + " : well, I'm leaving...");
            a.doDelete();
        }));
    }

    // 'clean-up' of the agent
    @Override
    protected void takeDown() {
        println("Me, Agent " + getLocalName() + " I leave the platform ! ");
    }
}
