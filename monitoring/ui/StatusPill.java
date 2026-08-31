package monitoring.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Small rounded, colored badge used to display a FIPA performative or an
 * agent state ("SENT", "RECEIVED", "INFORM", ...) without resorting to a
 * plain uncolored table cell.
 *
 * @author emmanuel adam
 * @version 1
 */
public class StatusPill extends JLabel {

    private Color color;

    public StatusPill(String text, Color color) {
        super(text);
        this.color = color;
        setFont(ModernTheme.FONT_SMALL.deriveFont(Font.BOLD));
        setForeground(Color.WHITE);
        setHorizontalAlignment(CENTER);
        setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        setOpaque(false);
    }

    public void setColor(Color color) {
        this.color = color;
        repaint();
    }

    public void setPillText(String text) {
        setText(text);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(d.height, 20));
    }
}
