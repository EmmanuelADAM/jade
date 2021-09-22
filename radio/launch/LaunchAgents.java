package radio.launch;

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
	  // add the Topic Management Service
	  prop.setProperty(Profile.SERVICES, "jade.core.messaging.TopicManagementService;jade.core.event.NotificationService");
	  // nommer les agencesVoyages.agents
	  prop.setProperty(Profile.AGENTS, "a:radio.agencesVoyages.agents.AgentDiffuseur;"
	  		+ "b:radio.agencesVoyages.agents.AgentAuditeur;c:radio.agencesVoyages.agents.AgentAuditeur;d:radio.agencesVoyages.agents.AgentAuditeur");
	  // creer le profile pour le conteneur principal
	  ProfileImpl profMain = new ProfileImpl(prop);
	  // lancer le conteneur principal
	  Runtime rt = Runtime.instance();
	  rt.createMainContainer(profMain);
	  }
	}
