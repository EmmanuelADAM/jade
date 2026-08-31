package monitoring.ui;

import javax.swing.*;
import java.awt.*;

/**
 * A card-like {@link JPanel}: rounded corners, a subtle border and the
 * theme's surface color, used to group controls in the monitoring windows.
 *
 * @author emmanuel adam
 * @version 1
 */
public class RoundedPanel extends JPanel {

    private static final int ARC = 16;

    public RoundedPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(ModernTheme.surface());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
        g2.setColor(ModernTheme.border());
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
        g2.dispose();
        super.paintComponent(g);
    }
}
