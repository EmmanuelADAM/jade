package ticTac;

import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.MessageTemplate;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;

import static java.lang.System.out;

/**
 * classe qui lance 2 agents pour illustrer le filtrage de messages.
 *
 * @author emmanueladam
 * */
public class LaunchAgents extends Agent {

    public static void main(String[] args) {
        // preparer les arguments pout le conteneur JADE
        Properties prop = new ExtendedProperties();
        // demander la fenetre de controle
        prop.setProperty(Profile.GUI, "true");
        // nommer les agents
        prop.setProperty(Profile.AGENTS, "decompte:ticTac.AgentPosteur;agentPiege:ticTac.AgentLecteur");
        // creer le profile pour le conteneur principal
        ProfileImpl profMain = new ProfileImpl(prop);
        // lancer le conteneur principal
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }
}
