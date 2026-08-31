package monitoring.agents;

import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import monitoring.MessageBus;
import monitoring.gui.ModernSnifferGui;
import monitoring.ui.ModernTheme;

/**
 * Modern, drop-in replacement for {@code jade.tools.sniffer.Sniffer}: opens
 * a monitoring window fed by {@link MessageBus}, showing every message
 * reported through {@code monitoring.Monitor} as a live, filterable table
 * and a sequence diagram.
 * <p>
 * Launch it like the original tool, e.g. from a Jade profile:
 * {@code "mySniffer:monitoring.agents.ModernSnifferAgent"}. Any agent that
 * wants to appear in the diagram must report its own messages through
 * {@code monitoring.Monitor.send(...)}/{@code Monitor.received(...)}; see
 * {@code monitoring/README.md} for the one-line instrumentation.
 *
 * @author emmanuel adam
 * @version 1
 */
public class ModernSnifferAgent extends GuiAgent {

    private ModernSnifferGui gui;
    private MessageBus.Listener listener;

    @Override
    protected void setup() {
        gui = new ModernSnifferGui(this);
        for (MessageBus.Trace trace : MessageBus.history()) {
            gui.addTrace(trace);
        }
        listener = gui::addTrace;
        MessageBus.subscribe(listener);
    }

    @Override
    protected void onGuiEvent(GuiEvent ev) {
        switch (ev.getType()) {
            case ModernSnifferGui.CLEAR -> {
                MessageBus.clearHistory();
                gui.clearAll();
            }
            case ModernSnifferGui.TOGGLE_THEME -> ModernTheme.toggleDark();
            case ModernSnifferGui.QUIT -> doDelete();
            default -> { /* nothing else to handle */ }
        }
    }

    @Override
    protected void takeDown() {
        if (listener != null) {
            MessageBus.unsubscribe(listener);
        }
        if (gui != null) {
            gui.dispose();
        }
    }
}
