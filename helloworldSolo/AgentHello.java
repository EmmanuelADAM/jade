package helloworldSolo;

import jade.core.Agent;

/**
 * A simple agent that display a text
 *
 * @author emmanueladam
 */
public class AgentHello extends Agent {
    /**
     * this main launch JADE plateforme and asks it to create an agent
     */
    public static void main(String[] args) {
        String[] jadeArgs = new String[2];
        StringBuilder sbAgents = new StringBuilder();
        sbAgents.append("myFirstAgent:helloworldSolo.AgentHello").append(";");
        jadeArgs[0] = "-gui";
        jadeArgs[1] = sbAgents.toString();
        jade.Boot.main(jadeArgs);
    }

    /**
     * agent set-up
     */
    @Override
    protected void setup() {
        String texteHello = "Hello everybody and especially you !";

        println("From agent " + getLocalName() + " : " + texteHello);
        println("My address is " + getAID());
        //agent asks to be removed from the platform
        doDelete();
    }

    // 'clean-up' of the agent
    @Override
    protected void takeDown() {
        println("Me, Agent " + getLocalName() + " I leave the platform ! ");
    }
}
