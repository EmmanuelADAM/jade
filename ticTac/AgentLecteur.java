package ticTac;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.MessageTemplate;

import java.util.function.Consumer;

import static java.lang.System.out;

/**
 * classe d'agent qui ecoute 2 types de messages.
 *
 * @author emmanueladam
 */
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
                    println("agent " + getLocalName() + " : j'ai recu " + content + " de " + sender);
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
                    println("attention !!!! moi agent  " + getLocalName() + " : j'ai recu " + content + " de " + sender);
                } else block();
            }
        });

        addBehaviour(new OneShotBehaviour(this, a-> out.println("coucou je suis " + a.getLocalName())));
    }
}
