package ticTac;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * classe d'agents pour échange entre 1 agent de cette classe et 1 autre<br>
 * 2 msg de type differents sont envoyes
 *
 * @author emmanueladam
 */
public class AgentPosteur extends Agent {
    /**
     * Initialisation de l'agents
     */
    @Override
    protected void setup() {

        // creation d'un comportement qui enverra le texte 'tictac' toutes les secondes à l'agent demineur
        final var msgTic = new ACLMessage(ACLMessage.INFORM);
        //ajout d'un tag CLOCK au message
        msgTic.setConversationId("CLOCK");
        msgTic.addReceiver("agentDemineur");
        msgTic.setContent("tictac");
        TickerBehaviour compTicTac = new TickerBehaviour(AgentPosteur.this, 1000, a->a.send(msgTic));

        // ajout du  comportement cyclique  dans 1 secondes
        addBehaviour(new WakerBehaviour(this, 1000, a->a.addBehaviour(compTicTac)));

        // ajout d'un comportement qui enverra le texte 'boom'   dans 10 secondes
        final var msgBoom = new ACLMessage(ACLMessage.INFORM);
        //ajout d'un tag BOOM au message
        msgBoom.setConversationId("BOOM");
        msgBoom.addReceiver("agentDemineur");
        msgBoom.setContent("b o o m ! ! !");
        addBehaviour(new WakerBehaviour(this, 10000, a->{a.removeBehaviour(compTicTac);a.send(msgBoom);}));
    }
}
