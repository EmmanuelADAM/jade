package monitoring.launch;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;

import java.util.Properties;

/**
 * Launches a modern Dummy Agent alongside a modern Sniffer, so any message
 * composed and sent from the Dummy Agent window immediately shows up in
 * the Sniffer's table and sequence diagram.
 *
 * @author emmanuel adam
 * @version 1
 */
public class LaunchModernDummy {

    public static void main(String[] args) {
        Properties prop = new ExtendedProperties();
        prop.setProperty(Profile.GUI, "true");
        prop.setProperty(Profile.AGENTS,
                "dummy:monitoring.agents.ModernDummyAgent;sniffer:monitoring.agents.ModernSnifferAgent");
        ProfileImpl profMain = new ProfileImpl(prop);
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }
}
