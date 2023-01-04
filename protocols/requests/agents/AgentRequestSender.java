package protocols.requests.agents;


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
 * class of an agent that submits a sum request to other agents and handles the exchange through the AchieveRE
 * protocol
 *
 * @author eadam
 */
public class AgentRequestSender extends AgentWindowed {

    /**
     * agent set-up
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setButtonActivated(true);
        window.setBackgroundTextColor(Color.CYAN);
    }

    /**
     * add an AchieveRE protocol behavior initiator.
     * send a request message and handle the responses
     * @param id stamp for identify the message
     * @param computation order in the form 3+2+4+5...
     */
    private void createRequest(String id, String computation) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setConversationId(id);
        msg.setContent(computation);

        var adresses = AgentServicesTools.searchAgents(this, "calculator", "sum");
        msg.addReceivers(adresses);
        println("Calculator agents found : " + Arrays.stream(adresses).map(AID::getLocalName).toList());

        println("_".repeat(40));
        println("I send a request about:" + msg.getContent());
        println("_".repeat(40));
        //msg sent as soon as the behavior is activated
        AchieveREInitiator init = new AchieveREInitiator(this, msg) {
            //function triggered by a AGREE msg : the sender accept the resquest and will send an INFORM message with
            // its result
            @Override
            protected void handleAgree(ACLMessage agree) {
                window.println("agreement received from " + agree.getSender().getLocalName());
            }

            //function triggered by a REFUSE msg, the sender refuse to participate in the request
            @Override
            protected void handleRefuse(ACLMessage refuse) {
                window.println("refuse received from " + refuse.getSender().getLocalName());
            }

            //function triggered by an INFORM msg, the sender send its result
            @Override
            protected void handleInform(ACLMessage inform) {
                window.println("from " + inform.getSender().getLocalName() +
                        ", I received this result: " + inform.getContent());
            }


            //function triggered when all the responses (INFORM) have been received following the agreements
            //@param responses all the received INFORM msg
            @Override
            protected void handleAllResultNotifications(List<ACLMessage> responses) {
                println("~".repeat(40));
                StringBuilder sb = new StringBuilder("ok! I received all the responses... " +
                        "To resume: \n");
                for (ACLMessage msg : responses) {
                    sb.append("\t-from ").append(msg.getSender().getLocalName()).append(" : ").append(msg.getContent()).append("\n");
                }
                println(sb.toString());
                println("(o)".repeat(20));
            }
        };

        addBehaviour(init);
    }


    /**function triggered when an event is sent by the GUI.
     * create a request in the form 3+23+11+22....
     * a sum of 3 to 7 integers between 1 and 100
     * */
    protected void onGuiEvent(GuiEvent ignoredEvent) {
        Random r = new Random();
        int nb = r.nextInt(3, 7);
        StringBuilder sb = new StringBuilder(String.valueOf(r.nextInt(1, 100)));
        for (int i = 1; i < nb; i++)
            sb.append("+").append(r.nextInt(1, 100));
        createRequest("123",  sb.toString());
    }

}
