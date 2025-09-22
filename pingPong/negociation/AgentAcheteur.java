package pingPong.negociation;

import jade.core.*;
import jade.core.Runtime;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.ExtendedProperties;

import java.util.Properties;

import static java.lang.System.out;

/**
 * Agent class to allow exchange of messages between an agent named ping, that initiates the 'dialog', and an agent
 * named 'pong'
 *
 * @author emmanueladam
 */
public class AgentAcheteur extends Agent {

    double prixAcheteur;
    double prixMax;
    double delta;
    int nbEchanges;
    AID aidVendeur;
    /**
     * agent setup, adds its behaviours
     */
    @Override
    protected void setup() {
         prixAcheteur = 50;
         prixMax = 200;
         delta = 0.01;
         nbEchanges = 20;

        println(getLocalName() + " -> Hello, my address is " + getAID());

        var modele = MessageTemplate.and(
                MessageTemplate.MatchConversationId("MARCHE"),
                MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));

        // add a behavior, with 20 iterations, that wait for a 'INFORM' msg about 'SPORT' and replies to it after 300ms
        addBehaviour(new Behaviour(this) {
            int step = 0;
            boolean nego = true;

            @Override
            public void action() {
                var msg = receive(modele);
                if (msg != null) {
                    step++;
                    var content = msg.getContent();
                    aidVendeur = msg.getSender();
                    var reply = msg.createReply();
                    double offre = Double.parseDouble(content);
                    println("%s -> I received \"%s\" from '%s'".formatted(getLocalName(),content,aidVendeur.getLocalName()));
                    myAgent.doWait(300);
                    prixAcheteur = prixAcheteur * (1+ delta);
                    if (offre > prixAcheteur) {
                        reply.setContent(String.valueOf(prixAcheteur));
                    }
                    else {
                        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        reply.setContent("ok pour " + offre +"€ !!");
                        nego = false;
                    }
                    myAgent.send(reply);
                } else block();
            }

            @Override
            public boolean done() {
                if (step == nbEchanges && nego) {
                    println(getLocalName() + " -> I don't buy anymore");
                    var msg = new ACLMessage(ACLMessage.CANCEL);
                    msg.addReceiver(aidVendeur);
                    myAgent.send(msg);
                }
                return step == nbEchanges;
            }

        });

        var modele2 = MessageTemplate.MatchPerformative(ACLMessage.FAILURE);
        // add a behaviour that wait for an eventual failure msg
        addBehaviour(new ReceiverBehaviour(this,  -1, modele2,true, (a, msg) ->
                println(getLocalName() + " -> I received an error msg from " + msg.getSender().getLocalName() + " : " + msg.getContent())
        ));

        var modele3 = MessageTemplate.MatchPerformative(ACLMessage.CANCEL);
        // add a behaviour that wait for an eventual failure msg
        addBehaviour(new ReceiverBehaviour(this,  -1, modele3,true, (a, msg) ->
                println(getLocalName() + " -> " + msg.getSender().getLocalName() + " stopped the negociation  : " + msg.getContent())
        ));

        var modele4 = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        // add a behaviour that wait for an eventual failure msg
        addBehaviour(new ReceiverBehaviour(this,  -1, modele4,true, (a, msg) ->
                println(getLocalName() + " -> " + msg.getSender().getLocalName() + " accepted the offer  : " + msg.getContent())
        ));
    }

    /**I inform the user when I leave the platform*/
    @Override
    protected void takeDown() {
        out.println(getLocalName() + " -> I leave the plateform ! ");
    }

}
