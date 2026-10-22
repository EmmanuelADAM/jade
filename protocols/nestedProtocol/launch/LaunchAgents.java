package protocols.nestedProtocol.launch;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;

import java.util.Properties;

/**
 * a buyer ; a seller (4.5 €) and a reseller (margin 1 €) on the market ; a wholesaler (3 €) known only by the reseller
 */
public class LaunchAgents {
    public static void main(String[] args) {
        Properties prop = new ExtendedProperties();
        prop.setProperty(Profile.GUI, "true");
        prop.setProperty(Profile.AGENTS, "buyer:protocols.reseller.agents.BuyerAgent;"
                + "seller:protocols.reseller.agents.SellerAgent(4.5,market);"
                + "reseller:protocols.reseller.agents.ResellerAgent(1);"
                + "wholesaler:protocols.reseller.agents.SellerAgent(3,wholesale)");
        Runtime.instance().createMainContainer(new ProfileImpl(prop));
    }
}
