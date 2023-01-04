package protocols.vickrey.launch;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;

import java.util.Properties;

public class LaunchAgents {
    public static void main(String[] args) {
        // preparer les arguments pout le conteneur JADE
        Properties prop = new ExtendedProperties();
        // demander la fenetre de controle
        prop.setProperty(Profile.GUI, "true");
        // nommer les agents
        StringBuilder sb = new StringBuilder("bureau:protocols.vickrey.agents.AgentCommissairePriseur;");
        for (int i = 0; i < 5; i++)
            sb.append("votant_").append(i).append(":protocols.vickrey.agents.AgentParticipant;");
        prop.setProperty(Profile.AGENTS, sb.toString());
        // creer le profile pour le conteneur principal
        ProfileImpl profMain = new ProfileImpl(prop);
        // lancer le conteneur principal
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }
}
