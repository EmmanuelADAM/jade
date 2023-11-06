package td.negociationWindow;

import jade.core.Agent;
import jade.core.behaviours.ReceiverBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import static java.lang.System.out;

/**
 * Agent class to represent simple negociation between 2 agents.
 * Here, the buyer agent
 *
 * @author emmanueladam
 */
public class BuyerAgent extends AgentWindowed {
    /**Upper threshold beyond which the buyer refuses to buy*/
    double threshold;
    /**price that the buyer propose to the seller*/
    double proposedPrice;
    /**the buyer refuse the negociation beyond this nb max of rounds of negociation*/
    int maxRounds;
    /**coef applied to propose a new price*/
    double coef = 0.1;
    int[] nbTours ={0};

    /**
     * agent setup, adds its behaviours
     */
    @Override
    protected void setup() {
        window = new SimpleWindow4Agent(getLocalName(),this);
        reset();


        //wait for a propose msg
        var modele = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);

        addBehaviour(new ReceiverBehaviour(this, -1, modele, true,(a,msg)->{
            nbTours[0]++;
            double receivedPrice = Double.parseDouble(msg.getContent());
            println("-> I've received %.2f\tround(%d/%d)".formatted(receivedPrice,nbTours[0], maxRounds));
            var myAnswer = msg.createReply();
            if (receivedPrice<= proposedPrice*(1+coef)){
                println("\t"+getLocalName() + " -> I accept!");
                myAnswer.setPerformative(ACLMessage.ACCEPT_PROPOSAL);reset();}
            else
            if (receivedPrice> threshold){
                println(  " -> I refuse!");
                myAnswer.setPerformative(ACLMessage.REJECT_PROPOSAL);reset();}
            else
            if (nbTours[0]>maxRounds){
                println(  " -> I don't have time to negotiate anymore, I refuse and I stop there.");
                myAnswer.setPerformative(ACLMessage.REJECT_PROPOSAL);reset();}
            else{
                proposedPrice = proposedPrice *(1+coef);
                myAnswer.setPerformative(ACLMessage.PROPOSE);
                myAnswer.setContent(String.valueOf(proposedPrice));
                println("\t-> I propose %.2f".formatted( proposedPrice));
            }
            a.send(myAnswer);
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


}
