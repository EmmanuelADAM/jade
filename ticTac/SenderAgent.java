package ticTac;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * class or an agent that send periodically some messages
 *
 * @author emmanueladam
 */
public class SenderAgent extends Agent {
    /**
     * Agent set-up
     */
    @Override
    protected void setup() {

        // creation of a behavior that will send the text 'tictac' every second to the demining agent
        // -- creation of the INFORM message, tagged "CLOCK"
        final var msgTic = new ACLMessage(ACLMessage.INFORM);
        msgTic.setConversationId("CLOCK");
        msgTic.addReceiver("deminer");
        msgTic.setContent("tictac");
        // -- create a ticker behaviour with a period of 1000ms
        TickerBehaviour ticTacBehaviour = new TickerBehaviour(SenderAgent.this, 1000, a->a.send(msgTic));

        // add the ticker behaviour in 5000 ms
        addBehaviour(new WakerBehaviour(this, 5000, a->a.addBehaviour(ticTacBehaviour)));

        // creation of a behavior that will send the text 'b o o o m' in 10 secondes
        // -- creation of the INFORM message, tagged "BOOM"
        final var msgBoom = new ACLMessage(ACLMessage.INFORM);
        msgBoom.setConversationId("BOOM");
        msgBoom.addReceiver("deminer");
        msgBoom.setContent("b o o m ! ! !");
        // create and add a WakerBehaviour (with  delay of 10000ms) that remove the TicTac behavior and send the boom
        // msg
        addBehaviour(new WakerBehaviour(this, 10000, a->{a.removeBehaviour(ticTacBehaviour);a.send(msgBoom);}));
    }
}
