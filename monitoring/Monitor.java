package monitoring;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;

/**
 * One-line integration helper: call {@link #send} instead of {@code send()}
 * and {@link #received} right after a {@code receive()}/{@code blockingReceive()}
 * call, and the message shows up live in every open
 * {@code monitoring.agents.ModernSnifferAgent} window.
 * <p>
 * Example, inside any {@code jade.core.Agent}:
 * <pre>
 *     ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
 *     ...
 *     Monitor.send(this, msg);   // was: send(msg);
 * </pre>
 * <pre>
 *     ACLMessage msg = receive();
 *     if (msg != null) Monitor.received(this, msg);
 * </pre>
 *
 * @author emmanuel adam
 * @version 1
 */
public final class Monitor {

    private Monitor() {
    }

    /** Sends {@code msg} through {@code agent} and reports it to the bus. */
    public static void send(Agent agent, ACLMessage msg) {
        agent.send(msg);
        MessageBus.publish(agent.getLocalName(), MessageBus.Direction.SENT, msg);
    }

    /** Reports a message {@code agent} just received, without sending anything. */
    public static void received(Agent agent, ACLMessage msg) {
        if (msg != null) {
            MessageBus.publish(agent.getLocalName(), MessageBus.Direction.RECEIVED, msg);
        }
    }
}
