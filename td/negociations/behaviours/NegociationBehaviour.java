package td.negociations.behaviours;

import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import td.negociations.behaviours.strategies.PricingStrategy;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoublePredicate;

/**
 * Comportement qui : reçoit les offres (PROPOSE), les évalue (accepter / refuser / faire
 * une contre-proposition), et réagit aux réponses de l'adversaire (ACCEPT_PROPOSAL / REJECT_PROPOSAL /
 * FAILURE).<br>
 * Ce comportement ne calcule pas lui-même le prix : à chaque offre reçue,
 * il délègue le calcul du nouveau prix à une {@link PricingStrategy} (temporelle, écart de prix,
 * tit-for-tat...).<br>
 * Ce comportement est ajouté une seule fois, dans le setup() de l'agent, et vit pour toute sa durée de
 * vie (plusieurs négociations successives peuvent avoir lieu).
 *
 * @author emmanueladam
 */
public class NegociationBehaviour extends ParallelBehaviour {

    /**no du tour de negociation */
    private int round = 0;
    /**nb max de tours de negociation */
    private int maxRounds = 0;
    /**offre courante*/
    private double currentPrice;
    /**strategie d'adaptation de prix*/
    private PricingStrategy strategy;

    /**
     * @param a                 l'agent propriétaire de ce comportement
     * @param isAcceptable      reçoit (prix reçu, prix courant), vrai si ce prix n'exige plus aucune concession (on accepte)
     * @param isBeyondThreshold reçoit le prix reçu, vrai si ce prix est hors limite (on refuse, quel que soit le round)
     * @param println           affiche un message (fenêtre de l'agent, console...)
     */
    public NegociationBehaviour(Agent a,
                                 BiFunction<Double, Double, Boolean> isAcceptable,
                                 DoublePredicate isBeyondThreshold,
                                 Consumer<String> println) {
        //ce comportement s'arretera lorsque tous s'arreteront (donc jamais ici)
        super(a, ParallelBehaviour.WHEN_ALL);

        // attente d'une proposition
        var proposeTemplate = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
        addSubBehaviour(new ReceiverBehaviour(a, -1, proposeTemplate, true, (ag, msg) -> {
            round++;
            double receivedPrice = Double.parseDouble(msg.getContent());
            //calcul du prix à proposer
            currentPrice = strategy.nextPrice(currentPrice, receivedPrice, round, maxRounds);
            println.accept("-> I've received %.2f\tround(%d/%d)\t(my current price: %.2f)"
                    .formatted(receivedPrice, round, maxRounds, currentPrice));

            var reply = msg.createReply();
            //par rapport à ce qu'on veut proposer est ce que le prix reçu est acceptable ?
            if (isAcceptable.apply(receivedPrice, currentPrice)) {
                println.accept("\t-> I accept!");
                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                println.accept("~".repeat(30));
                println.accept("Click for a new negociation");
            }
            //est ce que le prix reçu est au dela de l'acceptable ?
            else if (isBeyondThreshold.test(receivedPrice)) {
                println.accept("-> I refuse!");
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                println.accept("~".repeat(30));
                println.accept("Click for a new negociation");
            }
            //on arrête si le nombre d'échanges est trop grand
            else if (round > maxRounds) {
                println.accept("-> I don't have time to negotiate anymore, I refuse and I stop there.");
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                println.accept("~".repeat(30));
                println.accept("Click for a new negociation");
            }
            // si la negociation n'est pas finir, on propose le nouveau prix
            else {
                reply.setPerformative(ACLMessage.PROPOSE);
                reply.setContent(String.valueOf(currentPrice));
                println.accept("-> I propose %.2f".formatted(currentPrice));
            }
            ag.send(reply);
        }));

        // reaction à un message d'acceptation
        var acceptTemplate = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        addSubBehaviour(new ReceiverBehaviour(a, -1, acceptTemplate, true, (ag, msg) -> {
            println.accept("-> " + msg.getSender().getLocalName() + " accept !!!");
            println.accept("~".repeat(30));
            println.accept("Click for a new negociation");
        }));

        // reaction à un message de rejet
        var rejectTemplate = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);
        addSubBehaviour(new ReceiverBehaviour(a, -1, rejectTemplate, true, (ag, msg) -> {
            println.accept("-> " + msg.getSender().getLocalName()  + " reject !!!");
            println.accept("~".repeat(30));
            println.accept("Click for a new negociation");
        }));

        // réaction à un message d'erreur
        var failureTemplate = MessageTemplate.MatchPerformative(ACLMessage.FAILURE);
        addSubBehaviour(new ReceiverBehaviour(a, -1, failureTemplate, true,
                (ag, msg) -> println.accept("-> an error !!! : " + msg.getContent())));
    }

    /**
     * appelé par l'agent quand il (re)démarre une négociation
     * @param strategy          strategy de proposition d'offre
     * @param initialPrice prix de départ de cette négociation
     * @param maxRounds    nombre de tours max autorisés pour cette négociation
     */
    public void start( PricingStrategy strategy, double initialPrice, int maxRounds) {
        this.strategy = strategy;
        this.currentPrice = initialPrice;
        this.maxRounds = maxRounds;
        this.round = 0;
    }

    /** prix actuellement proposé par l'agent dans la négociation en cours */
    public double getCurrentPrice() {
        return currentPrice;
    }

    /** numéro du tour courant de la négociation en cours (0 avant la première offre reçue) */
    public int getRound() {
        return round;
    }
}
