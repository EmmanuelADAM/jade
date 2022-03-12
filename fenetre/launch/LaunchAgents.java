package fenetre.launch;

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
	  prop.setProperty(Profile.AGENTS, "a:fenetre.agents.AgentDirectEmissionButton;"
	  		+ "b:fenetre.agents.AgentReceptionClassique;c:fenetre.agents.AgentReceptionClassique;d:fenetre.agents.AgentReceptionClassique");
	  // creer le profile pour le conteneur principal
	  ProfileImpl profMain = new ProfileImpl(prop);
	  // lancer le conteneur principal
	  Runtime rt = Runtime.instance();
	  rt.createMainContainer(profMain);
	  }
	}
