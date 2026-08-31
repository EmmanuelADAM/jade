package monitoring.gui;

import jade.core.AID;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLCodec;
import jade.lang.acl.ACLMessage;
import monitoring.io.MessageLogFile;
import monitoring.ui.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern, drop-in replacement window for the historical
 * {@code jade.tools.DummyAgent.DummyAgentGui}: a full ACL message composer
 * (every FIPA-ACL field {@code AclGui} exposes, except the raw envelope
 * tab and reply-by date) with reply/edit/delete on the history, and
 * save/open to a text file for both a single message and the whole queue.
 *
 * @author emmanuel adam
 * @version 1
 */
public class ModernDummyAgentGui extends JFrame {

    public static final int SEND = 1;
    public static final int CLEAR = 2;
    public static final int QUIT = -1;
    public static final int TOGGLE_THEME = 3;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final GuiAgent myAgent;
    private final List<ACLMessage> messages = new ArrayList<>();
    private File currentDir;

    private JComboBox<String> performativeBox;
    private JTextField receiversField;
    private JTextField replyToField;
    private JTextField languageField;
    private JTextField encodingField;
    private JTextField ontologyField;
    private JTextField protocolField;
    private JTextField conversationIdField;
    private JTextField inReplyToField;
    private JTextField replyWithField;
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
        setSize(980, 680);
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
        split.setResizeWeight(0.45);
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
        c.insets = new Insets(3, 4, 3, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        performativeBox = new JComboBox<>(ACLMessage.getAllPerformativeNames());
        performativeBox.setSelectedItem("REQUEST");
        receiversField = new JTextField();
        receiversField.putClientProperty("JTextField.placeholderText", "comma-separated local names");
        replyToField = new JTextField();
        replyToField.putClientProperty("JTextField.placeholderText", "optional, comma-separated");
        languageField = new JTextField();
        encodingField = new JTextField();
        ontologyField = new JTextField();
        protocolField = new JTextField();
        conversationIdField = new JTextField();
        inReplyToField = new JTextField();
        replyWithField = new JTextField();

        int row = 0;
        addFieldRow(fields, c, row++, "Performative", performativeBox, "Receivers", receiversField);
        addFieldRow(fields, c, row++, "Reply-to", replyToField, "Conversation-id", conversationIdField);
        addFieldRow(fields, c, row++, "Language", languageField, "Ontology", ontologyField);
        addFieldRow(fields, c, row++, "Protocol", protocolField, "Encoding", encodingField);
        addFieldRow(fields, c, row, "In-reply-to", inReplyToField, "Reply-with", replyWithField);

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
        ModernButton reset = new ModernButton("Reset", ModernButton.Variant.GHOST);
        reset.addActionListener(e -> resetComposer());
        ModernButton open = new ModernButton("Open...", ModernButton.Variant.GHOST);
        open.addActionListener(e -> openComposedMessage());
        ModernButton save = new ModernButton("Save...", ModernButton.Variant.GHOST);
        save.addActionListener(e -> saveComposedMessage());
        ModernButton send = new ModernButton("Send message", ModernButton.Variant.PRIMARY);
        send.addActionListener(e -> postEvent(SEND));
        south.add(reset);
        south.add(open);
        south.add(save);
        south.add(send);
        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }

    private void addFieldRow(JPanel fields, GridBagConstraints c, int row,
                              String label1, JComponent field1, String label2, JComponent field2) {
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0;
        fields.add(label(label1), c);
        c.gridx = 1;
        c.weightx = 1;
        fields.add(field1, c);
        c.gridx = 2;
        c.weightx = 0;
        fields.add(label(label2), c);
        c.gridx = 3;
        c.weightx = 1;
        fields.add(field2, c);
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
        ModernButton reply = new ModernButton("Reply", ModernButton.Variant.GHOST);
        reply.addActionListener(e -> replySelected());
        ModernButton edit = new ModernButton("Edit", ModernButton.Variant.GHOST);
        edit.addActionListener(e -> editSelected());
        ModernButton delete = new ModernButton("Delete", ModernButton.Variant.GHOST);
        delete.addActionListener(e -> deleteSelected());
        ModernButton openQueue = new ModernButton("Open queue...", ModernButton.Variant.GHOST);
        openQueue.addActionListener(e -> openQueue());
        ModernButton saveQueue = new ModernButton("Save queue...", ModernButton.Variant.GHOST);
        saveQueue.addActionListener(e -> saveQueue());
        ModernButton clear = new ModernButton("Clear history", ModernButton.Variant.GHOST);
        clear.addActionListener(e -> postEvent(CLEAR));
        south.add(reply);
        south.add(edit);
        south.add(delete);
        south.add(openQueue);
        south.add(saveQueue);
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
        msg.getAllReceiver().forEachRemaining(r -> sb.append(r.getLocalName()).append(' '));
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

    private int selectedRow() {
        return table.getSelectedRow();
    }

    /** Loads a reply to the selected message (receiver, conversation, in-reply-to already set) into the composer. */
    private void replySelected() {
        int row = selectedRow();
        if (row < 0) return;
        loadIntoComposer(messages.get(row).createReply());
    }

    /** Reloads the selected message as-is into the composer, so it can be edited and resent. */
    private void editSelected() {
        int row = selectedRow();
        if (row < 0) return;
        loadIntoComposer((ACLMessage) messages.get(row).clone());
    }

    private void deleteSelected() {
        int row = selectedRow();
        if (row < 0) return;
        messages.remove(row);
        tableModel.removeRow(row);
        detail.clear();
    }

    private void resetComposer() {
        performativeBox.setSelectedItem("REQUEST");
        receiversField.setText("");
        replyToField.setText("");
        languageField.setText("");
        encodingField.setText("");
        ontologyField.setText("");
        protocolField.setText("");
        conversationIdField.setText("");
        inReplyToField.setText("");
        replyWithField.setText("");
        contentArea.setText("");
    }

    private void loadIntoComposer(ACLMessage msg) {
        performativeBox.setSelectedItem(ACLMessage.getPerformative(msg.getPerformative()));
        receiversField.setText(joinLocalNames(msg.getAllReceiver()));
        replyToField.setText(joinLocalNames(msg.getAllReplyTo()));
        languageField.setText(nullToEmpty(msg.getLanguage()));
        encodingField.setText(nullToEmpty(msg.getEncoding()));
        ontologyField.setText(nullToEmpty(msg.getOntology()));
        protocolField.setText(nullToEmpty(msg.getProtocol()));
        conversationIdField.setText(nullToEmpty(msg.getConversationId()));
        inReplyToField.setText(nullToEmpty(msg.getInReplyTo()));
        replyWithField.setText(nullToEmpty(msg.getReplyWith()));
        contentArea.setText(nullToEmpty(msg.getContent()));
    }

    private static String joinLocalNames(java.util.Iterator<AID> ids) {
        StringBuilder sb = new StringBuilder();
        ids.forEachRemaining(a -> sb.append(a.getLocalName()).append(", "));
        return sb.length() == 0 ? "" : sb.substring(0, sb.length() - 2);
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    /** Builds the ACLMessage currently described by the composer fields; sender is this agent. */
    public ACLMessage getComposedMessage() {
        ACLMessage msg = new ACLMessage(ACLMessage.getInteger((String) performativeBox.getSelectedItem()));
        msg.setSender(myAgent.getAID());
        for (String name : splitNames(receiversField.getText())) {
            msg.addReceiver(new AID(name, AID.ISLOCALNAME));
        }
        for (String name : splitNames(replyToField.getText())) {
            msg.addReplyTo(new AID(name, AID.ISLOCALNAME));
        }
        setIfNotBlank(languageField.getText(), msg::setLanguage);
        setIfNotBlank(encodingField.getText(), msg::setEncoding);
        setIfNotBlank(ontologyField.getText(), msg::setOntology);
        setIfNotBlank(protocolField.getText(), msg::setProtocol);
        setIfNotBlank(conversationIdField.getText(), msg::setConversationId);
        setIfNotBlank(inReplyToField.getText(), msg::setInReplyTo);
        setIfNotBlank(replyWithField.getText(), msg::setReplyWith);
        msg.setContent(contentArea.getText());
        return msg;
    }

    private static List<String> splitNames(String csv) {
        List<String> names = new ArrayList<>();
        for (String part : csv.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) names.add(trimmed);
        }
        return names;
    }

    private static void setIfNotBlank(String value, java.util.function.Consumer<String> setter) {
        if (value != null && !value.isBlank()) setter.accept(value.trim());
    }

    private void saveComposedMessage() {
        JFileChooser chooser = new JFileChooser();
        if (currentDir != null) chooser.setCurrentDirectory(currentDir);
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        currentDir = chooser.getCurrentDirectory();
        try {
            MessageLogFile.writeMessage(getComposedMessage(), chooser.getSelectedFile());
        } catch (IOException e) {
            showError("Could not save the message: " + e.getMessage());
        }
    }

    private void openComposedMessage() {
        JFileChooser chooser = new JFileChooser();
        if (currentDir != null) chooser.setCurrentDirectory(currentDir);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        currentDir = chooser.getCurrentDirectory();
        try {
            loadIntoComposer(MessageLogFile.readMessage(chooser.getSelectedFile()));
        } catch (IOException | ACLCodec.CodecException e) {
            showError("Could not read the message: " + e.getMessage());
        }
    }

    private void saveQueue() {
        JFileChooser chooser = new JFileChooser();
        if (currentDir != null) chooser.setCurrentDirectory(currentDir);
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        currentDir = chooser.getCurrentDirectory();
        List<MessageLogFile.Entry> entries = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            String direction = String.valueOf(tableModel.getValueAt(i, 1));
            entries.add(new MessageLogFile.Entry(direction, getLocalName(), LocalDateTime.now(), messages.get(i)));
        }
        try {
            MessageLogFile.writeLog(entries, chooser.getSelectedFile());
        } catch (IOException e) {
            showError("Could not save the queue: " + e.getMessage());
        }
    }

    private void openQueue() {
        JFileChooser chooser = new JFileChooser();
        if (currentDir != null) chooser.setCurrentDirectory(currentDir);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        currentDir = chooser.getCurrentDirectory();
        try {
            List<MessageLogFile.Entry> entries = MessageLogFile.readLog(chooser.getSelectedFile());
            clearHistory();
            for (MessageLogFile.Entry entry : entries) {
                addMessage(entry.direction(), entry.message());
            }
        } catch (IOException | ACLCodec.CodecException e) {
            showError("Could not read the queue: " + e.getMessage());
        }
    }

    private String getLocalName() {
        return myAgent == null ? "?" : myAgent.getLocalName();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, getTitle(), JOptionPane.ERROR_MESSAGE);
    }

    private void postEvent(int code) {
        if (myAgent != null) {
            myAgent.postGuiEvent(new GuiEvent(this, code));
        }
    }
}
