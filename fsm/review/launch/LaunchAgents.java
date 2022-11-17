package fsm.review.launch;

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
        // name the agents
        StringBuilder sb = new StringBuilder("a:fsm.review.agents.AuthorAgent;");
        sb.append("j:fsm.review.agents.JournalAgent;");
        sb.append("ra:fsm.review.agents.ReviewerAgent;");
        sb.append("rb:fsm.review.agents.ReviewerAgent;");
        sb.append("rc:fsm.review.agents.ReviewerAgent;");
        prop.setProperty(Profile.AGENTS, sb.toString());
        // create the jade profile
        ProfileImpl profMain = new ProfileImpl(prop);
        // launch the main jade container
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }
}
