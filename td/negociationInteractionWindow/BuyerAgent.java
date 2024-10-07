package td.negociationInteractionWindow;

import jade.core.AID;
import jade.core.behaviours.ReceiverBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import td.negociationInteractionWindow.gui.BuyerGui4Agent;

import static java.lang.System.out;

/**
 * Agent class to represent simple negociation between 2 agents.
 * Here, the buyer agent
 *
 * @author emmanueladam
 */
public class BuyerAgent extends GuiAgent {
    /**Upper threshold beyond which the buyer refuses to buy*/
    double threshold;
    /**price that the buyer propose to the seller*/
    double proposedPrice;
    /**the buyer refuse the negociation beyond this nb max of rounds of negociation*/
    int maxRounds;
    /**coef applied to propose a new price*/
    double coef = 0.1;
    int[] nbTours ={0};
    BuyerGui4Agent window;
    AID aidSeller;

    /**
     * agent setup, adds its behaviours
     */
    @Override
    protected void setup() {
        window = new BuyerGui4Agent(this);
//        reset();


        //wait for a propose msg
        var modele = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
        addBehaviour(new ReceiverBehaviour(this, -1, modele, true,(a,msg)->{
            aidSeller = msg.getSender();
            window.println("-> I've received a proposal of %.2f€".formatted(Double.parseDouble(msg.getContent())));
        }));

        //wait for a accept proposal msg
        modele = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        addBehaviour(new ReceiverBehaviour(this, -1, modele, true,(a,msg)-> {println(getLocalName() + " -> seller accept !!!");reset();}));

        //wait for a reject proposal msg
        modele = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);
        addBehaviour(new ReceiverBehaviour(this, -1, modele, (a,msg)-> {println(getLocalName() + " -> seller reject !!!");reset();}));

    }

    private void reset() {
        maxRounds = 4 + (int)(Math.random()*4); //[4 - 7]
        threshold = 280 + (int)(Math.random()*40); //[280 - 320[
        proposedPrice = 100 + (int)(Math.random()*40); //[100 - 140[
        nbTours[0] = 0;
        println("X".repeat(30));
        println("X".repeat(30));
        println("""        
        -> I am ready.
            I accept %d max rounds of negotiation.
            I start with a proposal of %.2f.
            And I will not go above %.2f""".formatted( maxRounds, proposedPrice, threshold ));
        println("~".repeat(20));
    }

    /**I inform the user when I leave the platform*/
    @Override
    protected void takeDown() {
        out.println(getLocalName() + " -> I leave the plateform ! ");
    }

    /**
     * Send a message to the agents registered under a given service
     * @param text text to send
     */
    private void sendMessage(String text) {
        ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
        msg.setContent(text);
        msg.addReceiver(aidSeller);
        send(msg);
        window.println("-> \""+text + "\" sent to the seller agent");
    }


    @Override
    protected void onGuiEvent(GuiEvent ev) {
        switch (ev.getType()) {
            case BuyerGui4Agent.SENDOFFER -> sendMessage(window.lowTextArea.getText());
            case BuyerGui4Agent.QUITCODE -> doDelete();
        }
    }

}
