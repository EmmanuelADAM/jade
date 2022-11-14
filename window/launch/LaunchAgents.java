package window.launch;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;

import java.util.Properties;

public class LaunchAgents {
    public static void main(String[] args) {
        // prepare arguments for the Jade container
        Properties prop = new ExtendedProperties();
        // -- add a control/debug window
        prop.setProperty(Profile.GUI, "true");
        // -- add the agents
        prop.setProperty(Profile.AGENTS, "a:window.agents.SenderAgentWithWindow;"
                + "b:window.agents.ReceiverAgentWithWindow;c:window.agents.ReceiverAgentWithWindow;d:window.agents.ReceiverAgentWithWindow");
        // create the jade profile
        ProfileImpl profMain = new ProfileImpl(prop);
        // launch the main jade container
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }
}
