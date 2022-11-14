package ticTac;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.MessageTemplate;

import java.util.function.Consumer;

import static java.lang.System.out;

/**
 * class for an aget that listen 2 types of messages
 *
 * @author emmanueladam
 */
public class AgentLecteur extends Agent {
    /**
     * Agent set-up
     */
    @Override
    protected void setup() {
        // add a behaviour that reacts to "CLOCK" msgs
        addBehaviour(new CyclicBehaviour(this) {
            final MessageTemplate mt = MessageTemplate.MatchConversationId("CLOCK");

            public void action() {
                var msg = receive(mt);
                if (msg != null) {
                    var content = msg.getContent();
                    var sender = msg.getSender();
                    println("%s -> I received \"%s\" from '%s'".formatted(getLocalName(), content,sender.getLocalName()));
                } else block();
            }
        });

        // add a behaviour that reacts to "BOOM" msgs
        addBehaviour(new CyclicBehaviour(this) {
            final MessageTemplate mt = MessageTemplate.MatchConversationId("BOOM");

            public void action() {
                var msg = receive(mt);
                if (msg != null) {
                    var content = msg.getContent();
                    var sender = msg.getSender();
                    println("ATTENTION :: %s -> I received \"%s\" from '%s'".formatted(getLocalName(), content,
                            sender.getLocalName()));
                } else block();
            }
        });

    }
}
