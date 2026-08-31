package monitoring.gui;

import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import monitoring.ui.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern, drop-in replacement window for the very dated
 * {@code jade.tools.DummyAgent.DummyAgentGui}: a small "ACL message
 * console" that lets a developer compose and send arbitrary ACL messages
 * to any agent, and shows every sent/received message in a live table
 * with a full-detail viewer.
 *
 * @author emmanuel adam
 * @version 1
 */
public class ModernDummyAgentGui extends JFrame {

    public static final int SEND = 1;
    public static final int CLEAR = 2;
    public static final int QUIT = -1;
    public static final int TOGGLE_THEME = 3;

    private static final String[] PERFORMATIVES = {
            "REQUEST", "INFORM", "CFP", "PROPOSE", "ACCEPT_PROPOSAL", "REJECT_PROPOSAL",
            "AGREE", "REFUSE", "FAILURE", "QUERY_IF", "QUERY_REF", "SUBSCRIBE", "NOT_UNDERSTOOD"
    };

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final GuiAgent myAgent;
    private final List<ACLMessage> messages = new ArrayList<>();

    private JComboBox<String> performativeBox;
    private JTextField receiverField;
    private JTextField conversationIdField;
    private JTextArea contentArea;
    private JTable table;
    private DefaultTableModel tableModel;
    private ModernLogPanel detail;
    private JLabel titleLabel;

    public ModernDummyAgentGui(GuiAgent agent) {
        super(agent == null ? "Modern Dummy Agent" : agent.getLocalName());
        this.myAgent = agent;
        ModernTheme.apply();
        buildGui();
        ModernTheme.onThemeChange(this::refreshTheme);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(920, 600);
        setLocationByPlatform(true);
        setVisible(true);
    }

    private void refreshTheme() {
        getContentPane().setBackground(ModernTheme.background());
        titleLabel.setForeground(ModernTheme.text());
        contentArea.setBackground(ModernTheme.surfaceAlt());
        contentArea.setForeground(ModernTheme.text());
        table.setSelectionBackground(ModernTheme.withAlpha(ModernTheme.PRIMARY, 60));
        repaint();
    }

    private void buildGui() {
        getContentPane().setBackground(ModernTheme.background());
        getContentPane().setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        getContentPane().add(buildHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildComposer(), buildHistory());
        split.setResizeWeight(0);
        split.setOpaque(false);
        split.setBorder(null);
        getContentPane().add(split, BorderLayout.CENTER);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        titleLabel = new JLabel(getTitle());
        titleLabel.setFont(ModernTheme.FONT_TITLE);
        titleLabel.setForeground(ModernTheme.text());
        header.add(titleLabel, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        ModernButton theme = new ModernButton("Dark / Light", ModernButton.Variant.GHOST);
        theme.addActionListener(e -> postEvent(TOGGLE_THEME));
        ModernButton quit = new ModernButton("Quit", ModernButton.Variant.DANGER);
        quit.addActionListener(e -> postEvent(QUIT));
        actions.add(theme);
        actions.add(quit);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JComponent buildComposer() {
        RoundedPanel panel = new RoundedPanel(new BorderLayout(8, 8));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        performativeBox = new JComboBox<>(PERFORMATIVES);
        receiverField = new JTextField();
        conversationIdField = new JTextField();

        c.gridx = 0;
        c.gridy = 0;
        fields.add(label("Performative"), c);
        c.gridx = 1;
        c.weightx = 1;
        fields.add(performativeBox, c);
        c.gridx = 2;
        c.weightx = 0;
        fields.add(label("Receiver (local name)"), c);
        c.gridx = 3;
        c.weightx = 1;
        fields.add(receiverField, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        fields.add(label("Conversation id"), c);
        c.gridx = 1;
        c.weightx = 1;
        fields.add(conversationIdField, c);

        panel.add(fields, BorderLayout.NORTH);

        contentArea = new JTextArea(4, 20);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setFont(ModernTheme.FONT_MONO);
        contentArea.setBackground(ModernTheme.surfaceAlt());
        contentArea.setForeground(ModernTheme.text());
        contentArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane contentScroll = new JScrollPane(contentArea);
        ModernScrollBarUI.install(contentScroll);
        panel.add(contentScroll, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        south.setOpaque(false);
        ModernButton send = new ModernButton("Send message", ModernButton.Variant.PRIMARY);
        send.addActionListener(e -> postEvent(SEND));
        south.add(send);
        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(ModernTheme.muted());
        l.setFont(ModernTheme.FONT_SMALL);
        return l;
    }

    private JComponent buildHistory() {
        RoundedPanel panel = new RoundedPanel(new BorderLayout(8, 8));

        tableModel = new DefaultTableModel(
                new Object[]{"Time", "Dir.", "Performative", "Sender", "Receiver", "Content"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFont(ModernTheme.FONT_BASE);
        table.setRowHeight(26);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(ModernTheme.withAlpha(ModernTheme.PRIMARY, 60));
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(70);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(1).setCellRenderer(pillRenderer(true));
        table.getColumnModel().getColumn(2).setCellRenderer(pillRenderer(false));
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showDetail(table.getSelectedRow());
        });

        JScrollPane tableScroll = new JScrollPane(table);
        ModernScrollBarUI.install(tableScroll);

        detail = new ModernLogPanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, detail);
        split.setResizeWeight(0.55);
        split.setOpaque(false);
        split.setBorder(null);
        panel.add(split, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        south.setOpaque(false);
        ModernButton clear = new ModernButton("Clear history", ModernButton.Variant.GHOST);
        clear.addActionListener(e -> postEvent(CLEAR));
        south.add(clear);
        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }

    private DefaultTableCellRenderer pillRenderer(boolean direction) {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                             boolean hasFocus, int row, int column) {
                String text = String.valueOf(value);
                Color color = direction
                        ? ("SENT".equals(text) ? ModernTheme.PRIMARY : ModernTheme.SUCCESS)
                        : PerformativeColors.of(row < messages.size() ? messages.get(row).getPerformative() : -1);
                StatusPill pill = new StatusPill(text, color);
                pill.setOpaque(false);
                JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                wrapper.setOpaque(true);
                wrapper.setBackground(t.getBackground());
                wrapper.add(pill);
                return wrapper;
            }
        };
    }

    private void showDetail(int row) {
        if (row < 0 || row >= messages.size()) return;
        ACLMessage msg = messages.get(row);
        detail.clear();
        detail.log("From", ModernTheme.PRIMARY, String.valueOf(msg.getSender()));
        detail.log("To", ModernTheme.PRIMARY, receiversOf(msg));
        detail.log("Performative", PerformativeColors.of(msg), PerformativeColors.name(msg));
        detail.log("Conversation-id", ModernTheme.muted(), String.valueOf(msg.getConversationId()));
        detail.log("Ontology", ModernTheme.muted(), String.valueOf(msg.getOntology()));
        detail.log("Content", ModernTheme.text(), String.valueOf(msg.getContent()));
    }

    private static String receiversOf(ACLMessage msg) {
        StringBuilder sb = new StringBuilder();
        msg.getAllReceiver().forEachRemaining(r -> sb.append(r).append(' '));
        return sb.toString().trim();
    }

    /** Appends a message to the history table (called by the agent, from any thread). */
    public void addMessage(String direction, ACLMessage msg) {
        SwingUtilities.invokeLater(() -> {
            messages.add(msg);
            String preview = msg.getContent() == null ? "" : msg.getContent();
            if (preview.length() > 60) preview = preview.substring(0, 60) + "...";
            tableModel.addRow(new Object[]{
                    LocalTime.now().format(TIME_FORMAT), direction, PerformativeColors.name(msg),
                    msg.getSender() == null ? "?" : msg.getSender().getLocalName(),
                    receiversOf(msg), preview
            });
        });
    }

    public void clearHistory() {
        messages.clear();
        tableModel.setRowCount(0);
        detail.clear();
    }

    public String getPerformative() {
        return (String) performativeBox.getSelectedItem();
    }

    public String getReceiver() {
        return receiverField.getText().trim();
    }

    public String getConversationId() {
        return conversationIdField.getText().trim();
    }

    public String getContent() {
        return contentArea.getText();
    }

    private void postEvent(int code) {
        if (myAgent != null) {
            myAgent.postGuiEvent(new GuiEvent(this, code));
        }
    }
}
