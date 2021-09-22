package ticTac;

import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;

/**
 * classe qui lance 2 agencesVoyages.agents pour illustrer le filtrage de messages.
 *
 * @author emmanueladam
 * */
public class LaunchAgents extends Agent {

    public static void main(String[] args) {
        // preparer les arguments pout le conteneur JADE
        Properties prop = new ExtendedProperties();
        // demander la fenetre de controle
        prop.setProperty(Profile.GUI, "true");
        // nommer les agencesVoyages.agents
        prop.setProperty(Profile.AGENTS, "agentDecompte:ticTac.AgentPosteur;agentPiege:ticTac.AgentLecteur");
        // creer le profile pour le conteneur principal
        ProfileImpl profMain = new ProfileImpl(prop);
        // lancer le conteneur principal
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }
}
