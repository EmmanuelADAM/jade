package helloworldSolo;

import jade.core.Agent;

import static java.lang.System.out;

/**
 * A simple agent that display a text found in its parameters
 *
 * @author emmanueladam
 */
public class AgentHelloParametre extends Agent {
    /**
     * this main launch JADE plateforme and asks it to create an agent
     */
    public static void main(String[] args) {
        String[] jadeArgs = new String[2];
        StringBuilder sbAgents = new StringBuilder();
        sbAgents.append("agentA:helloworldSolo.AgentHelloParametre(Hi)").append(";");
        sbAgents.append("agentB:helloworldSolo.AgentHelloParametre(Hello)").append(";");
        jadeArgs[0] = "-gui";
        jadeArgs[1] = sbAgents.toString();
        jade.Boot.main(jadeArgs);
    }

    /**
     * agent set-up
     */
    @Override
    protected void setup() {
        String texteHello = null;
        Object[] params = this.getArguments();
        if (params.length > 0) texteHello = (String) params[0];
        else texteHello = "Hello everybody and especially you !";

        println("From agent " + getLocalName() + " : " + texteHello);
        println("My address is " + getAID());
    }

    // 'Nettoyage' de l'agent
    @Override
    protected void takeDown() {
        println("Me, Agent " + getLocalName() + " I leave the platform ! ");
    }
}
