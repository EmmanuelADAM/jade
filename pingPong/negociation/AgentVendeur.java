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
public class AgentVendeur extends Agent {

    double prixVendeur;
    double prixMin;
    double delta;
    int nbEchanges;
    AID aidAcheteur;

    /**
     * agent setup, adds its behaviours
     */
    @Override
    protected void setup() {
        prixVendeur = 100;
        prixMin = 25;
        delta = 0.01;
        nbEchanges = 10;


        println(getLocalName() + " -> Hello, my address is " + getAID());
        // if the agent names "ping"
        // add a behaviour that will send the first "ball" msg in 10 sec. to "pong" agent
        long temps = 10000;
        out.println(getLocalName() + " -> I start in" + temps + " ms");

        addBehaviour(new WakerBehaviour(this, temps) {
            protected void onWake() {
                var msg = new ACLMessage(ACLMessage.PROPOSE);
                msg.addReceiver("acheteur");
                msg.setContent(String.valueOf(prixVendeur));
                msg.setConversationId("MARCHE");
                myAgent.send(msg);
                println(getLocalName() + " -> I launch the negociation");
            }
        });


        var modele = MessageTemplate.and(
                MessageTemplate.MatchConversationId("MARCHE"),
                MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
        // add a behavior, with 20 iterations, that wait for a 'INFORM' msg about 'SPORT' and replies to it after 300ms
        addBehaviour(new Behaviour(this) {
            int step = 0;
            boolean nego = true;


            public void action() {
                var msg = receive(modele);
                if (msg != null) {
                    step++;
                    var content = msg.getContent();
                    var sender = msg.getSender();
                    var reply = msg.createReply();
                    println("%s -> I received \"%s\" from '%s'".formatted(getLocalName(),content,sender.getLocalName()));
                    myAgent.doWait(300);
                    double offre = Double.parseDouble(content);
                    prixVendeur = prixVendeur * (1 - delta);
                    if (offre < prixVendeur) {
                        reply.setContent(String.valueOf(prixVendeur));
                    }
                    else {
                        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        reply.setContent("ok pour " + offre +"€ !!");
                        nego = false;
                    }
                    myAgent.send(reply);
                } else block();
            }

            public boolean done() {
                if (step == nbEchanges && nego) {
                    println(getLocalName() + " -> I don't sell anymore");
                    var msg = new ACLMessage(ACLMessage.CANCEL);
                    msg.addReceiver(aidAcheteur);
                    myAgent.send(msg);
                }
                return step == nbEchanges || !nego;
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
