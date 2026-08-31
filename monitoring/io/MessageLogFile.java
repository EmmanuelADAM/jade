package monitoring.io;

import jade.lang.acl.ACLCodec;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.StringACLCodec;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Save/load of ACL messages to/from a plain text file, for both
 * {@code monitoring.agents.ModernDummyAgent} (a single composed message, or
 * its whole sent/received history) and {@code monitoring.agents.ModernSnifferAgent}
 * (its recorded log).
 * <p>
 * A single message is written exactly as {@link ACLMessage#toString()}
 * renders it (the standard FIPA-ACL text syntax, readable by any ACL
 * parser). A log of several messages prefixes each one with a plain
 * {@code ### DIRECTION|owner|timestamp} marker line so direction/owner/time
 * survive the round trip; this marker is specific to this tool, not part of
 * the FIPA-ACL syntax.
 *
 * @author emmanuel adam
 * @version 1
 */
public final class MessageLogFile {

    private static final String MARKER = "### ";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /** one entry of a saved log: who reported it, in which direction, when, and the message itself */
    public record Entry(String direction, String owner, LocalDateTime time, ACLMessage message) {
    }

    private MessageLogFile() {
    }

    public static void writeMessage(ACLMessage message, File file) throws IOException {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(message.toString());
        }
    }

    public static ACLMessage readMessage(File file) throws IOException, ACLCodec.CodecException {
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            StringACLCodec codec = new StringACLCodec(reader, null);
            return codec.decode();
        }
    }

    public static void writeLog(List<Entry> entries, File file) throws IOException {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            for (Entry entry : entries) {
                writer.write(MARKER + entry.direction() + "|" + entry.owner() + "|"
                        + entry.time().format(TIME_FORMAT) + "\n");
                writer.write(entry.message().toString());
                writer.write("\n\n");
            }
        }
    }

    public static List<Entry> readLog(File file) throws IOException, ACLCodec.CodecException {
        List<Entry> entries = new ArrayList<>();
        List<String> lines;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            lines = reader.lines().toList();
        }

        String direction = null;
        String owner = null;
        LocalDateTime time = null;
        StringBuilder body = new StringBuilder();

        for (String line : lines) {
            if (line.startsWith(MARKER)) {
                flush(entries, direction, owner, time, body);
                body.setLength(0);
                String[] parts = line.substring(MARKER.length()).split("\\|", 3);
                direction = parts.length > 0 ? parts[0] : "SENT";
                owner = parts.length > 1 ? parts[1] : "?";
                time = parts.length > 2 ? LocalDateTime.parse(parts[2], TIME_FORMAT) : LocalDateTime.now();
            } else {
                body.append(line).append('\n');
            }
        }
        flush(entries, direction, owner, time, body);
        return entries;
    }

    private static void flush(List<Entry> entries, String direction, String owner, LocalDateTime time,
                               StringBuilder body) throws ACLCodec.CodecException {
        if (direction == null || body.toString().isBlank()) {
            return;
        }
        StringACLCodec codec = new StringACLCodec(new StringReader(body.toString()), null);
        ACLMessage message = codec.decode();
        entries.add(new Entry(direction, owner, time, message));
    }
}
