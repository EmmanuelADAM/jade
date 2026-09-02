package protocols.bordaCount5000.agents;


import jade.core.Agent;
import jade.core.AgentServicesTools;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * agent that waits for a message from a CFP protocol, prepares the response, and returns it
 * use AgentWindowed to display the messages in a window if few agents are used, otherwise use Agent to avoid too many windows (nb agents between 100 and 10000...)
 * @author eadam
 */
public class ParticipantAgent extends Agent  { // extends Agent {

    String myName = null;
    /**
     * agent setup
     * - registration to the service "vote"-"participant"
     * - add the contractnetresponder protocol behaviour
     * - create the gui
     */
    protected void setup() {
        myName = this.getLocalName();

        AgentServicesTools.register(this, "vote", "participant");

        MessageTemplate model = MessageTemplate.MatchConversationId("voteNo1");

        ContractNetResponder comportementVote = new ContractNetResponder(this, model) {

            //function triggered by a PROPOSE msg : send back the ranking
            @Override
            protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
//                println(myName + " ~".repeat(40));
//                println(myName + " " + cfp.getSender().getLocalName() + " proposes this options: " + cfp.getContent());
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
  //              println(myName + " : I propose this ranking: " + proposition);
                return proposition;
            }

            //function triggered by a ACCEPT_PROPOSAL msg : the polling station agent  accept the vote
            //@param cfp : the initial cfp message
            //@param propose : the proposal I sent
            //@param accept : the acceptation sent by the auctioneer
            @Override
            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
    //            println(myName + " : =".repeat(15));
    //            println(myName + " : " + cfp.getSender().getLocalName() + " started a vote between " + cfp.getContent());
    //            println(myName + " : I proposed " + propose.getContent());
    //            println(myName + " : " + cfp.getSender().getLocalName() + " has analyzed the votes and sent the result:  " + accept.getContent());
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
                //                println(myName + " : =".repeat(10));
                //                println(myName + " : VOTE REJECTED");
                //                println(myName + " : " + cfp.getSender().getLocalName() + " started a vote between " + cfp.getContent());
                //                println(myName + " : I proposed " + propose.getContent());
                //                println(myName + " : " + cfp.getSender().getLocalName() + " refused ! with this message: " + reject.getContent());
            }


        };

        addBehaviour(comportementVote);

    }

    //before leaving, the agent unsubscribe from its services
    @Override
    public void takeDown() {
        AgentServicesTools.deregisterAll(this);
//        System.err.println(this.getLocalName() + ", I leave the platform...");
    }

}
