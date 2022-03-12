package ticTac;

import jade.core.*;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * classe d'agents pour échange entre 1 agent de cette classe et 1 autre<br>
 *     2 msg de type differents sont envoyes
 * @author emmanueladam
 * */
public class AgentPosteur extends Agent {
    /**
     * Initialisation de l'agents
     */
    @Override
    protected void setup() {

        // creation d'un comportement qui enverra le texte 'tictac' toutes les secondes
        TickerBehaviour compTicTac = new TickerBehaviour(AgentPosteur.this, 1000) {
            @Override
            protected void onTick() {
                var msg = new ACLMessage(ACLMessage.INFORM);
                //ajout d'un identifiant au msg
                msg.setConversationId("CLOCK");
                msg.addReceiver(new AID("agentDemineur", AID.ISLOCALNAME));
                msg.setContent("tictac");
                myAgent.send(msg);
            }};
        // ajout du  comportement cyclique  dans 1 secondes
        addBehaviour(new WakerBehaviour(this, 1000) {
        protected void onWake() { myAgent.addBehaviour(compTicTac); } });

        // ajout d'un comportement qui enverra le texte 'boom'   dans 10 secondes
        addBehaviour(new WakerBehaviour(this, 10000) {
            protected void onWake() {
                    var msg = new ACLMessage(ACLMessage.INFORM);
                    msg.setConversationId("BOOM");
                    msg.addReceiver(new AID("agentDemineur", AID.ISLOCALNAME));
                    msg.setContent("b o o m ! ! !");
                    myAgent.removeBehaviour(compTicTac);
                    myAgent.send(msg);
            }});
    }
}
