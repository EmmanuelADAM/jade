package protocoles.requetes.launch;

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
        // nommer les agencesVoyages.agents
        StringBuilder sb = new StringBuilder("a:protocoles.requetes.agents.AgentEmissionARE;");
        String typeAgent = ":protocoles.requetes.agents.AgentReceptionARE;";
        for(int i=0; i<10; i++)
            sb.append((char)(98+i)).append(typeAgent);
        prop.setProperty(Profile.AGENTS, sb.toString());
        // creer le profile pour le conteneur principal
        ProfileImpl profMain = new ProfileImpl(prop);
        // lancer le conteneur principal
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }
}
