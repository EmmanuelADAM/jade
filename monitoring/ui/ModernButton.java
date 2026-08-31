package monitoring.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Flat, rounded {@link JButton} with a hover/press animation, replacing the
 * boxy default Swing button used across the agent GUIs of this repository.
 *
 * @author emmanuel adam
 * @version 1
 */
public class ModernButton extends JButton {

    /** color variants a button can use */
    public enum Variant {PRIMARY, SUCCESS, DANGER, GHOST}

    private static final int ARC = 14;

    private final Variant variant;
    private boolean hover = false;
    private boolean pressed = false;

    public ModernButton(String text, Variant variant) {
        super(text);
        this.variant = variant;
        setFont(ModernTheme.FONT_BASE.deriveFont(Font.BOLD));
        setForeground(variant == Variant.GHOST ? ModernTheme.text() : Color.WHITE);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
    }

    private Color baseColor() {
        return switch (variant) {
            case PRIMARY -> ModernTheme.PRIMARY;
            case SUCCESS -> ModernTheme.SUCCESS;
            case DANGER -> ModernTheme.DANGER;
            case GHOST -> ModernTheme.surfaceAlt();
        };
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color base = baseColor();
        Color fill = pressed ? base.darker() : (hover ? base.brighter() : base);
        if (!isEnabled()) {
            fill = ModernTheme.muted();
        }
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);

        if (variant == Variant.GHOST) {
            g2.setColor(ModernTheme.border());
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
