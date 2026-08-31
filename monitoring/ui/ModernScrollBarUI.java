package monitoring.ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/**
 * Thin, flat scrollbar (no arrow buttons, rounded thumb) used to replace the
 * default platform scrollbar inside the monitoring windows.
 *
 * @author emmanuel adam
 * @version 1
 */
public class ModernScrollBarUI extends BasicScrollBarUI {

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return zeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return zeroButton();
    }

    private JButton zeroButton() {
        JButton b = new JButton();
        b.setPreferredSize(new Dimension(0, 0));
        return b;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        g.setColor(ModernTheme.surfaceAlt());
        g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if (thumbBounds.isEmpty()) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(ModernTheme.muted());
        int pad = 3;
        g2.fillRoundRect(thumbBounds.x + pad, thumbBounds.y + pad,
                thumbBounds.width - 2 * pad, thumbBounds.height - 2 * pad, 8, 8);
        g2.dispose();
    }

    /** Applies this UI to both scrollbars of {@code pane}. */
    public static void install(JScrollPane pane) {
        pane.setBorder(BorderFactory.createEmptyBorder());
        pane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        pane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        pane.getVerticalScrollBar().setUnitIncrement(16);
    }
}
