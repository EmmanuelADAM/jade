package helloworldSolo;

import jade.core.Agent;

import static java.lang.System.out;

/**
 * un simple agent qui affiche un texte
 *
 * @author emmanueladam
 */
public class AgentHello extends Agent {
    /**
     * ce main lance la plateforme jade et lui demande de creer un agent
     */
    public static void main(String[] args) {
        String[] jadeArgs = new String[2];
        StringBuilder sbAgents = new StringBuilder();
        sbAgents.append("monPremierAgent:helloworldSolo.AgentHello").append(";");
        jadeArgs[0] = "-gui";
        jadeArgs[1] = sbAgents.toString();
        jade.Boot.main(jadeArgs);
    }

    /**
     * Initialisation de l'agent
     */
    @Override
    protected void setup() {
        String texteHello = "Bonjour a toutezetatousse";

        println("De l'agent " + getLocalName() + " : " + texteHello);
        println("Mon adresse est " + getAID());
        //l'agent demande son retrait de la plateforme
//        doDelete();
    }

    // 'Nettoyage' de l'agent
    @Override
    protected void takeDown() {
        println("Moi, Agent " + getLocalName() + " je quitte la plateforme ! ");
    }
}
