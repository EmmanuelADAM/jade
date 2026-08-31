package monitoring.ui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalTime;

/**
 * A styled, colorized replacement for the plain {@code JTextArea} used as a
 * "console" in every agent GUI of this repository (see for instance
 * {@code helloWorldService.gui.SimpleGui4Agent}). Each line gets a
 * timestamp, an optional colored tag, and its own text color.
 *
 * @author emmanuel adam
 * @version 1
 */
public class ModernLogPanel extends JPanel {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final JTextPane pane = new JTextPane();
    private final JCheckBox autoScroll = new JCheckBox("auto-scroll", true);

    public ModernLogPanel() {
        super(new BorderLayout());
        setOpaque(false);

        pane.setEditable(false);
        pane.setFont(ModernTheme.FONT_MONO);
        pane.setBackground(ModernTheme.surface());
        pane.setForeground(ModernTheme.text());
        pane.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JScrollPane scroll = new JScrollPane(pane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        ModernScrollBarUI.install(scroll);
        add(scroll, BorderLayout.CENTER);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        toolbar.setOpaque(false);
        autoScroll.setOpaque(false);
        autoScroll.setForeground(ModernTheme.muted());
        autoScroll.setFont(ModernTheme.FONT_SMALL);
        ModernButton clear = new ModernButton("Clear", ModernButton.Variant.GHOST);
        clear.addActionListener(e -> clear());
        toolbar.add(autoScroll);
        toolbar.add(clear);
        add(toolbar, BorderLayout.SOUTH);

        ModernTheme.onThemeChange(this::refreshTheme);
    }

    private void refreshTheme() {
        pane.setBackground(ModernTheme.surface());
        pane.setForeground(ModernTheme.text());
        autoScroll.setForeground(ModernTheme.muted());
        repaint();
    }

    public void clear() {
        pane.setText("");
    }

    /** Appends a neutral line (theme's text color, no tag). */
    public void log(String message) {
        log(null, ModernTheme.text(), message);
    }

    /** Appends a line prefixed by a colored tag, e.g. "SENT", "ERROR". */
    public void log(String tag, Color tagColor, String message) {
        StyledDocument doc = pane.getStyledDocument();
        try {
            SimpleAttributeSet time = new SimpleAttributeSet();
            StyleConstants.setForeground(time, ModernTheme.muted());
            doc.insertString(doc.getLength(), LocalTime.now().format(TIME_FORMAT) + "  ", time);

            if (tag != null) {
                SimpleAttributeSet tagStyle = new SimpleAttributeSet();
                StyleConstants.setForeground(tagStyle, tagColor);
                StyleConstants.setBold(tagStyle, true);
                doc.insertString(doc.getLength(), "[" + tag + "] ", tagStyle);
            }

            SimpleAttributeSet body = new SimpleAttributeSet();
            StyleConstants.setForeground(body, ModernTheme.text());
            doc.insertString(doc.getLength(), message + "\n", body);
        } catch (BadLocationException ignored) {
            // never happens: we always insert at doc.getLength()
        }

        if (autoScroll.isSelected()) {
            pane.setCaretPosition(doc.getLength());
        }
    }
}
