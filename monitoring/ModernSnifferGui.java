package monitoring;

import jade.core.AID;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.Path2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Modern, single-file replacement window for the historical
 * {@code jade.tools.sniffer.Sniffer} GUI: a live, filterable table of
 * exchanged messages plus a sequence-diagram view (the equivalent of the
 * original's {@code MMCanvas}), fed by the real platform {@code SniffOn}
 * mechanism - type an agent's local name and watch it, exactly like the
 * original tool, no code change needed in the watched agent.
 *
 * @author emmanuel adam
 * @version 1
 */
public class ModernSnifferGui extends JFrame {

    public static final int WATCH = 1;
    public static final int UNWATCH = 2;
    public static final int CLEAR = 3;
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
    private static final Color PURPLE = new Color(0x9B, 0x5D, 0xE0);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_BASE = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    /** one recorded exchange */
    public record Trace(LocalDateTime time, String direction, ACLMessage message) {
    }

    private final GuiAgent myAgent;
    private final List<Trace> allTraces = new ArrayList<>();
    private File currentDir;

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField filterField;
    private SequenceCanvas canvas;
    private JTextField watchField;
    private JPanel watchedChips;

    public ModernSnifferGui(GuiAgent agent) {
        super("Modern Sniffer");
        this.myAgent = agent;
        getContentPane().setBackground(BG);
        getContentPane().setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(header());
        north.add(Box.createVerticalStrut(6));
        north.add(watchBar());
        getContentPane().add(north, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(FONT_BASE);
        tabs.addTab("Message table", tablePanel());
        canvas = new SequenceCanvas();
        tabs.addTab("Sequence diagram", new JScrollPane(canvas));
        getContentPane().add(tabs, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1050, 650);
        setLocationByPlatform(true);
        setVisible(true);
    }

    private JComponent header() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        JLabel title = new JLabel("Modern Sniffer");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT);
        p.add(title, BorderLayout.WEST);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setOpaque(false);
        filterField = new JTextField(16);
        filterField.getDocument().addDocumentListener((SimpleDoc) e -> refresh());
        filters.add(new JLabel("Filter by agent:"));
        filters.add(filterField);
        p.add(filters, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton clear = button("Clear", MUTED);
        clear.addActionListener(e -> post(CLEAR, null));
        JButton saveLog = button("Save log...", MUTED);
        saveLog.addActionListener(e -> saveLog());
        JButton quit = button("Quit", DANGER);
        quit.addActionListener(e -> post(QUIT, null));
        actions.add(clear);
        actions.add(saveLog);
        actions.add(quit);
        p.add(actions, BorderLayout.EAST);
        return p;
    }

    private JComponent watchBar() {
        JPanel bar = card(new BorderLayout(8, 4));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JLabel l = new JLabel("Watch an agent (no code change needed):");
        l.setForeground(MUTED);
        l.setFont(FONT_SMALL);
        watchField = new JTextField(14);
        Runnable submit = () -> {
            String name = watchField.getText().trim();
            if (!name.isEmpty()) {
                post(WATCH, name);
                watchField.setText("");
            }
        };
        watchField.addActionListener(e -> submit.run());
        JButton watch = button("Watch", PRIMARY);
        watch.addActionListener(e -> submit.run());
        left.add(l);
        left.add(watchField);
        left.add(watch);
        bar.add(left, BorderLayout.WEST);

        watchedChips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        watchedChips.setOpaque(false);
        bar.add(watchedChips, BorderLayout.CENTER);
        return bar;
    }

    private JComponent tablePanel() {
        JPanel card = card(new BorderLayout());
        tableModel = new DefaultTableModel(new Object[]{"Time", "Direction", "Agent", "Peer", "Performative", "Content"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFont(FONT_BASE);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(1).setCellRenderer(pill(t -> "SENT".equals(t) ? PRIMARY : SUCCESS));
        table.getColumnModel().getColumn(4).setCellRenderer(pill(t -> performativeColor(ACLMessage.getInteger(t))));
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    // ---- inline style helpers (no separate toolkit) --------------------

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
        p.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
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
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private DefaultTableCellRenderer pill(java.util.function.Function<String, Color> colorOf) {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int col) {
                JLabel l = new JLabel(String.valueOf(v));
                l.setOpaque(true);
                l.setForeground(Color.WHITE);
                l.setHorizontalAlignment(CENTER);
                l.setFont(FONT_SMALL.deriveFont(Font.BOLD));
                l.setBackground(colorOf.apply(String.valueOf(v)));
                l.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
                return l;
            }
        };
    }

    static Color performativeColor(int performative) {
        return switch (performative) {
            case ACLMessage.REQUEST, ACLMessage.CFP, ACLMessage.QUERY_IF, ACLMessage.QUERY_REF -> PRIMARY;
            case ACLMessage.INFORM, ACLMessage.CONFIRM, ACLMessage.AGREE, ACLMessage.ACCEPT_PROPOSAL -> SUCCESS;
            case ACLMessage.REFUSE, ACLMessage.FAILURE, ACLMessage.CANCEL, ACLMessage.DISCONFIRM,
                    ACLMessage.REJECT_PROPOSAL, ACLMessage.NOT_UNDERSTOOD -> DANGER;
            case ACLMessage.PROPOSE -> PURPLE;
            default -> MUTED;
        };
    }

    // ---- business logic --------------------------------------------

    /** Adds a chip for a newly watched agent; click the chip to stop watching it. */
    public void addWatchedAgent(String name) {
        JButton chip = button(name + "  ×", SUCCESS);
        chip.addActionListener(e -> {
            watchedChips.remove(chip);
            watchedChips.revalidate();
            watchedChips.repaint();
            post(UNWATCH, name);
        });
        watchedChips.add(chip);
        watchedChips.revalidate();
        watchedChips.repaint();
    }

    /** Adds one recorded trace and refreshes the view (must be called on the EDT). */
    public void addTrace(String direction, ACLMessage msg) {
        allTraces.add(new Trace(LocalDateTime.now(), direction, msg));
        refresh();
    }

    public void clearAll() {
        allTraces.clear();
        refresh();
    }

    public List<Trace> traces() {
        return List.copyOf(allTraces);
    }

    private void refresh() {
        tableModel.setRowCount(0);
        String filter = filterField.getText().trim().toLowerCase();
        List<Trace> filtered = new ArrayList<>();
        for (Trace t : allTraces) {
            String owner = t.direction().equals("SENT") ? localName(t.message().getSender()) : ownerReceiver(t.message());
            String peer = peerOf(t);
            if (!filter.isEmpty() && !owner.toLowerCase().contains(filter) && !peer.toLowerCase().contains(filter)) {
                continue;
            }
            filtered.add(t);
            String preview = t.message().getContent() == null ? "" : t.message().getContent();
            if (preview.length() > 60) preview = preview.substring(0, 60) + "...";
            tableModel.addRow(new Object[]{
                    t.time().format(TIME_FORMAT), t.direction(), owner, peer,
                    ACLMessage.getPerformative(t.message().getPerformative()), preview});
        }
        canvas.setTraces(filtered);
    }

    private static String peerOf(Trace t) {
        ACLMessage m = t.message();
        if ("SENT".equals(t.direction())) {
            return m.getAllReceiver().hasNext() ? m.getAllReceiver().next().getLocalName() : "?";
        }
        return localName(m.getSender());
    }

    private static String localName(AID aid) {
        return aid == null ? "?" : aid.getLocalName();
    }

    private static String ownerReceiver(ACLMessage m) {
        return m.getAllReceiver().hasNext() ? m.getAllReceiver().next().getLocalName() : "?";
    }

    private void saveLog() {
        JFileChooser chooser = new JFileChooser();
        if (currentDir != null) chooser.setCurrentDirectory(currentDir);
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        currentDir = chooser.getCurrentDirectory();
        try (Writer w = new FileWriter(chooser.getSelectedFile())) {
            for (Trace t : allTraces) {
                w.write("### " + t.direction() + "\n");
                w.write(t.message().toString());
                w.write("\n\n");
            }
        } catch (IOException e) {
            showError("Could not save the log: " + e.getMessage());
        }
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Modern Sniffer", JOptionPane.ERROR_MESSAGE);
    }

    private void post(int code, String param) {
        if (myAgent != null) {
            GuiEvent ev = new GuiEvent(this, code);
            if (param != null) ev.addParameter(param);
            myAgent.postGuiEvent(ev);
        }
    }

    @FunctionalInterface
    private interface SimpleDoc extends javax.swing.event.DocumentListener {
        void update(javax.swing.event.DocumentEvent e);

        @Override
        default void insertUpdate(javax.swing.event.DocumentEvent e) {
            update(e);
        }

        @Override
        default void removeUpdate(javax.swing.event.DocumentEvent e) {
            update(e);
        }

        @Override
        default void changedUpdate(javax.swing.event.DocumentEvent e) {
            update(e);
        }
    }

    /** Lightweight sequence-diagram view - the equivalent of the original Sniffer's MMCanvas. */
    private static class SequenceCanvas extends JPanel {
        private List<Trace> traces = List.of();

        SequenceCanvas() {
            setBackground(SURFACE);
        }

        void setTraces(List<Trace> traces) {
            this.traces = traces;
            Map<String, Integer> columns = columnsOf(traces);
            setPreferredSize(new Dimension(90 + Math.max(1, columns.size()) * 170 + 40, 46 + traces.size() * 46 + 40));
            revalidate();
            repaint();
        }

        private static Map<String, Integer> columnsOf(List<Trace> traces) {
            Map<String, Integer> columns = new LinkedHashMap<>();
            for (Trace t : traces) {
                for (String name : participantsOf(t)) columns.putIfAbsent(name, columns.size());
            }
            return columns;
        }

        private static String[] participantsOf(Trace t) {
            AID sender = t.message().getSender();
            AID receiver = t.message().getAllReceiver().hasNext() ? t.message().getAllReceiver().next() : null;
            return new String[]{sender != null ? sender.getLocalName() : "?", receiver != null ? receiver.getLocalName() : "?"};
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(FONT_SMALL);
            Map<String, Integer> columns = columnsOf(traces);
            if (columns.isEmpty()) {
                g2.setColor(MUTED);
                g2.drawString("No message recorded yet.", 20, 30);
                g2.dispose();
                return;
            }
            int bottom = 46 + traces.size() * 46 + 20;
            for (Map.Entry<String, Integer> e : columns.entrySet()) {
                int x = xOf(e.getValue());
                g2.setColor(BORDER);
                g2.drawLine(x, 46, x, bottom);
                badge(g2, e.getKey(), x);
            }
            int row = 0;
            for (Trace t : traces) {
                String[] p = participantsOf(t);
                int fromX = xOf(columns.get(p[0]));
                int toX = xOf(columns.get(p[1]));
                int y = 46 + row * 46 + 23;
                g2.setColor(performativeColor(t.message().getPerformative()));
                if (fromX == toX) selfArrow(g2, fromX, y);
                else arrow(g2, fromX, toX, y);
                String label = ACLMessage.getPerformative(t.message().getPerformative());
                g2.setColor(TEXT);
                g2.drawString(label, Math.min(fromX, toX) + Math.abs(toX - fromX) / 2
                        - g2.getFontMetrics().stringWidth(label) / 2, y - 6);
                row++;
            }
            g2.dispose();
        }

        private int xOf(int col) {
            return 90 + col * 170 + 85;
        }

        private void badge(Graphics2D g2, String name, int x) {
            g2.setFont(FONT_BASE.deriveFont(Font.BOLD));
            FontMetrics fm = g2.getFontMetrics();
            int w = Math.max(60, fm.stringWidth(name) + 20);
            g2.setColor(PRIMARY);
            g2.fillRoundRect(x - w / 2, 8, w, 26, 13, 13);
            g2.setColor(Color.WHITE);
            g2.drawString(name, x - fm.stringWidth(name) / 2, 25);
            g2.setFont(FONT_SMALL);
        }

        private void arrow(Graphics2D g2, int fromX, int toX, int y) {
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(fromX, y, toX, y);
            head(g2, toX, y, toX > fromX);
        }

        private void selfArrow(Graphics2D g2, int x, int y) {
            g2.setStroke(new BasicStroke(2f));
            Path2D path = new Path2D.Double();
            path.moveTo(x, y - 6);
            path.curveTo(x + 40, y - 20, x + 40, y + 20, x, y + 6);
            g2.draw(path);
            head(g2, x, y + 6, false);
        }

        private void head(Graphics2D g2, int x, int y, boolean right) {
            int s = 6, dx = right ? -s : s;
            Path2D h = new Path2D.Double();
            h.moveTo(x, y);
            h.lineTo(x + dx, y - s);
            h.lineTo(x + dx, y + s);
            h.closePath();
            g2.fill(h);
        }
    }
}
