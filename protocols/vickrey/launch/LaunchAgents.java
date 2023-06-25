package protocols.vickrey.launch;

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
        StringBuilder sb = new StringBuilder("office:protocols.vickrey.agents.AuctioneerAgent;");
        for (int i = 0; i < 5; i++)
            sb.append("bidder_").append(i).append(":protocols.vickrey.agents.ParticipantAgent;");
        prop.setProperty(Profile.AGENTS, sb.toString());
        // create the jade profile
        ProfileImpl profMain = new ProfileImpl(prop);
        // launch the main jade container
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }
}
