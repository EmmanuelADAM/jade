package helloworldSolo;

import jade.core.Agent;

import static java.lang.System.out;

/**
 * un simple agent qui affiche un texte passé en paramètre
 * @author emmanueladam
 * */
public class AgentHelloParametre extends Agent {
    /**
     * Initialisation de l'agent
     */
    @Override
    protected void setup() {
        String texteHello = null;
        Object[]params = this.getArguments();
        if(params.length>0) texteHello = (String) params[0];
        else texteHello = "Bonjour à toutezétatousse";

        out.println("De l'agent " + getLocalName() + " : " + texteHello);
        out.println("Mon adresse est " + getAID());
        //l'agent demande son retrait de la plateforme..
        doDelete();
    }

    // 'Nettoyage' de l'agent
    @Override
    protected void takeDown() {
        out.println("Moi, Agent " + getLocalName() + " je quitte la plateforme ! ");
    }

    public static void main(String[] args) {
        String[] jadeArgs = new String[2];
        StringBuilder sbAgents = new StringBuilder();
        sbAgents.append("agentA:helloworldSolo.AgentHelloParametre(coucou)").append(";");
        sbAgents.append("agentB:helloworldSolo.AgentHelloParametre(bonjour)").append(";");
        jadeArgs[0] = "-agencesVoyages.gui";
        jadeArgs[1] = sbAgents.toString();
        jade.Boot.main(jadeArgs);
    }
}
