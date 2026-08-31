package monitoring.ui;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import monitoring.MessageBus;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A lightweight, self-painted sequence-diagram view: one vertical lifeline
 * per agent, one horizontal arrow per exchanged message, ordered
 * chronologically top to bottom. This is the modern equivalent of the
 * canvas drawn by the historical {@code jade.tools.sniffer.Sniffer}
 * (its {@code MMCanvas} class), fed by {@link MessageBus} instead of a
 * platform-level hook.
 *
 * @author emmanuel adam
 * @version 1
 */
public class SequenceCanvas extends JPanel {

    private static final int COLUMN_WIDTH = 170;
    private static final int ROW_HEIGHT = 46;
    private static final int HEADER_HEIGHT = 46;
    private static final int LEFT_MARGIN = 90;

    private List<MessageBus.Trace> traces = List.of();

    public SequenceCanvas() {
        setOpaque(true);
    }

    @Override
    public Color getBackground() {
        // always reflect the current theme instead of a color snapshotted at construction time
        return ModernTheme.surface();
    }

    public void setTraces(List<MessageBus.Trace> traces) {
        this.traces = traces;
        Map<String, Integer> columns = columnsOf(traces);
        int width = LEFT_MARGIN + Math.max(1, columns.size()) * COLUMN_WIDTH + 40;
        int height = HEADER_HEIGHT + traces.size() * ROW_HEIGHT + 40;
        setPreferredSize(new Dimension(width, height));
        revalidate();
        repaint();
    }

    private static Map<String, Integer> columnsOf(List<MessageBus.Trace> traces) {
        Map<String, Integer> columns = new LinkedHashMap<>();
        for (MessageBus.Trace t : traces) {
            for (String name : participantsOf(t)) {
                columns.putIfAbsent(name, columns.size());
            }
        }
        return columns;
    }

    private static String[] participantsOf(MessageBus.Trace t) {
        ACLMessage msg = t.message();
        AID sender = msg.getSender();
        AID receiver = msg.getAllReceiver().hasNext() ? msg.getAllReceiver().next() : null;
        return new String[]{
                sender != null ? sender.getLocalName() : "?",
                receiver != null ? receiver.getLocalName() : "?"
        };
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(ModernTheme.FONT_SMALL);

        Map<String, Integer> columns = columnsOf(traces);
        if (columns.isEmpty()) {
            g2.setColor(ModernTheme.muted());
            g2.drawString("No message recorded yet.", 20, 30);
            g2.dispose();
            return;
        }

        int bottom = HEADER_HEIGHT + traces.size() * ROW_HEIGHT + 20;

        // lifelines + headers
        for (Map.Entry<String, Integer> e : columns.entrySet()) {
            int x = xOf(e.getValue());
            g2.setColor(ModernTheme.border());
            g2.drawLine(x, HEADER_HEIGHT, x, bottom);

            drawAgentBadge(g2, e.getKey(), x);
        }

        int row = 0;
        for (MessageBus.Trace t : traces) {
            String[] p = participantsOf(t);
            int fromX = xOf(columns.get(p[0]));
            int toX = xOf(columns.get(p[1]));
            int y = HEADER_HEIGHT + row * ROW_HEIGHT + ROW_HEIGHT / 2;

            Color color = PerformativeColors.of(t.message());
            g2.setColor(color);

            if (fromX == toX) {
                drawSelfArrow(g2, fromX, y);
            } else {
                drawArrow(g2, fromX, toX, y);
            }

            String label = PerformativeColors.name(t.message());
            int labelX = Math.min(fromX, toX) + Math.abs(toX - fromX) / 2 - g2.getFontMetrics().stringWidth(label) / 2;
            g2.setColor(ModernTheme.text());
            g2.drawString(label, Math.max(4, labelX), y - 6);
            row++;
        }
        g2.dispose();
    }

    private int xOf(int columnIndex) {
        return LEFT_MARGIN + columnIndex * COLUMN_WIDTH + COLUMN_WIDTH / 2;
    }

    private void drawAgentBadge(Graphics2D g2, String name, int x) {
        FontMetrics fm = g2.getFontMetrics(ModernTheme.FONT_BASE.deriveFont(Font.BOLD));
        int w = Math.max(60, fm.stringWidth(name) + 20);
        g2.setFont(ModernTheme.FONT_BASE.deriveFont(Font.BOLD));
        g2.setColor(ModernTheme.PRIMARY);
        g2.fillRoundRect(x - w / 2, 8, w, 26, 13, 13);
        g2.setColor(Color.WHITE);
        g2.drawString(name, x - fm.stringWidth(name) / 2, 25);
        g2.setFont(ModernTheme.FONT_SMALL);
    }

    private void drawArrow(Graphics2D g2, int fromX, int toX, int y) {
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(fromX, y, toX, y);
        drawArrowHead(g2, toX, y, toX > fromX);
    }

    private void drawSelfArrow(Graphics2D g2, int x, int y) {
        g2.setStroke(new BasicStroke(2f));
        Path2D path = new Path2D.Double();
        path.moveTo(x, y - 6);
        path.curveTo(x + 40, y - 20, x + 40, y + 20, x, y + 6);
        g2.draw(path);
        drawArrowHead(g2, x, y + 6, false);
    }

    private void drawArrowHead(Graphics2D g2, int x, int y, boolean pointingRight) {
        int size = 6;
        int dx = pointingRight ? -size : size;
        Path2D head = new Path2D.Double();
        head.moveTo(x, y);
        head.lineTo(x + dx, y - size);
        head.lineTo(x + dx, y + size);
        head.closePath();
        g2.fill(head);
    }
}
