package fsm.review.agents;


import jade.core.AgentServicesTools;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;

import java.awt.*;
import java.util.Random;


/**
 * class for a reviewer agent that wait for article to evaluate and send an evaluation
 *
 * @author eadam
 */
public class ReviewerAgent extends AgentWindowed {

    ACLMessage message;
    boolean busy;
    Random random;

    Behaviour bReception;

    /**
     * a reviewer agent declares itself to the yellow pages (DFAgent) and adds a cyclic listening behaviour
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setButtonActivated(true);
        window.setBackgroundTextColor(Color.CYAN);
        random = new Random();
        AgentServicesTools.register(this, "journal", "reviewer");
        var dfd = AgentServicesTools.getAgentDescription(this, "journal", "reviewer");
//        addBehaviour(new BehaviourReviewer());
        addBehaviour(new ReceiverBehaviour(this, -1, null, true, (a,m)->{
            message = m;
            println("---> I received a new content to evaluate: \"" + message.getContent() + "\"");
            println("click to send a random evaluation... ");
            busy = true;
            window.setButtonActivated(true);
        }));
        window.setButtonActivated(false);
    }

    /**
     * A click on the button triggers this function which sends a random mark back for article received
     */
    @Override
    protected void onGuiEvent(GuiEvent arg0) {
        if (message != null) {
            var reply = message.createReply();
            reply.setContent(Integer.toString(random.nextInt(0, 3)));
            println("I send this evaluation %s with the key %s ".formatted(reply.getContent(), reply.getConversationId()));
            println("-".repeat(40));
            send(reply);
            busy = false;
            addBehaviour(new BehaviourReviewer());
            window.setButtonActivated(false);
        }
    }

    /**deregister to all the services when leaving the platform*/
    @Override
    protected void takeDown() {
        AgentServicesTools.deregisterAll(this);
    }

    class BehaviourReviewer extends CyclicBehaviour {
        @Override
        public void action() {
            if (!busy) {
                message = receive();
                if (message != null) {
                    println("j'ai recu ce contenu � reviewer : " + message.getContent());
                    println("cliquez pour envoyer une evaluation... ");
                    busy = true;
                    window.setButtonActivated(true);
                } else block();
            }
        }
    }
}
