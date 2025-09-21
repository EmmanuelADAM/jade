package td.negociationWindowSolution.agents;

import jade.core.AID;
import jade.core.behaviours.ReceiverBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import td.negociationWindowSolution.gui.GuiBuyerAgent;

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
    GuiBuyerAgent window;
    AID aidSeller;

    /**
     * agent setup, adds its behaviours
     */
    @Override
    protected void setup() {
        window = new GuiBuyerAgent(this);
//        reset();


        //wait for a propose msg
        var modele = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
        addBehaviour(new ReceiverBehaviour(this, -1, modele, true,(a,msg)->{
            nbTours[0]++;
            aidSeller = msg.getSender();
            window.println("-> Round %d, I've received a proposal of %.2f€".formatted(nbTours[0], Double.parseDouble(msg.getContent())));
        }));

        //wait for a accept proposal msg
        modele = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        addBehaviour(new ReceiverBehaviour(this, -1, modele, true,(a,msg)-> {window.println(getLocalName() + " -> seller accept !!!");reset();}));

        //wait for a reject proposal msg
        modele = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);
        addBehaviour(new ReceiverBehaviour(this, -1, modele, (a,msg)-> {window.println(getLocalName() + " -> seller reject !!!");reset();}));

    }

    private void reset() {
        nbTours[0] = 0;
        window.println("X".repeat(30));
        window.println("X".repeat(30));
        window.println("-> I am ready.");
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
            case GuiBuyerAgent.SENDOFFER -> sendMessage(window.lowTextArea.getText());
            case GuiBuyerAgent.QUITCODE -> doDelete();
        }
    }

}