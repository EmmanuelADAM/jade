package monitoring.gui;

import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import monitoring.MessageBus;
import monitoring.ui.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern, drop-in replacement window for the historical
 * {@code jade.tools.sniffer.Sniffer} GUI: a live table of exchanged
 * messages plus a sequence-diagram view, filterable by agent name and
 * performative, fed by {@link MessageBus}.
 *
 * @author emmanuel adam
 * @version 1
 */
public class ModernSnifferGui extends JFrame {

    public static final int CLEAR = 1;
    public static final int TOGGLE_THEME = 2;
    public static final int WATCH = 3;
    public static final int UNWATCH = 4;
    public static final int SAVE_LOG = 5;
    public static final int QUIT = -1;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final GuiAgent myAgent;
    private final List<MessageBus.Trace> allTraces = new ArrayList<>();

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField filterField;
    private JComboBox<String> performativeFilter;
    private JToggleButton pauseButton;
    private SequenceCanvas canvas;
    private JLabel titleLabel;
    private JTabbedPane tabs;
    private JTextField watchField;
    private JPanel watchedChips;

    public ModernSnifferGui(GuiAgent agent) {
        super(agent == null ? "Modern Sniffer" : "Modern Sniffer");
        this.myAgent = agent;
        ModernTheme.apply();
        buildGui();
        ModernTheme.onThemeChange(this::refreshTheme);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1050, 650);
        setLocationByPlatform(true);
        setVisible(true);
    }

    private void refreshTheme() {
        getContentPane().setBackground(ModernTheme.background());
        titleLabel.setForeground(ModernTheme.text());
        tabs.setFont(ModernTheme.FONT_BASE);
        table.setSelectionBackground(ModernTheme.withAlpha(ModernTheme.PRIMARY, 60));
        repaint();
    }

    private void buildGui() {
        getContentPane().setBackground(ModernTheme.background());
        getContentPane().setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(buildHeader());
        north.add(Box.createVerticalStrut(6));
        north.add(buildWatchBar());
        getContentPane().add(north, BorderLayout.NORTH);

        tabs = new JTabbedPane();
        tabs.setFont(ModernTheme.FONT_BASE);
        tabs.addTab("Message table", buildTable());
        canvas = new SequenceCanvas();
        JScrollPane canvasScroll = new JScrollPane(canvas);
        ModernScrollBarUI.install(canvasScroll);
        tabs.addTab("Sequence diagram", canvasScroll);
        getContentPane().add(tabs, BorderLayout.CENTER);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);

        titleLabel = new JLabel("Modern Sniffer");
        titleLabel.setFont(ModernTheme.FONT_TITLE);
        titleLabel.setForeground(ModernTheme.text());
        header.add(titleLabel, BorderLayout.WEST);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setOpaque(false);
        filterField = new JTextField(16);
        filterField.putClientProperty("JTextField.placeholderText", "filter by agent name...");
        filterField.getDocument().addDocumentListener((SimpleDocListener) e -> refresh());
        performativeFilter = new JComboBox<>(new String[]{
                "ALL", "REQUEST", "INFORM", "CFP", "PROPOSE", "ACCEPT_PROPOSAL", "REJECT_PROPOSAL",
                "AGREE", "REFUSE", "FAILURE", "QUERY_IF", "QUERY_REF", "SUBSCRIBE", "NOT_UNDERSTOOD"
        });
        performativeFilter.addActionListener(e -> refresh());
        filters.add(new JLabel("Agent:"));
        filters.add(filterField);
        filters.add(new JLabel("Performative:"));
        filters.add(performativeFilter);
        header.add(filters, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        pauseButton = new JToggleButton("Pause");
        pauseButton.setFont(ModernTheme.FONT_SMALL);
        ModernButton theme = new ModernButton("Dark / Light", ModernButton.Variant.GHOST);
        theme.addActionListener(e -> postEvent(TOGGLE_THEME));
        ModernButton clear = new ModernButton("Clear", ModernButton.Variant.GHOST);
        clear.addActionListener(e -> postEvent(CLEAR));
        ModernButton saveLog = new ModernButton("Save log...", ModernButton.Variant.GHOST);
        saveLog.addActionListener(e -> postEvent(SAVE_LOG));
        ModernButton quit = new ModernButton("Quit", ModernButton.Variant.DANGER);
        quit.addActionListener(e -> postEvent(QUIT));
        actions.add(pauseButton);
        actions.add(theme);
        actions.add(clear);
        actions.add(saveLog);
        actions.add(quit);
        header.add(actions, BorderLayout.EAST);

        return header;
    }

    private JComponent buildWatchBar() {
        RoundedPanel bar = new RoundedPanel(new BorderLayout(8, 4));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JLabel label = new JLabel("Watch an agent (zero instrumentation - no code change needed):");
        label.setForeground(ModernTheme.muted());
        label.setFont(ModernTheme.FONT_SMALL);
        watchField = new JTextField(14);
        watchField.putClientProperty("JTextField.placeholderText", "agent local name...");
        Runnable submit = () -> {
            String name = watchField.getText().trim();
            if (!name.isEmpty()) {
                postWatch(WATCH, name);
                watchField.setText("");
            }
        };
        watchField.addActionListener(e -> submit.run());
        ModernButton watchButton = new ModernButton("Watch", ModernButton.Variant.PRIMARY);
        watchButton.addActionListener(e -> submit.run());
        left.add(label);
        left.add(watchField);
        left.add(watchButton);
        bar.add(left, BorderLayout.WEST);

        watchedChips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        watchedChips.setOpaque(false);
        bar.add(watchedChips, BorderLayout.CENTER);

        return bar;
    }

    /** Adds a chip for a newly watched agent; click the chip to stop watching it. */
    public void addWatchedAgent(String name) {
        ModernButton chip = new ModernButton(name + "  ×", ModernButton.Variant.SUCCESS);
        chip.setToolTipText("Click to stop watching " + name);
        chip.addActionListener(e -> {
            watchedChips.remove(chip);
            watchedChips.revalidate();
            watchedChips.repaint();
            postWatch(UNWATCH, name);
        });
        watchedChips.add(chip);
        watchedChips.revalidate();
        watchedChips.repaint();
    }

    private void postWatch(int code, String name) {
        if (myAgent != null) {
            GuiEvent ev = new GuiEvent(this, code);
            ev.addParameter(name);
            myAgent.postGuiEvent(ev);
        }
    }

    private JComponent buildTable() {
        RoundedPanel panel = new RoundedPanel(new BorderLayout());
        tableModel = new DefaultTableModel(
                new Object[]{"Time", "Direction", "Agent", "Peer", "Performative", "Content"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFont(ModernTheme.FONT_BASE);
        table.setRowHeight(26);
        table.setShowGrid(false);
        table.setSelectionBackground(ModernTheme.withAlpha(ModernTheme.PRIMARY, 60));
        table.getColumnModel().getColumn(1).setCellRenderer(directionRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(performativeRenderer());

        JScrollPane scroll = new JScrollPane(table);
        ModernScrollBarUI.install(scroll);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private DefaultTableCellRenderer directionRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                             boolean hasFocus, int row, int column) {
                String text = String.valueOf(value);
                Color color = "SENT".equals(text) ? ModernTheme.PRIMARY : ModernTheme.SUCCESS;
                return pill(t, text, color);
            }
        };
    }

    private DefaultTableCellRenderer performativeRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                             boolean hasFocus, int row, int column) {
                String text = String.valueOf(value);
                Color color = PerformativeColors.of(ACLMessage.getInteger(text));
                return pill(t, text, color);
            }
        };
    }

    private JComponent pill(JTable t, String text, Color color) {
        StatusPill pill = new StatusPill(text, color);
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrapper.setOpaque(true);
        wrapper.setBackground(t.getBackground());
        wrapper.add(pill);
        return wrapper;
    }

    /** Adds one recorded trace and refreshes the view (must be called on the EDT). */
    public void addTrace(MessageBus.Trace trace) {
        allTraces.add(trace);
        if (!pauseButton.isSelected()) {
            refresh();
        }
    }

    public void clearAll() {
        allTraces.clear();
        refresh();
    }

    /** Exposes the currently recorded traces, e.g. to save them to a file. */
    public List<MessageBus.Trace> traces() {
        return List.copyOf(allTraces);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Modern Sniffer", JOptionPane.ERROR_MESSAGE);
    }

    private void refresh() {
        tableModel.setRowCount(0);
        String agentFilter = filterField.getText().trim().toLowerCase();
        String perfFilter = (String) performativeFilter.getSelectedItem();

        List<MessageBus.Trace> filtered = new ArrayList<>();
        for (MessageBus.Trace t : allTraces) {
            ACLMessage msg = t.message();
            String perfName = PerformativeColors.name(msg);
            if (perfFilter != null && !"ALL".equals(perfFilter) && !perfFilter.equals(perfName)) {
                continue;
            }
            if (!agentFilter.isEmpty() && !t.owner().toLowerCase().contains(agentFilter)
                    && !peerOf(t).toLowerCase().contains(agentFilter)) {
                continue;
            }
            filtered.add(t);
            String preview = msg.getContent() == null ? "" : msg.getContent();
            if (preview.length() > 60) preview = preview.substring(0, 60) + "...";
            tableModel.addRow(new Object[]{
                    t.time().format(TIME_FORMAT), t.direction().name(), t.owner(), peerOf(t), perfName, preview
            });
        }
        canvas.setTraces(filtered);
    }

    private static String peerOf(MessageBus.Trace t) {
        ACLMessage msg = t.message();
        if (t.direction() == MessageBus.Direction.SENT) {
            return msg.getAllReceiver().hasNext() ? msg.getAllReceiver().next().getLocalName() : "?";
        }
        return msg.getSender() != null ? msg.getSender().getLocalName() : "?";
    }

    private void postEvent(int code) {
        if (myAgent != null) {
            myAgent.postGuiEvent(new GuiEvent(this, code));
        }
    }

    @FunctionalInterface
    private interface SimpleDocListener extends DocumentListener {
        void update(DocumentEvent e);

        @Override
        default void insertUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        default void removeUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        default void changedUpdate(DocumentEvent e) {
            update(e);
        }
    }
}
