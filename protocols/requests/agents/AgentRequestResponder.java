package protocols.requests.agents;


import jade.core.AgentServicesTools;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

import java.awt.*;
import java.util.Random;


/**
 * class for an agent that waits for a message from the AchieveRE protocol, prepares the response, and returns it
 *
 * @author eadam
 */
@SuppressWarnings("serial")
public class AgentRequestResponder extends AgentWindowed {

    /**
     * agent set-up
     * add an AchieveRE behavior for responder
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());

        //register to the service "calculator-sum"
        AgentServicesTools.register(this, "calculator", "sum");
        Random hasard = new Random();


        //stamp on the request
        MessageTemplate model = MessageTemplate.MatchConversationId("123");

        //add a AchieveRE behavior that wait for a request and reply
        AchieveREResponder init = new AchieveREResponder(this, model) {

            //function triggered by a REQUEST msg :
            // the agents decides to refuse or to agree with the request
            //if it agrees it hase to send an inform message next
            //return the answer to the request
            @Override
            protected ACLMessage handleRequest(ACLMessage request) {
                window.setBackgroundTextColor(Color.WHITE);
                window.println("received  " + request.getContent());
                ACLMessage answer = request.createReply();
                //parfois l'agent choisi de refuser la demande
                if (hasard.nextBoolean()) {
                    answer.setPerformative(ACLMessage.AGREE);
                    window.setBackgroundTextColor(Color.PINK);
                    println("I'm ok to answer...");
                    println("-".repeat(40));
                } else {
                    answer.setPerformative(ACLMessage.REFUSE);
                    window.setBackgroundTextColor(Color.LIGHT_GRAY);
                    println("I refuse the request !");
                    println("(o)".repeat(20));
                }
                return answer;
            }

            //Function used to return a result by a message
            //param : request = initial initial request, response = agreement I just sent
            @Override
            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
                String content = request.getContent();
                String[] strValues = content.split("\\+");
                int sum = 0;
                for (String strV : strValues)
                    sum += Integer.parseInt(strV);

                ACLMessage answer = request.createReply();
                answer.setPerformative(ACLMessage.INFORM);
                answer.setContent("result = " + sum);
                println("I send: " + sum);
                println("(o)".repeat(20));
                return answer;
            }
        };

        addBehaviour(init);
    }

    /**deregister to the service and close the window when leaving*/
    @Override
    public void takeDown() {
        AgentServicesTools.deregisterAll(this);
        window.setVisible(false);
        System.err.println("moi " + this.getLocalName() + ", je quitte la plateforme...");
    }
}
