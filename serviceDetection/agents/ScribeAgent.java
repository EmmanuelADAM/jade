package serviceDetection.agents;

import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.domain.DFSubscriber;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Class for an agent who subscribes to the yellow page to receive all the information about a particular service.
 * It disseminates to all the agents of this service the information it receives.
 *
 * @author eadam
 */
@SuppressWarnings("serial")
public class ScribeAgent extends AgentWindowed {
    /** list of traveller agents*/
    List<AID> travellers;

    /** no of sent message*/
    int i = 0;

    /*** Agent set-up*/
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setBackgroundTextColor(Color.YELLOW);
        detectTravellers();
    }

    /**
     * listen for event about registration/deregistration to the yellow pages about the service traveller-quiet<br>
     * At each new arrival/departure, the list of agents of the group is sent to these agents
     */
    private void detectTravellers() {
        var model = AgentServicesTools.createAgentDescription("traveller", "quiet");
        travellers = new ArrayList<>();

        //Subscribe to the Yellow Pages service to receive an alert in case of movement on the traveller-quiet service
        addBehaviour(new DFSubscriber(this, model) {
            @Override
            public void onRegister(DFAgentDescription dfd) {
                travellers.add(dfd.getName());
                window.println(dfd.getName().getLocalName() + " has just registered to " + model);
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                //transform the list in an array
                msg.addReceivers(travellers.toArray(AID[]::new));
                msg.setContent("group members: " + Arrays.toString(travellers.stream().map(AID::getLocalName).toArray()));
                send(msg);
            }

            @Override
            public void onDeregister(DFAgentDescription dfd) {
                travellers.remove(dfd.getName());
                window.println(dfd.getName().getLocalName() + " has just deregistered from  " + model);
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceivers(travellers.toArray(AID[]::new));
                msg.setContent("group members: " + Arrays.toString(travellers.stream().map(AID::getLocalName).toArray()));
                send(msg);
            }

        });
    }

    /**close the window when leaving*/
    @Override
    public void takeDown() {
        System.err.println("I'm leaving the platform...("+getLocalName()+")");
        window.dispose();
    }


}
