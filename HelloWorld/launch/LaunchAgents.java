package launch;

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
  prop.setProperty(Profile.AGENTS,"a1:agents.HelloAgent(hello);a2:agents.HelloAgent(salut);a3:agents.HelloAgent(coucou)");
  //avec 100 agents :
//  StringBuffer sb = new StringBuffer();
//  String typeAgent = ":agents.HelloAgent(hello);";
//  for(int i=0; i<100; i++) sb.append("a"+i+typeAgent);
//  prop.setProperty(Profile.AGENTS,sb.toString());
  // creer le profile pour le conteneur principal
  ProfileImpl profMain = new ProfileImpl(prop);
  // lancer le conteneur principal
  Runtime rt = Runtime.instance();
  rt.createMainContainer(profMain);
  }
}
