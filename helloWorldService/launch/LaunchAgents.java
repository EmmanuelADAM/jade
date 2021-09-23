package helloWorldService.launch;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.core.Runtime; 

public class LaunchAgents {
 public static void main(String[] args) {
  // preparer les arguments pout le conteneur JADE
  Properties prop = new ExtendedProperties();
  // demander la fenetre de controle
  prop.setProperty(Profile.GUI, "true");
  // nommer les agents
 //  prop.setProperty(Profile.AGENTS,"a1:helloWorldService.agents.HelloAgent(hello);a2:helloWorldService.agents.HelloAgent(salut);a3:helloWorldService.agents.HelloAgent(coucou)");
  //avec 50 agents :
  StringBuilder sb = new StringBuilder();
  String nomAgent = "jean";
  String typeAgent = ":helloWorldService.agents.HelloAgent(hello);";
  for(int i=0; i<50; i++) sb.append(nomAgent).append(i).append(typeAgent);
  prop.setProperty(Profile.AGENTS,sb.toString());
  // creer le profile pour le conteneur principal
  ProfileImpl profMain = new ProfileImpl(prop);
  // lancer le conteneur principal
  Runtime rt = Runtime.instance();
  rt.createMainContainer(profMain);
  }
}
