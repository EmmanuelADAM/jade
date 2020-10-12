package protocoles.launch;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;

public class LaunchAgents {
    public static void main(String[] args) {
        // preparer les arguments pout le conteneur JADE
        Properties prop = new ExtendedProperties();
        // demander la fenetre de controle
        prop.setProperty(Profile.GUI, "true");
        // nommer les agents
        prop.setProperty(Profile.AGENTS, "a:protocoles.agents.AgentEmissionARE;" +
                "b:protocoles.agents.AgentReceptionARE;c:protocoles.agents.AgentReceptionARE;d:protocoles.agents.AgentReceptionARE;e:protocoles.agents.AgentReceptionARE;f:protocoles.agents.AgentReceptionARE;g:protocoles.agents.AgentReceptionARE");
        // creer le profile pour le conteneur principal
        ProfileImpl profMain = new ProfileImpl(prop);
        // lancer le conteneur principal
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }
}
