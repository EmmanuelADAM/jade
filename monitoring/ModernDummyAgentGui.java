package monitoring;

import jade.core.AID;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLCodec;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.StringACLCodec;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Modern, single-file replacement for the historical
 * {@code jade.tools.DummyAgent.DummyAgentGui}: a full ACL message composer
 * (every field the original {@code AclGui} exposes, except the raw
 * envelope tab and the reply-by date), a live sent/received history with
 * Reply, Edit (set as current) and Delete, and Open/Save for both a
 * single message and the whole history - exactly what the original tool
 * offers, styled with flat colors instead of default Swing.
 *
 * @author emmanuel adam
 * @version 1
 */
public class ModernDummyAgentGui extends JFrame {

    public static final int SEND = 1;
    public static final int CLEAR = 2;
    public static final int QUIT = -1;

    private static final Color BG = new Color(0xF3, 0xF4, 0xF8);
    private static final Color SURFACE = Color.WHITE;
    private static final Color SURFACE_ALT = new Color(0xF7, 0xF8, 0xFB);
    private static final Color BORDER = new Color(0xDD, 0xE1, 0xEA);
    private static final Color TEXT = new Color(0x1F, 0x24, 0x33);
    private static final Color MUTED = new Color(0x6B, 0x72, 0x80);
    private static final Color PRIMARY = new Color(0x4F, 0x6B, 0xFF);
    private static final Color SUCCESS = new Color(0x2E, 0xB6, 0x72);
    private static final Color DANGER = new Color(0xE0, 0x50, 0x5E);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_BASE = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_MONO = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final GuiAgent myAgent;
    private final List<ACLMessage> messages = new ArrayList<>();
    private File currentDir;

    private JComboBox<String> performativeBox;
    private JTextField receiversField, replyToField, languageField, encodingField,
            ontologyField, protocolField, conversationIdField, inReplyToField, replyWithField;
    private JTextArea contentArea;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea detailArea;

    public ModernDummyAgentGui(GuiAgent agent) {
        super(agent == null ? "Modern Dummy Agent" : agent.getLocalName());
        this.myAgent = agent;
        getContentPane().setBackground(BG);
        getContentPane().setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        getContentPane().add(header(), BorderLayout.NORTH);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, composer(), history());
        split.setResizeWeight(0.45);
        split.setBorder(null);
        getContentPane().add(split, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(980, 680);
        setLocationByPlatform(true);
        setVisible(true);
    }

    private JComponent header() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel title = new JLabel(getTitle());
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT);
        p.add(title, BorderLayout.WEST);
        JButton quit = button("Quit", DANGER);
        quit.addActionListener(e -> post(QUIT));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(quit);
        p.add(actions, BorderLayout.EAST);
        return p;
    }

    private JComponent composer() {
        JPanel card = card(new BorderLayout(8, 8));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        performativeBox = new JComboBox<>(ACLMessage.getAllPerformativeNames());
        performativeBox.setSelectedItem("REQUEST");
        receiversField = new JTextField();
        replyToField = new JTextField();
        languageField = new JTextField();
        encodingField = new JTextField();
        ontologyField = new JTextField();
        protocolField = new JTextField();
        conversationIdField = new JTextField();
        inReplyToField = new JTextField();
        replyWithField = new JTextField();

        int row = 0;
        row(fields, c, row++, "Performative", performativeBox, "Receivers (comma-separated)", receiversField);
        row(fields, c, row++, "Reply-to", replyToField, "Conversation-id", conversationIdField);
        row(fields, c, row++, "Language", languageField, "Ontology", ontologyField);
        row(fields, c, row++, "Protocol", protocolField, "Encoding", encodingField);
        row(fields, c, row, "In-reply-to", inReplyToField, "Reply-with", replyWithField);
        card.add(fields, BorderLayout.NORTH);

        contentArea = new JTextArea(4, 20);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setFont(FONT_MONO);
        contentArea.setBackground(SURFACE_ALT);
        contentArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        card.add(new JScrollPane(contentArea), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        south.setOpaque(false);
        JButton reset = button("Reset", MUTED);
        reset.addActionListener(e -> resetComposer());
        JButton open = button("Open...", MUTED);
        open.addActionListener(e -> openMessage());
        JButton save = button("Save...", MUTED);
        save.addActionListener(e -> saveMessage());
        JButton send = button("Send message", PRIMARY);
        send.addActionListener(e -> post(SEND));
        south.add(reset);
        south.add(open);
        south.add(save);
        south.add(send);
        card.add(south, BorderLayout.SOUTH);
        return card;
    }

    private void row(JPanel p, GridBagConstraints c, int r, String l1, JComponent f1, String l2, JComponent f2) {
        c.gridy = r;
        c.gridx = 0;
        c.weightx = 0;
        p.add(label(l1), c);
        c.gridx = 1;
        c.weightx = 1;
        p.add(f1, c);
        c.gridx = 2;
        c.weightx = 0;
        p.add(label(l2), c);
        c.gridx = 3;
        c.weightx = 1;
        p.add(f2, c);
    }

    private JComponent history() {
        JPanel card = card(new BorderLayout(8, 8));

        tableModel = new DefaultTableModel(new Object[]{"Time", "Dir.", "Performative", "Sender", "Receiver", "Content"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFont(FONT_BASE);
        table.setRowHeight(24);
        table.setSelectionBackground(new Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 60));
        table.getColumnModel().getColumn(1).setCellRenderer(pillRenderer(true));
        table.getColumnModel().getColumn(2).setCellRenderer(pillRenderer(false));
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showDetail(table.getSelectedRow());
        });

        detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setFont(FONT_MONO);
        detailArea.setBackground(SURFACE);
        detailArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(table), new JScrollPane(detailArea));
        split.setResizeWeight(0.55);
        split.setBorder(null);
        card.add(split, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        south.setOpaque(false);
        JButton reply = button("Reply", MUTED);
        reply.addActionListener(e -> replySelected());
        JButton edit = button("Edit", MUTED);
        edit.addActionListener(e -> editSelected());
        JButton delete = button("Delete", MUTED);
        delete.addActionListener(e -> deleteSelected());
        JButton openQ = button("Open queue...", MUTED);
        openQ.addActionListener(e -> openQueue());
        JButton saveQ = button("Save queue...", MUTED);
        saveQ.addActionListener(e -> saveQueue());
        JButton clear = button("Clear history", MUTED);
        clear.addActionListener(e -> post(CLEAR));
        south.add(reply);
        south.add(edit);
        south.add(delete);
        south.add(openQ);
        south.add(saveQ);
        south.add(clear);
        card.add(south, BorderLayout.SOUTH);
        return card;
    }

    // ---- small style helpers, inline instead of a separate toolkit -----

    private JPanel card(LayoutManager lm) {
        JPanel p = new JPanel(lm) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SURFACE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        return p;
    }

    private JButton button(String text, Color color) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? color.darker() : (getModel().isRollover() ? color.brighter() : color));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setForeground(Color.WHITE);
        b.setFont(FONT_BASE.deriveFont(Font.BOLD));
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(MUTED);
        l.setFont(FONT_SMALL);
        return l;
    }

    private DefaultTableCellRenderer pillRenderer(boolean direction) {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int col) {
                JLabel l = new JLabel(String.valueOf(v));
                l.setOpaque(true);
                l.setForeground(Color.WHITE);
                l.setHorizontalAlignment(CENTER);
                l.setFont(FONT_SMALL.deriveFont(Font.BOLD));
                l.setBackground(direction
                        ? ("SENT".equals(v) ? PRIMARY : SUCCESS)
                        : ModernSnifferGui.performativeColor(ACLMessage.getInteger(String.valueOf(v))));
                l.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
                return l;
            }
        };
    }

    // ---- business logic --------------------------------------------

    private void showDetail(int row) {
        if (row < 0 || row >= messages.size()) return;
        ACLMessage m = messages.get(row);
        detailArea.setText(
                "From: " + m.getSender() + "\n" +
                        "To: " + receiversOf(m) + "\n" +
                        "Performative: " + ACLMessage.getPerformative(m.getPerformative()) + "\n" +
                        "Conversation-id: " + m.getConversationId() + "\n" +
                        "Ontology: " + m.getOntology() + "\n\n" +
                        "Content:\n" + m.getContent());
    }

    private static String receiversOf(ACLMessage m) {
        StringBuilder sb = new StringBuilder();
        Iterator<AID> it = m.getAllReceiver();
        while (it.hasNext()) sb.append(it.next().getLocalName()).append(' ');
        return sb.toString().trim();
    }

    /** Appends a message to the history (called by the agent, from any thread). */
    public void addMessage(String direction, ACLMessage msg) {
        SwingUtilities.invokeLater(() -> {
            messages.add(msg);
            String preview = msg.getContent() == null ? "" : msg.getContent();
            if (preview.length() > 60) preview = preview.substring(0, 60) + "...";
            tableModel.addRow(new Object[]{
                    LocalTime.now().format(TIME_FORMAT), direction,
                    ACLMessage.getPerformative(msg.getPerformative()),
                    msg.getSender() == null ? "?" : msg.getSender().getLocalName(),
                    receiversOf(msg), preview});
        });
    }

    public void clearHistory() {
        messages.clear();
        tableModel.setRowCount(0);
        detailArea.setText("");
    }

    private void replySelected() {
        int row = table.getSelectedRow();
        if (row >= 0) loadIntoComposer(messages.get(row).createReply());
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row >= 0) loadIntoComposer((ACLMessage) messages.get(row).clone());
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        messages.remove(row);
        tableModel.removeRow(row);
        detailArea.setText("");
    }

    private void resetComposer() {
        performativeBox.setSelectedItem("REQUEST");
        for (JTextField f : List.of(receiversField, replyToField, languageField, encodingField,
                ontologyField, protocolField, conversationIdField, inReplyToField, replyWithField)) {
            f.setText("");
        }
        contentArea.setText("");
    }

    private void loadIntoComposer(ACLMessage m) {
        performativeBox.setSelectedItem(ACLMessage.getPerformative(m.getPerformative()));
        receiversField.setText(joinLocalNames(m.getAllReceiver()));
        replyToField.setText(joinLocalNames(m.getAllReplyTo()));
        languageField.setText(nz(m.getLanguage()));
        encodingField.setText(nz(m.getEncoding()));
        ontologyField.setText(nz(m.getOntology()));
        protocolField.setText(nz(m.getProtocol()));
        conversationIdField.setText(nz(m.getConversationId()));
        inReplyToField.setText(nz(m.getInReplyTo()));
        replyWithField.setText(nz(m.getReplyWith()));
        contentArea.setText(nz(m.getContent()));
    }

    private static String joinLocalNames(Iterator<AID> ids) {
        StringBuilder sb = new StringBuilder();
        while (ids.hasNext()) sb.append(ids.next().getLocalName()).append(", ");
        return sb.isEmpty() ? "" : sb.substring(0, sb.length() - 2);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    /** Builds the ACLMessage currently described by the composer fields; sender is this agent. */
    public ACLMessage getComposedMessage() {
        ACLMessage m = new ACLMessage(ACLMessage.getInteger((String) performativeBox.getSelectedItem()));
        m.setSender(myAgent.getAID());
        for (String n : split(receiversField.getText())) m.addReceiver(new AID(n, AID.ISLOCALNAME));
        for (String n : split(replyToField.getText())) m.addReplyTo(new AID(n, AID.ISLOCALNAME));
        setIf(languageField.getText(), m::setLanguage);
        setIf(encodingField.getText(), m::setEncoding);
        setIf(ontologyField.getText(), m::setOntology);
        setIf(protocolField.getText(), m::setProtocol);
        setIf(conversationIdField.getText(), m::setConversationId);
        setIf(inReplyToField.getText(), m::setInReplyTo);
        setIf(replyWithField.getText(), m::setReplyWith);
        m.setContent(contentArea.getText());
        return m;
    }

    private static List<String> split(String csv) {
        List<String> out = new ArrayList<>();
        for (String p : csv.split(",")) {
            String t = p.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    private static void setIf(String v, java.util.function.Consumer<String> setter) {
        if (v != null && !v.isBlank()) setter.accept(v.trim());
    }

    private void saveMessage() {
        JFileChooser chooser = new JFileChooser();
        if (currentDir != null) chooser.setCurrentDirectory(currentDir);
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        currentDir = chooser.getCurrentDirectory();
        try (Writer w = new FileWriter(chooser.getSelectedFile())) {
            w.write(getComposedMessage().toString());
        } catch (IOException e) {
            error("Could not save the message: " + e.getMessage());
        }
    }

    private void openMessage() {
        JFileChooser chooser = new JFileChooser();
        if (currentDir != null) chooser.setCurrentDirectory(currentDir);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        currentDir = chooser.getCurrentDirectory();
        try (Reader r = new FileReader(chooser.getSelectedFile())) {
            loadIntoComposer(new StringACLCodec(r, null).decode());
        } catch (IOException | ACLCodec.CodecException e) {
            error("Could not read the message: " + e.getMessage());
        }
    }

    private void saveQueue() {
        JFileChooser chooser = new JFileChooser();
        if (currentDir != null) chooser.setCurrentDirectory(currentDir);
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        currentDir = chooser.getCurrentDirectory();
        try (Writer w = new FileWriter(chooser.getSelectedFile())) {
            for (int i = 0; i < messages.size(); i++) {
                w.write("### " + tableModel.getValueAt(i, 1) + "\n");
                w.write(messages.get(i).toString());
                w.write("\n\n");
            }
        } catch (IOException e) {
            error("Could not save the queue: " + e.getMessage());
        }
    }

    private void openQueue() {
        JFileChooser chooser = new JFileChooser();
        if (currentDir != null) chooser.setCurrentDirectory(currentDir);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        currentDir = chooser.getCurrentDirectory();
        try (BufferedReader r = new BufferedReader(new FileReader(chooser.getSelectedFile()))) {
            clearHistory();
            String direction = null;
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                if (line.startsWith("### ")) {
                    flushQueueEntry(direction, body);
                    direction = line.substring(4).trim();
                    body.setLength(0);
                } else {
                    body.append(line).append('\n');
                }
            }
            flushQueueEntry(direction, body);
        } catch (IOException e) {
            error("Could not read the queue: " + e.getMessage());
        }
    }

    private void flushQueueEntry(String direction, StringBuilder body) {
        if (direction == null || body.toString().isBlank()) return;
        try {
            ACLMessage m = new StringACLCodec(new StringReader(body.toString()), null).decode();
            addMessage(direction, m);
        } catch (ACLCodec.CodecException ignored) {
            // skip a malformed entry rather than abort the whole load
        }
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(this, message, getTitle(), JOptionPane.ERROR_MESSAGE);
    }

    private void post(int code) {
        if (myAgent != null) myAgent.postGuiEvent(new GuiEvent(this, code));
    }
}
