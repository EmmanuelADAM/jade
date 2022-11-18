package serviceDetection.agents;


import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;

/**
 * agent that register to a service in "some" time and display the message received
 *
 * @author eadam
 */
public class IncomingAgent extends AgentWindowed {
    AID topic = null;


    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent  " + getAID().getName() + " is ready. ");

        //regisration to any service
        AgentServicesTools.register(this, "anyTypeOfService", "anyNameOfService");

        //regisration to the service
        addBehaviour(new WakerBehaviour(this, (long)(Math.random()*10000d),
                a->AgentServicesTools.register(a, "traveller", "quiet")));


        //Wait limitless(-1) for all message types(null), cyclically (true)
        addBehaviour(new ReceiverBehaviour(this, -1, null, true,
                (a,msg)-> println("received ftom \"" + msg.getSender().getLocalName() + "\", ceci : \n" + msg.getContent() +
                        "\n" + "-".repeat(20))));
    }



    /**deregister from all services when leaving*/
    @Override
    public void takeDown() {
        AgentServicesTools.deregisterAll(this);
        System.err.println("I'm leaving the platform...("+getLocalName()+")");
        window.dispose();
    }


}
