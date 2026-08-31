package monitoring.ui;

import jade.lang.acl.ACLMessage;

import java.awt.*;

/**
 * Maps a FIPA-ACL performative to a distinct accent color, so both the
 * modern Dummy Agent and the modern Sniffer can visually group messages by
 * type at a glance (a request-like performative, a positive answer, a
 * negative answer, an information, ...).
 *
 * @author emmanuel adam
 * @version 1
 */
public final class PerformativeColors {

    private PerformativeColors() {
    }

    public static Color of(int performative) {
        return switch (performative) {
            case ACLMessage.REQUEST, ACLMessage.CFP, ACLMessage.QUERY_IF, ACLMessage.QUERY_REF ->
                    ModernTheme.PRIMARY;
            case ACLMessage.INFORM, ACLMessage.CONFIRM, ACLMessage.AGREE, ACLMessage.ACCEPT_PROPOSAL ->
                    ModernTheme.SUCCESS;
            case ACLMessage.REFUSE, ACLMessage.FAILURE, ACLMessage.CANCEL, ACLMessage.DISCONFIRM,
                    ACLMessage.REJECT_PROPOSAL, ACLMessage.NOT_UNDERSTOOD -> ModernTheme.DANGER;
            case ACLMessage.PROPOSE -> ModernTheme.PURPLE;
            case ACLMessage.SUBSCRIBE, ACLMessage.PROXY -> ModernTheme.TEAL;
            case ACLMessage.INFORM_IF, ACLMessage.INFORM_REF -> ModernTheme.WARNING;
            default -> ModernTheme.muted();
        };
    }

    public static Color of(ACLMessage msg) {
        return of(msg.getPerformative());
    }

    public static String name(ACLMessage msg) {
        return ACLMessage.getPerformative(msg.getPerformative());
    }
}
