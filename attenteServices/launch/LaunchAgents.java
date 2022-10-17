package attenteServices.launch;

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
        StringBuilder sb = new StringBuilder("a:attenteServices.agents.AgentPointeur;");
        for (int i = 0; i < 10; i++)
            sb.append("jean_").append(i).append(":attenteServices.agents.AgentVenant;");
        prop.setProperty(Profile.AGENTS, sb.toString());
        prop.setProperty(Profile.MAIN_PORT, "1234");
        // creer le profile pour le conteneur principal
        ProfileImpl profMain = new ProfileImpl(prop);
        // lancer le conteneur principal
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }
}
