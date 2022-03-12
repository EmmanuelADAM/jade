package ticTac;

import jade.core.*;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.MessageTemplate;

import static java.lang.System.out;

/**
 * classe d'agent qui ecoute 2 types de messages.
 *
 * @author emmanueladam
 * */
public class AgentLecteur extends Agent {
    /**
     * Initialisation de l'agent
     */
    @Override
    protected void setup() {
        out.println("De l'agent " + getLocalName());

        // ajout d'un comportement qui attend des msgs de type clock
        addBehaviour(new CyclicBehaviour(this) {
            final MessageTemplate mt = MessageTemplate.MatchConversationId("CLOCK");

            public void action() {
                var msg = receive(mt);
                if (msg != null) {
                    var content = msg.getContent();
                    var sender = msg.getSender();
                    out.println("agent " + getLocalName() + " : j'ai reçu " + content + " de " + sender);
                } else block();
            }
        });
        // ajout d'un comportement qui attend des msgs de type boom
        addBehaviour(new CyclicBehaviour(this) {
            final MessageTemplate mt = MessageTemplate.MatchConversationId("BOOM");

            public void action() {
                var msg = receive(mt);
                if (msg != null) {
                    var content = msg.getContent();
                    var sender = msg.getSender();
                    out.println("attention !!!! moi agent  " + getLocalName() + " : j'ai reçu " + content + " de " + sender);
                } else block();
            }
        });
    }
}
