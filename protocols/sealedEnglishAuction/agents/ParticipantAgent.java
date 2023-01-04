package protocols.sealedEnglishAuction.agents;

import jade.core.AgentServicesTools;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

import java.awt.*;
import java.util.Random;


/**
 * agent that waits for a message from a CFP protocol, prepares the response, and returns it
 *
 * @author eadam
 */
@SuppressWarnings("serial")
public class ParticipantAgent extends AgentWindowed {

    /**
     * agent setup
     * - registration to the service "auction"-"bidder"
     * - add the contractnetresponder protocol behaviour
     * - create the gui
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        println("- ".repeat(20));
        Random hasard = new Random();

        AgentServicesTools.register(this, "auction", "bidder");
        MessageTemplate model = MessageTemplate.MatchConversationId("auction1");

        ContractNetResponder bidding = new ContractNetResponder(this, model) {

            //function triggered by a PROPOSE msg : decide to bid or not, return a response
            @Override
            protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
                window.setBackgroundTextColor(Color.WHITE);
                ACLMessage reponse = cfp.createReply();
                println("'%s' proposes the object '%s' for bidding...".formatted(cfp.getSender().getLocalName(),
                        cfp.getContent()));
                int offre = hasard.nextInt(0, 100);
                //ici l'agent refuse 1 fois sur 3 (lorsque la valeur aleatoire offre est < a 33)
                if (offre < 33) {
                    window.setBackgroundTextColor(Color.LIGHT_GRAY);
                    println("I decide to not bid for this object.");
                    reponse.setPerformative(ACLMessage.REFUSE);
                } else {
                    println(String.format("I propose %d to buy '%s' to the agent : '%s'", offre, cfp.getContent(),
                            cfp.getSender().getLocalName()));
                    reponse.setPerformative(ACLMessage.PROPOSE);
                    reponse.setContent(String.valueOf(offre));
                }
                println("-".repeat(30));
                return reponse;
            }

            //function triggered by a ACCEPT_PROPOSAL msg : the auctioneer accept my offer
            //@param cfp : the initial cfp message
            //@param propose : the proposal I sent
            //@param accept : the acceptation sent by the auctioneer
            @Override
            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
                window.setBackgroundTextColor(Color.YELLOW);
                println("-".repeat(30));
                println("OFFER ACCEPTED, as a reminder : ");
                println("'%s' launched an auction for '%s'".formatted(cfp.getSender().getLocalName(),
                        cfp.getContent()));
                println(" I've proposed " + propose.getContent());
                println("'%s' has accepted with this message '%s'".formatted(cfp.getSender().getLocalName(),
                        accept.getContent()));
                println("_".repeat(40));
                println("");
                ACLMessage msg = accept.createReply();
                msg.setPerformative(ACLMessage.INFORM);
                msg.setContent("ok !");
                return msg;
            }

            //function triggered by a ACCEPT_PROPOSAL msg : the auctioneer accept my offer
            //@param cfp : the initial cfp message
            //@param propose : the proposal I sent
            //@param accept : the acceptation sent by the auctioneer
            @Override
            protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                window.setBackgroundTextColor(Color.RED);
                println("-".repeat(30));
                println("OFFER REJECTED, as a reminder : ");
                println("'%s' launched an auction for '%s'".formatted(cfp.getSender().getLocalName(),
                        cfp.getContent()));
                println(" I've proposed " + propose.getContent());
                println("'%s' has declined with this message '%s'".formatted(cfp.getSender().getLocalName(), reject.getContent()));
                println("_".repeat(40));
                println("");
            }
        };

        addBehaviour(bidding);

    }



    @Override
    public void takeDown() {
        //before leaving, the agent unsubscribe from its services
        AgentServicesTools.deregisterAll(this);
        System.err.println("moi " + this.getLocalName() + ", je quitte la plateforme...");
    }

}
