package window.agents;


import jade.core.behaviours.ReceiverBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;

/**
 * agent bound to a window, which waits for messages and displays them
 *
 * @author eadam
 */
@SuppressWarnings("serial")
public class ReceiverAgentWithWindow extends AgentWindowed {

    /**
     * agent set-up
     * adds a cyclic message waiting behavior
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent I'm ready. ");


        // add a receiver behaviour that wait, without time limit (-1), a message with no particular signature (null)
        // , and continuously (true)
        addBehaviour(new ReceiverBehaviour(this, -1, null, true, (a,msg)->
                    println("received " + msg.getContent() + ", from " + msg.getSender().getLocalName())));
    }


}
