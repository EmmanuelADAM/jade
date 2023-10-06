package td.negociation;

import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.ExtendedProperties;

import java.util.Properties;

import static java.lang.System.out;

/**
 * Agent class to allow exchange of messages between an agent named ping, that initiates the 'dialog', and an agent
 * named 'pong'
 *
 * @author emmanueladam
 */
public class Main  {

    public static void main(String[] args) {
        // prepare argument for the JADE container
        Properties prop = new ExtendedProperties();
        // display a control/debug window
        prop.setProperty(Profile.GUI, "true");
        // declare the agents
        prop.setProperty(Profile.AGENTS, "buyer:td.negociation.BuyerAgent;seller:td.negociation.SellerAgent");
        // create the ain container
        ProfileImpl profMain = new ProfileImpl(prop);
        // launch it !
        Runtime rt = Runtime.instance();
        rt.createMainContainer(profMain);
    }
}
