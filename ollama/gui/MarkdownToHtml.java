package ollama.gui;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * a small Markdown to HTML converter, enough for the answers of a LLM
 * (titles, bold, italic, code, lists, tables, quotes, links),
 * the HTML produced can be displayed by a JEditorPane (HTML 3.2)
 *
 * @author emmanuel adam
 * @version 1
 */
public class MarkdownToHtml {
    private static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.*?)\\s*#*$");
    private static final Pattern RULE = Pattern.compile("^\\s*([-*_])(\\s*\\1){2,}\\s*$");
    private static final Pattern UL_ITEM = Pattern.compile("^(\\s*)[-*+]\\s+(.*)$");
    private static final Pattern OL_ITEM = Pattern.compile("^(\\s*)\\d+[.)]\\s+(.*)$");
    private static final Pattern TABLE_SEPARATOR = Pattern.compile("^\\s*\\|?\\s*:?-+:?\\s*(\\|\\s*:?-+:?\\s*)*\\|?\\s*$");

    private final StringBuilder html = new StringBuilder();
    /** opened lists : type ("ul" or "ol") and indentation of each level */
    private final Deque<String> listTypes = new ArrayDeque<>();
    private final Deque<Integer> listIndents = new ArrayDeque<>();
    private final StringBuilder paragraph = new StringBuilder();
    private boolean inCode = false;
    private boolean inTable = false;
    private boolean inQuote = false;

    /**
     * convert a markdown text into html (without the html and body tags)
     */
    public static String convert(String markdown) {
        MarkdownToHtml converter = new MarkdownToHtml();
        for (String line : markdown.split("\r?\n", -1)) converter.addLine(line);
        converter.closeAll();
        // a code block not yet closed (the answer is still arriving) is closed here
        if (converter.inCode) converter.html.append("</pre>\n");
        return converter.html.toString();
    }

    private void addLine(String line) {
        String trimmed = line.trim();
        // code block between ``` or ~~~
        if (trimmed.startsWith("```") || trimmed.startsWith("~~~")) {
            if (inCode) html.append("</pre>\n");
            else {
                closeAll();
                html.append("<pre>");
            }
            inCode = !inCode;
            return;
        }
        if (inCode) {
            html.append(escape(line)).append('\n');
            return;
        }
        if (trimmed.isEmpty()) {
            closeAll();
            return;
        }
        // table : lines beginning with |
        if (trimmed.startsWith("|")) {
            if (TABLE_SEPARATOR.matcher(trimmed).matches()) return;
            addTableRow(trimmed);
            return;
        }
        closeTable();
        // quote
        boolean quote = trimmed.startsWith(">");
        if (quote != inQuote) {
            closeParagraph();
            closeLists();
            html.append(quote ? "<blockquote>" : "</blockquote>\n");
            inQuote = quote;
        }
        if (quote) {
            line = trimmed.substring(1);
            trimmed = line.trim();
            if (trimmed.isEmpty()) { closeParagraph(); return; }
        }

        Matcher m;
        if ((m = HEADING.matcher(trimmed)).matches()) {
            closeParagraph();
            closeLists();
            int level = m.group(1).length();
            html.append("<h").append(level).append('>').append(inline(m.group(2))).append("</h").append(level).append(">\n");
        } else if (RULE.matcher(trimmed).matches()) {
            closeParagraph();
            closeLists();
            html.append("<hr>\n");
        } else if ((m = UL_ITEM.matcher(line)).matches()) {
            addListItem("ul", m.group(1), m.group(2));
        } else if ((m = OL_ITEM.matcher(line)).matches()) {
            addListItem("ol", m.group(1), m.group(2));
        } else if (!listTypes.isEmpty() && line.startsWith(" ")) {
            // following of a list item
            html.append("<br>").append(inline(trimmed));
        } else {
            closeLists();
            if (paragraph.length() > 0) paragraph.append("<br>");
            paragraph.append(inline(trimmed));
        }
    }

    private void addListItem(String type, String indentation, String text) {
        closeParagraph();
        int indent = indentation.replace("\t", "    ").length();
        // close the deeper lists
        while (!listIndents.isEmpty() && indent < listIndents.peek()) closeList();
        if (listIndents.isEmpty() || indent > listIndents.peek()) {
            html.append('<').append(type).append(">\n");
            listTypes.push(type);
            listIndents.push(indent);
        } else if (!listTypes.peek().equals(type)) {
            // same level but other kind of list
            closeList();
            html.append('<').append(type).append(">\n");
            listTypes.push(type);
            listIndents.push(indent);
        }
        html.append("<li>").append(inline(text)).append('\n');
    }

    private void addTableRow(String row) {
        closeParagraph();
        closeLists();
        String tag = inTable ? "td" : "th";
        if (!inTable) {
            html.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"4\">\n");
            inTable = true;
        }
        if (row.startsWith("|")) row = row.substring(1);
        if (row.endsWith("|")) row = row.substring(0, row.length() - 1);
        html.append("<tr>");
        for (String cell : row.split("\\|", -1))
            html.append('<').append(tag).append('>').append(inline(cell.trim())).append("</").append(tag).append('>');
        html.append("</tr>\n");
    }

    private void closeParagraph() {
        if (paragraph.length() > 0) {
            html.append("<p>").append(paragraph).append("</p>\n");
            paragraph.setLength(0);
        }
    }

    private void closeList() {
        html.append("</").append(listTypes.pop()).append(">\n");
        listIndents.pop();
    }

    private void closeLists() {
        while (!listTypes.isEmpty()) closeList();
    }

    private void closeTable() {
        if (inTable) {
            html.append("</table>\n");
            inTable = false;
        }
    }

    private void closeAll() {
        closeParagraph();
        closeLists();
        closeTable();
        if (inQuote) {
            html.append("</blockquote>\n");
            inQuote = false;
        }
    }

    /**
     * inline elements : `code`, **bold**, *italic*, ~~strike~~, [link](url)
     */
    static String inline(String text) {
        StringBuilder sb = new StringBuilder();
        // the parts between ` are code, and are not transformed
        String[] parts = text.split("`", -1);
        for (int i = 0; i < parts.length; i++) {
            if (i % 2 == 1 && i < parts.length - 1) {
                sb.append("<code>").append(escape(parts[i])).append("</code>");
            } else {
                String s = escape(parts[i]);
                if (i % 2 == 1) s = "`" + s; // a lonely `
                s = s.replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>");
                s = s.replaceAll("__(.+?)__", "<b>$1</b>");
                s = s.replaceAll("(?<![\\w*])\\*(?!\\s)(.+?)(?<!\\s)\\*(?![\\w*])", "<i>$1</i>");
                s = s.replaceAll("(?<!\\w)_(?!\\s)(.+?)(?<!\\s)_(?!\\w)", "<i>$1</i>");
                s = s.replaceAll("~~(.+?)~~", "<strike>$1</strike>");
                s = s.replaceAll("\\[([^\\]]+)]\\(([^)\\s]+)\\)", "<a href=\"$2\">$1</a>");
                sb.append(s);
            }
        }
        return sb.toString();
    }

    /**
     * escape the html special characters
     */
    public static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
