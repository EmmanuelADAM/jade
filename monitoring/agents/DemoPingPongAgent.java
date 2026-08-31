package monitoring.agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import monitoring.Monitor;

/**
 * Tiny ping-pong agent (same idea as {@code pingPong.AgentPingPong}) whose
 * only addition is reporting every sent/received message to
 * {@link Monitor}, so that a {@code monitoring.agents.ModernSnifferAgent}
 * has real traffic to show out of the box. See
 * {@code monitoring.launch.LaunchModernMonitoringDemo}.
 *
 * @author emmanuel adam
 * @version 1
 */
public class DemoPingPongAgent extends Agent {

    private static final int ROUNDS = 8;

    @Override
    protected void setup() {
        println(getLocalName() + " -> Hello, my address is " + getAID());

        if (getLocalName().equals("ping")) {
            addBehaviour(new WakerBehaviour(this, 1000) {
                @Override
                protected void onWake() {
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver("pong");
                    msg.setContent("ball");
                    msg.setConversationId("SPORT");
                    Monitor.send(myAgent, msg);
                    println(getLocalName() + " -> I launch the ball");
                }
            });
        }

        MessageTemplate template = MessageTemplate.MatchConversationId("SPORT");
        addBehaviour(new Behaviour(this) {
            int step = 0;

            @Override
            public void action() {
                ACLMessage msg = receive(template);
                if (msg == null) {
                    block();
                    return;
                }
                Monitor.received(myAgent, msg);
                step++;
                println("%s -> I received \"%s\" from '%s'".formatted(
                        getLocalName(), msg.getContent(), msg.getSender().getLocalName()));
                if (step < ROUNDS) {
                    ACLMessage reply = msg.createReply();
                    reply.setContent("ball-" + step);
                    doWait(400);
                    Monitor.send(myAgent, reply);
                }
            }

            @Override
            public boolean done() {
                return step >= ROUNDS;
            }
        });
    }

    @Override
    protected void takeDown() {
        println(getLocalName() + " -> I leave the platform");
    }
}
