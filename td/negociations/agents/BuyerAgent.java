package td.negociations.agents;

import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import td.negociations.behaviours.strategies.DistanceStrategy;
import td.negociations.behaviours.NegociationBehaviour;
import td.negociations.behaviours.strategies.TemporalStrategy;
import td.negociations.gui.GuiBuyerAgent;

/**
 * Agent class to represent simple negociation between 2 agents.
 * Here, the buyer agent
 *
 * @author emmanueladam
 */
public class BuyerAgent extends GuiAgent {
    /**Upper threshold beyond which the buyer refuses to buy*/
    double threshold;
    /**price proposed at the very beginning of the current negociation (fixed for the whole negociation)*/
    double initialPrice;
    /**current price proposed by the buyer, kept up to date by the annex temporal strategy behaviour*/
    double proposedPrice;
    /**the buyer refuses the negociation beyond this nb max of rounds of negociation*/
    int maxRounds;
    /**shape of the concession over time : <1 tough (Boulware), 1 linear, >1 conciliatory (Conceder)*/
    double beta = 0.7;

    /**comportement de negociation*/
    NegociationBehaviour negoc;

    GuiBuyerAgent window;

    /**
     * agent setup, adds its behaviours
     */
    @Override
    protected void setup() {
        window = new GuiBuyerAgent(this);

        println("~".repeat(30));
        println("Set your parameters, then click START, and wait for the seller's offer");

        negoc = new NegociationBehaviour(this,
                (received, current) -> received <= current,
                price -> price > threshold,
                this::println);
        addBehaviour(negoc);

    }


    /**
     * reads the parameters set in the window, and gets ready to negotiate;
     * the buyer does not initiate the negociation, it just waits for the seller's opening offer
     */
    private void getReady() {
        maxRounds = Integer.parseInt(window.jtfNbRounds.getText());
        threshold = Double.parseDouble(window.jtfMaximalPrice.getText());
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
            And I will not go above %.2f""".formatted(maxRounds, initialPrice, threshold ));
        println("~".repeat(20));
    }

    @Override
    protected void onGuiEvent(GuiEvent ev) {
        switch (ev.getType()) {
            case GuiBuyerAgent.SENDOFFER -> getReady();
            case GuiBuyerAgent.QUITCODE -> doDelete();
        }
    }

    /**I inform the user when I leave the platform*/
    @Override
    protected void takeDown() {
        System.out.println(getLocalName() + " -> I leave the plateform ! ");
    }

    public void println(String text){window.println(text);}

}
