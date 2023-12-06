package handsOn.circularEconomy.launch;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * launch the simulation of travelers and travel agencies
 *
 * @author emmanueladam
 */
public class LaunchSimu {

    public static final Logger logger = Logger.getLogger("simu");

    /**
     *
     */
    public static void main(String... args) {

        logger.setLevel(Level.ALL);
        Handler fh;
        try {
            fh = new FileHandler("./simuCircularEconomy.xml", false);
            logger.addHandler(fh);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }

        // ******************JADE******************

        // allow to send arguments to the JADE launcher
        var pp = new ExtendedProperties();
        // add the gui
        pp.setProperty(Profile.GUI, "true");
        // add the Topic Management Service
        pp.setProperty(Profile.SERVICES, "jade.core.messaging.TopicManagementService;jade.core.event.NotificationService");

        var agents = new StringBuilder();
        for(int i=0; i<2; i++) {
            agents.append("user~").append(i).append(":handsOn.circularEconomy.agents.UserAgent;");
        }
        for(int i=0; i<5; i++) {
            agents.append("repairCoffee~").append(i).append(":handsOn.circularEconomy.agents.RepairCoffeeAgent;");
        }
        for(int i=0; i<4; i++) {
            agents.append("partStore~").append(i).append(":handsOn.circularEconomy.agents.PartStoreAgent;");
        }
        pp.setProperty(Profile.AGENTS, agents.toString());
        // create a default Profile
        var pMain = new ProfileImpl(pp);

        // launch the main jade container
        Runtime.instance().createMainContainer(pMain);

    }

}
