package monitoring;

import jade.lang.acl.ACLMessage;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-process, publish/subscribe registry of exchanged ACL messages.
 * <p>
 * This is what feeds the "modern sniffer" ({@code monitoring.agents.ModernSnifferAgent}):
 * any agent that wants to be observed just reports its own sends/receives
 * here (see {@link Monitor}), and every subscribed monitoring window is
 * notified on the Swing Event Dispatch Thread.
 * <p>
 * Unlike the historical {@code jade.tools.sniffer.Sniffer}, which taps
 * directly into the platform's message router and therefore can watch any
 * agent - including ones on remote containers - without their cooperation,
 * this bus only sees what is explicitly reported to it and only within the
 * same JVM. In exchange it needs no low-level platform hook, stays a plain
 * ~50 line class, and is trivial to plug into any teaching example of this
 * repository.
 *
 * @author emmanuel adam
 * @version 1
 */
public final class MessageBus {

    /** direction of a recorded exchange, from the reporting agent's point of view */
    public enum Direction {SENT, RECEIVED}

    /** one recorded exchange */
    public record Trace(LocalDateTime time, Direction direction, String owner, ACLMessage message) {
    }

    @FunctionalInterface
    public interface Listener {
        void onTrace(Trace trace);
    }

    private static final int MAX_HISTORY = 5000;
    private static final List<Listener> LISTENERS = new CopyOnWriteArrayList<>();
    private static final List<Trace> HISTORY = new CopyOnWriteArrayList<>();

    private MessageBus() {
    }

    public static void subscribe(Listener listener) {
        LISTENERS.add(listener);
    }

    public static void unsubscribe(Listener listener) {
        LISTENERS.remove(listener);
    }

    public static List<Trace> history() {
        return List.copyOf(HISTORY);
    }

    public static void clearHistory() {
        HISTORY.clear();
    }

    /**
     * Records one exchange and notifies subscribers. Called by {@link Monitor}
     * for agents instrumented in-process, and by {@code monitoring.agents.ModernSnifferAgent}
     * for messages it decoded from a real platform {@code SniffOn} subscription.
     */
    public static void publish(String owner, Direction direction, ACLMessage message) {
        Trace trace = new Trace(LocalDateTime.now(), direction, owner, message);
        HISTORY.add(trace);
        while (HISTORY.size() > MAX_HISTORY) {
            HISTORY.remove(0);
        }
        SwingUtilities.invokeLater(() -> {
            for (Listener listener : LISTENERS) {
                listener.onTrace(trace);
            }
        });
    }
}
