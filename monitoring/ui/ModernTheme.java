package monitoring.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Central color/font palette and Look&amp;Feel tuning shared by every window
 * of the "monitoring" package (modern Dummy Agent, modern Sniffer, ...).
 * <p>
 * No external UI library is used: this class only tunes the standard
 * javax.swing UIManager defaults (flatter buttons, thinner scrollbars,
 * softer colors) so it stays a plain drop-in for any Jade agent GUI in
 * this repository, in a light or a dark variant.
 *
 * @author emmanuel adam
 * @version 1
 */
public final class ModernTheme {

    /** true once {@link #apply()} has switched the palette to dark mode */
    private static boolean dark = false;

    /**
     * Hooks run after every {@link #apply()}/{@link #toggleDark()}, so a
     * window built with explicit colors (not just UIManager defaults) can
     * re-read the current palette. Register with {@link #onThemeChange}.
     */
    private static final java.util.List<Runnable> refreshHooks = new java.util.concurrent.CopyOnWriteArrayList<>();

    // ---- light palette -----------------------------------------------
    private static final Color L_BACKGROUND = new Color(0xF3, 0xF4, 0xF8);
    private static final Color L_SURFACE = Color.WHITE;
    private static final Color L_SURFACE_ALT = new Color(0xF7, 0xF8, 0xFB);
    private static final Color L_BORDER = new Color(0xDD, 0xE1, 0xEA);
    private static final Color L_TEXT = new Color(0x1F, 0x24, 0x33);
    private static final Color L_MUTED = new Color(0x6B, 0x72, 0x80);

    // ---- dark palette ---------------------------------------------------
    private static final Color D_BACKGROUND = new Color(0x1A, 0x1D, 0x27);
    private static final Color D_SURFACE = new Color(0x23, 0x27, 0x34);
    private static final Color D_SURFACE_ALT = new Color(0x2A, 0x2F, 0x3E);
    private static final Color D_BORDER = new Color(0x3A, 0x40, 0x52);
    private static final Color D_TEXT = new Color(0xE9, 0xEC, 0xF3);
    private static final Color D_MUTED = new Color(0x9A, 0xA1, 0xB2);

    // ---- accents, identical in both modes for consistent branding ------
    public static final Color PRIMARY = new Color(0x4F, 0x6B, 0xFF);
    public static final Color PRIMARY_DARK = new Color(0x3B, 0x53, 0xD9);
    public static final Color SUCCESS = new Color(0x2E, 0xB6, 0x72);
    public static final Color WARNING = new Color(0xE8, 0xA0, 0x2B);
    public static final Color DANGER = new Color(0xE0, 0x50, 0x5E);
    public static final Color PURPLE = new Color(0x9B, 0x5D, 0xE0);
    public static final Color TEAL = new Color(0x1F, 0xA8, 0xA0);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BASE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    private ModernTheme() {
    }

    public static boolean isDark() {
        return dark;
    }

    public static Color background() {
        return dark ? D_BACKGROUND : L_BACKGROUND;
    }

    public static Color surface() {
        return dark ? D_SURFACE : L_SURFACE;
    }

    public static Color surfaceAlt() {
        return dark ? D_SURFACE_ALT : L_SURFACE_ALT;
    }

    public static Color border() {
        return dark ? D_BORDER : L_BORDER;
    }

    public static Color text() {
        return dark ? D_TEXT : L_TEXT;
    }

    public static Color muted() {
        return dark ? D_MUTED : L_MUTED;
    }

    /**
     * Applies the current palette (light by default) to the running Swing
     * application. Safe to call several times, e.g. after {@link #toggleDark()}.
     */
    public static void apply() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // fall back silently to the cross-platform default look
        }

        // ColorUIResource/FontUIResource (not plain Color/Font) so that a later
        // updateComponentTreeUI() - triggered by toggleDark() - is allowed to
        // overwrite them again; Swing's installDefaults() only replaces a
        // component's color/font when it is still a UIResource.
        putColor("control", surface());
        putColor("info", surface());
        putColor("nimbusBase", PRIMARY);
        putColor("Panel.background", background());
        putColor("OptionPane.background", background());
        putColor("Viewport.background", surface());

        putColor("Label.foreground", text());
        putFont("Label.font", FONT_BASE);

        putColor("TextField.background", surface());
        putColor("TextField.foreground", text());
        putColor("TextField.caretForeground", text());
        putFont("TextField.font", FONT_BASE);
        putColor("TextArea.background", surface());
        putColor("TextArea.foreground", text());
        putFont("TextArea.font", FONT_MONO);

        putColor("ComboBox.background", surface());
        putColor("ComboBox.foreground", text());
        putFont("ComboBox.font", FONT_BASE);

        putColor("Table.background", surface());
        putColor("Table.foreground", text());
        putColor("Table.gridColor", border());
        putColor("Table.selectionBackground", withAlpha(PRIMARY, 60));
        putColor("Table.selectionForeground", text());
        putFont("Table.font", FONT_BASE);
        putColor("TableHeader.background", surfaceAlt());
        putColor("TableHeader.foreground", muted());
        putFont("TableHeader.font", FONT_SMALL);

        putColor("ScrollPane.background", surface());
        UIManager.put("ScrollBar.width", 12);

        putColor("SplitPane.background", background());
        putColor("SplitPaneDivider.draggingColor", border());

        putColor("ToolTip.background", dark ? D_SURFACE_ALT : L_TEXT);
        putColor("ToolTip.foreground", dark ? D_TEXT : Color.WHITE);
        putFont("ToolTip.font", FONT_SMALL);

        applyToAllWindows();
    }

    private static void putColor(String key, Color color) {
        UIManager.put(key, new javax.swing.plaf.ColorUIResource(color));
    }

    private static void putFont(String key, Font font) {
        UIManager.put(key, new javax.swing.plaf.FontUIResource(font));
    }

    /** Flips light/dark and re-applies the palette to every open window. */
    public static void toggleDark() {
        dark = !dark;
        apply();
        for (Runnable hook : refreshHooks) {
            hook.run();
        }
    }

    /**
     * Registers a callback invoked after every theme change, so a window
     * that set explicit colors on stock Swing components (a content pane,
     * a JTable, a JTextPane, ...) can refresh them; {@link #apply} alone
     * only affects components still using UIManager defaults.
     */
    public static void onThemeChange(Runnable hook) {
        refreshHooks.add(hook);
    }

    private static void applyToAllWindows() {
        for (Window w : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(w);
        }
    }

    /** Returns {@code base} with a modified alpha channel. */
    public static Color withAlpha(Color base, int alpha) {
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
    }
}
