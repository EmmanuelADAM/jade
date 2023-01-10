package protocols.bordaCount.agents;


import jade.core.AgentServicesTools;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * agent that waits for a message from a CFP protocol, prepares the response, and returns it
 *
 * @author eadam
 */
public class ParticipantAgent extends AgentWindowed {

    /**
     * agent setup
     * - registration to the service "vote"-"participant"
     * - add the contractnetresponder protocol behaviour
     * - create the gui
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());

        AgentServicesTools.register(this, "vote", "participant");

        MessageTemplate model = MessageTemplate.MatchConversationId("voteNo1");

        ContractNetResponder comportementVote = new ContractNetResponder(this, model) {

            //function triggered by a PROPOSE msg : send back the ranking
            @Override
            protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
                println("~".repeat(40));
                println(cfp.getSender().getLocalName() + " proposes this options: " + cfp.getContent());
                ACLMessage answer = cfp.createReply();
                answer.setPerformative(ACLMessage.PROPOSE);
                String choice = makeItsChoice(cfp.getContent());
                answer.setContent(choice);
                return answer;
            }

            /**proposals in the form option1,option2,option3,option4,.....
             * * he returns his choice by ordering the options and giving their positions
             * @param offres list of proposals in the form of option1,option2,option3,option4
             * @return orderly choice in the form of option2_1,option4_2,option3_3,option1_4
             * */
            private String makeItsChoice(String offres) {
                ArrayList<String> choice = new ArrayList<>(List.of(offres.split(",")));
                Collections.shuffle(choice);
                StringBuilder sb = new StringBuilder();
                String pref = ">";
                for (String s : choice) sb.append(s).append(pref);
                String proposition  = sb.substring(0, sb.length()-1);
                println("I propose this ranking: " + proposition);
                return proposition;
            }

            //function triggered by a ACCEPT_PROPOSAL msg : the polling station agent  accept the vote
            //@param cfp : the initial cfp message
            //@param propose : the proposal I sent
            //@param accept : the acceptation sent by the auctioneer
            @Override
            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
                println("=".repeat(15));
                println("END OF ROUND");
                println(cfp.getSender().getLocalName() + " started a vote between " + cfp.getContent());
                println(" I proposed " + propose.getContent());
                println(cfp.getSender().getLocalName() + " accepted my vote and sent the result:  " + accept.getContent());
                ACLMessage msg = accept.createReply();
                msg.setPerformative(ACLMessage.INFORM);
                msg.setContent("ok !");
                return msg;
            }

            //function triggered by a REJECT_PROPOSAL msg : the auctioneer rejected my vote !
            //@param cfp : the initial cfp message
            //@param propose : the proposal I sent
            //@param accept : the reject sent by the auctioneer
            @Override
            protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                println("=".repeat(10));
                println("VOTE REJECTED");
                println(cfp.getSender().getLocalName() + " started a vote between " + cfp.getContent());
                println(" I proposed " + propose.getContent());
                println(cfp.getSender().getLocalName() + " refused ! with this message: " + reject.getContent());
            }


        };

        addBehaviour(comportementVote);

    }

    //before leaving, the agent unsubscribe from its services
    @Override
    public void takeDown() {
        AgentServicesTools.deregisterAll(this);
        System.err.println(this.getLocalName() + ", I leave the platform...");
    }

}
