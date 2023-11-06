package td.negociationWindow;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import static java.lang.System.out;

/**
 * Agent class to allow exchange of messages between an agent named ping, that initiates the 'dialog', and an agent
 * named 'pong'
 *
 * @author emmanueladam
 */
public class SellerAgent extends AgentWindowed {
    /**Upper threshold beyond which the buyer refuses to buy*/
    double threshold;
    /**price that the buyer propose to the seller*/
    double proposedPrice;
    /**the buyer refuse the negociation beyond this nb max of rounds of negociation*/
    int maxRounds;
    /**coef applied to propose a new price*/
    double coef = 0.1;

    int[] nbTours = {0};

    /**
     * agent setup, adds its behaviours
     */
    @Override
    protected void setup() {
        window = new SimpleWindow4Agent(getLocalName(),this);
        window.setButtonActivated(true);
        println("~".repeat(30));
        println("Click for a new negociation");

        addBehaviour(buildNegociationBehaviour());

    }

    private Behaviour buildNegociationBehaviour() {
        //wait for a propose msg
        ParallelBehaviour parab = new ParallelBehaviour();
        var modele = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
        parab.addSubBehaviour(new ReceiverBehaviour(this, -1, modele,true, (a,msg)->{
            nbTours[0]++;
            double receivedPrice = Double.parseDouble(msg.getContent());
            println("-> I've received %.2f\tround(%d/%d)".formatted(receivedPrice,nbTours[0], maxRounds));
            var myAnswer = msg.createReply();
            if (receivedPrice>= this.proposedPrice*(1-coef)){
                println("\t"+getLocalName() + " -> I accept!");
                myAnswer.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                println("~".repeat(30));
                println("Click for a new negociation");
            }
            else
            if (receivedPrice<threshold){
                println( " -> I refuse!");
                myAnswer.setPerformative(ACLMessage.REJECT_PROPOSAL);
                println("~".repeat(30));
                println("Click for a new negociation");
            }
            else
            if (nbTours[0]>maxRounds){
                println(" -> I don't have time to negotiate anymore, I refuse and I stop there.");
                myAnswer.setPerformative(ACLMessage.REJECT_PROPOSAL);
                println("~".repeat(30));
                println("Click for a new negociation");
            }
            else{
                this.proposedPrice = this.proposedPrice *(1-coef);
                myAnswer.setPerformative(ACLMessage.PROPOSE);
                myAnswer.setContent(String.valueOf(this.proposedPrice));
                println(" -> I propose %.2f".formatted( this.proposedPrice));
            }
            a.send(myAnswer);
        }));

        //wait for a accept proposal msg
        modele = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        parab.addSubBehaviour(new ReceiverBehaviour(this, -1, modele, (a,msg)-> {
            println(" -> seller accept !!!");
            println("~".repeat(30));
            println("Click for a new negociation");
        }));

        //wait for a reject proposal msg
        modele = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);
        parab.addSubBehaviour(new ReceiverBehaviour(this, -1, modele, (a,msg)-> {
            println( " -> seller reject !!!");
            println("~".repeat(30));
            println("Click for a new negociation");
        }));
        

        //wait for a reject proposal msg
        modele = MessageTemplate.MatchPerformative(ACLMessage.FAILURE);
        parab.addSubBehaviour(new ReceiverBehaviour(this, -1, modele, (a,msg)-> println( " -> an error !!! : " + msg.getContent())));

        return parab;
    }


    private void sendFirstMessage() {
        maxRounds = 4 + (int)(Math.random()*4); //[4 - 7]
        threshold = 100 * (0.8+Math.random()*0.4); //[80 - 120[
        proposedPrice = 250 + (int)(Math.random()*50); //[250 - 300[

        nbTours[0] = 0;
        println("X".repeat(30));
        println("X".repeat(30));
        println("""
        -> I am ready.
            I accept %d max rounds of negociation.
            I start whith a proposal of %.2f.
            And I will not go below %.2f""".formatted(maxRounds, proposedPrice, threshold ));
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


}
