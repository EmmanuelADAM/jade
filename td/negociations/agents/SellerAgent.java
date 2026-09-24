package td.negociations.agents;

import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import td.negociations.behaviours.NegociationBehaviour;
import td.negociations.behaviours.strategies.TemporalStrategy;
import td.negociations.gui.GuiSellerAgent;

/**
 * Agent class to allow exchange of messages between an agent named ping, that initiates the 'dialog', and an agent
 * named 'pong'
 *
 * @author emmanueladam
 */
public class SellerAgent  extends GuiAgent {
    /**Lower threshold beyond which the seller refuses to sell*/
    double threshold;
    /**price proposed at the very beginning of the current negociation (fixed for the whole negociation)*/
    double initialPrice;
    /**current price proposed by the seller, kept up to date by the annex temporal strategy behaviour*/
    double proposedPrice;
    /**the seller refuses the negociation beyond this nb max of rounds of negociation*/
    int maxRounds;
    /**shape of the concession over time : <1 tough (Boulware), 1 linear, >1 conciliatory (Conceder)*/
    double beta = 0.5;
    /**comportement de negociation*/
    NegociationBehaviour negoc;

    GuiSellerAgent window;

    /**
     * agent setup, adds its behaviours
     */
    @Override
    protected void setup() {
        window = new GuiSellerAgent(this);

        println("~".repeat(30));
        println("Click for a new negociation");
        var strategy = new TemporalStrategy(0.5, initialPrice,  threshold);
        // ou : new DistanceStrategy(0.25, 0.10, 0.15);
        // ou : new DistanceStrategy(0.25);
        // ou : new TitForTatStrategy(0.8, false);   // false = prix qui descend (vendeur)

        negoc = new NegociationBehaviour(this,
                (received, current) -> received >= current,
                price -> price < threshold,
                this::println);
        addBehaviour(negoc);
    }



    private void sendFirstMessage() {
        maxRounds = Integer.parseInt(window.jtfNbRounds.getText());
        threshold = Double.parseDouble(window.jtfMinimalPrice.getText());
        initialPrice = Double.parseDouble(window.jtfInitialPrice.getText());
        proposedPrice = initialPrice;
        var strategy = new TemporalStrategy(0.5, initialPrice,  threshold);
        // ou : new DistanceStrategy(0.25, 0.10, 0.15);
        // ou : new DistanceStrategy(0.25);
        // ou : new TitForTatStrategy(0.8, false);   // false = prix qui descend (vendeur)
        negoc.start(strategy, initialPrice, maxRounds);

        println("X".repeat(30));
        println("X".repeat(30));
        println("""
        -> I am ready.
            I accept %d max rounds of negociation.
            I start whith a proposal of %.2f.
            And I will not go below %.2f""".formatted(maxRounds, initialPrice, threshold ));
        println("~".repeat(20));
        var msg = new ACLMessage(ACLMessage.PROPOSE);
        msg.addReceiver("buyer");
        msg.setContent(String.valueOf(proposedPrice));
        this.send(msg);
        println("-> I proposed %.2f".formatted(proposedPrice));
    }


    @Override
    public void onGuiEvent(GuiEvent ev) {
        if (ev.getType() == SimpleWindow4Agent.OK_EVENT) {
            sendFirstMessage();
        }
        if (ev.getType() == SimpleWindow4Agent.QUIT_EVENT) {
            doDelete();
        }
    }

    /**I inform the user when I leave the platform*/
    @Override
    protected void takeDown() {
        System.out.println(getLocalName() + " -> I leave the plateform ! ");
    }



    public void println(String text){window.println(text);}

}
