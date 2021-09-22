package protocoles.anglaisesscellees.launch;

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
        StringBuilder sb = new StringBuilder("a:protocoles.anglaisesscellees.agents.AgentCommissairePriseur;");
        for(int i=0; i<10; i++)
            sb.append("encherisseur_").append(i).append(":protocoles.anglaisesscellees.agents.AgentParticipant;");
        prop.setProperty(Profile.AGENTS, sb.toString());
        // creer le profile pour le conteneur principal
        ProfileImpl profMain = new ProfileImpl(prop);
        // lancer le conteneur principal
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }
}
