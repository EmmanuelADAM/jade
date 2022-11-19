package protocoles.requetes.agents;


import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Class for an agent that send a request to other agents about a sum of integers to calculate.
 * The other agents can refuse or agree to calculate and then send their result.
 * This agent use an AchieveRE protocol to manage the messages.
 *
 * @author eadam
 */
public class AgentEmissionARE extends AgentWindowed {

    /**
     * agent set-up
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.println("Hello! I'm ready, my address is " + this.getAID().getName());
        window.println("Click to the button to launch requests");
        window.println("-".repeat(40));
        window.setButtonActivated(true);
        window.setBackgroundTextColor(Color.CYAN);
    }

    /**
     * add a AchieveREInitiator protocol to send a request about an addition and wait manage the responses
     */
    private void createRequest(String id, String computation) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setConversationId(id);
        msg.setContent(computation);

        var adresses = AgentServicesTools.searchAgents(this, "calculation", "addition");
        msg.addReceivers(adresses);
        println("Calculator agents found: " + Arrays.stream(adresses).map(AID::getLocalName).toList());

        println("_".repeat(40));
        println("I send a request for " + msg.getContent());
        println("_".repeat(40));
        //AchieveRE protocol, the msg is sent at its activation
        AchieveREInitiator init = new AchieveREInitiator(this, msg) {
            //function triggered when receiving an AGREE message (sender is agree to send a result)
            @Override
            protected void handleAgree(ACLMessage agree) {
                window.println("Received an agree msg from " + agree.getSender().getLocalName());
            }

            //function triggered when receiving an REFUSE message (sender refuses to send a result)
            @Override
            protected void handleRefuse(ACLMessage refuse) {
                window.println("Received a refuse msg from " + refuse.getSender().getLocalName());
            }

            //function triggered when receiving an INFORM message (sender gives its result)
            @Override
            protected void handleInform(ACLMessage inform) {
                window.println("Received from " + inform.getSender().getLocalName() + ", this result: " + inform.getContent());
            }


            //function triggered when all receivers have sent a response (agree&inform or refuse)
            // responses = list of received messages (inform & refuse)
            @Override
            protected void handleAllResultNotifications(List<ACLMessage> responses) {
                println("~".repeat(40));
                StringBuilder sb = new StringBuilder("Ok, I've all the responses. To resume: \n");
                for (ACLMessage msg : responses) {
                    sb.append("\t-from ").append(msg.getSender().getLocalName()).append(" : ").append(msg.getContent()).append("\n");
                }
                println(sb.toString());
                println("(o)".repeat(20));
            }
        };

        addBehaviour(init);
    }


    /**reaction to an event sent by the gui:
     * create a request with the shape sum x,y,z,...
     * and ask to add an achieveReInitior behaviour */
    protected void onGuiEvent(GuiEvent arg0) {
        Random r = new Random();
        int nb = r.nextInt(3, 7);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nb; i++)
            sb.append(r.nextInt(1, 100)).append(",");
        createRequest("123", "sum " + sb);
    }

}
