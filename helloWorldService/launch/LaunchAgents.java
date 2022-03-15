package helloWorldService.launch;

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
        // nommer les agents jean0, jean1, ....
        StringBuilder sb = new StringBuilder();
        String nomAgent = "jean";
        String typeAgent = ":helloWorldService.agents.HelloAgent(hello);";
        for (int i = 0; i < 10; i++) sb.append(nomAgent).append(i).append(typeAgent);
        // demander leurs creations au demarrage de Jade
        prop.setProperty(Profile.AGENTS, sb.toString());
        // creer le profile pour le conteneur principal
        ProfileImpl profMain = new ProfileImpl(prop);
        // lancer le conteneur principal
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }
}
