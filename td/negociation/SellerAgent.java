package td.negociation;

import jade.core.Agent;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import static java.lang.System.out;

/**
 * Agent class to allow exchange of messages between an agent named ping, that initiates the 'dialog', and an agent
 * named 'pong'
 *
 * @author emmanueladam
 */
public class SellerAgent extends Agent {
    /**Upper threshold beyond which the buyer refuses to buy*/
    double threshold;
    /**price that the buyer propose to the seller*/
    double proposedPrice;
    /**the buyer refuse the negociation beyond this nb max of rounds of negociation*/
    int maxRounds;
    /**coef applied to propose a new price*/
    double coef = 0.1;

    /**
     * agent setup, adds its behaviours
     */
    @Override
    protected void setup() {

        maxRounds = 4 + (int)(Math.random()*4); //[4 - 7]
        threshold = 100 * (0.8+Math.random()*0.4); //[80 - 120[
        proposedPrice = 250 + (int)(Math.random()*50); //[250 - 300[

        int[] nbTours = {0};
        println("""
        %s\t -> I am ready.
            I accept %d max rounds of negociation.
            I start whith a proposal of %.2f.
            And I will not go below %.2f""".formatted(getLocalName() , maxRounds, proposedPrice, threshold ));
        println("~".repeat(20));

        int temps = 1000;
        // In temps ms, the seller propose a price for its object to the buyer
        addBehaviour(new WakerBehaviour(this, temps) {
            protected void onWake() {
                var msg = new ACLMessage(ACLMessage.PROPOSE);
                msg.addReceiver("buyer");
                msg.setContent(String.valueOf(proposedPrice));
                myAgent.send(msg);
                println("%s\t-> I proposed %.2f".formatted(getLocalName(), proposedPrice));
            }
        });

        //wait for a propose msg
        var modele = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);

        addBehaviour(new ReceiverBehaviour(this, -1, modele,true, (a,msg)->{
        /**A FAIRE*/ 
        }));

        //wait for a accept proposal msg
        modele = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        addBehaviour(new ReceiverBehaviour(this, -1, modele, (a,msg)-> println(getLocalName() + " -> seller accept !!!")));

        //wait for a reject proposal msg
        modele = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);
        addBehaviour(new ReceiverBehaviour(this, -1, modele, (a,msg)-> println(getLocalName() + " -> seller reject !!!")));

    }

    /**I inform the user when I leave the platform*/
    @Override
    protected void takeDown() {
        out.println(getLocalName() + " -> I leave the plateform ! ");
    }


}
