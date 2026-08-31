package monitoring.launch;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;

import java.util.Properties;

/**
 * Launches a modern Sniffer together with the small instrumented ping-pong
 * demo ({@code monitoring.agents.DemoPingPongAgent}), so the sequence
 * diagram and message table show real traffic right after startup, with
 * nothing left to configure.
 *
 * @author emmanuel adam
 * @version 1
 */
public class LaunchModernMonitoringDemo {

    public static void main(String[] args) {
        Properties prop = new ExtendedProperties();
        prop.setProperty(Profile.GUI, "true");
        prop.setProperty(Profile.AGENTS,
                "sniffer:monitoring.agents.ModernSnifferAgent;"
                        + "ping:monitoring.agents.DemoPingPongAgent;"
                        + "pong:monitoring.agents.DemoPingPongAgent");
        ProfileImpl profMain = new ProfileImpl(prop);
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }
}
