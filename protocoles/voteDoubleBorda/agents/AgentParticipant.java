package protocoles.voteDoubleBorda.agents;


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
 * agent qui attend un message à partir du protocole CFP, prépare la réponse et la retourne
 *
 * @author eadam
 */
public class AgentParticipant extends AgentWindowed {

    /**
     * ajout du suivi de protocole AchieveRE
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());

        AgentServicesTools.register(this, "vote", "participant");

        MessageTemplate model = MessageTemplate.MatchConversationId("voteNo1");

        ContractNetResponder comportementVote = new ContractNetResponder(this, model) {

            /**fonction lancee a la reception d'un appel d'offre*/
            @Override
            protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
                println("~".repeat(40));
                println(cfp.getSender().getLocalName() + " propose les choix : " + cfp.getContent());
                ACLMessage reponse = cfp.createReply();
                reponse.setPerformative(ACLMessage.PROPOSE);
                String choix = faireSonChoix(cfp.getContent());
                reponse.setContent(choix);
                return reponse;
            }

            /**le participant recoit une liste de propositions sous la forme option1,option2,option3,option4,.....
             * il retourne son choix en ordonnant les options et en donnant leurs positions
             * @param offres liste de propositions sous la forme option1,option2,option3,option4
             * @return choix ordonne sous la forme option2_1,option4_2,option3_3,option1_4
             * */
            private String faireSonChoix(String offres) {
                ArrayList<String> choix = new ArrayList<>(List.of(offres.split(",")));
                Collections.shuffle(choix);
                StringBuilder sb = new StringBuilder();
                String pref = ">";//pour eviter de recreer une chaine ',' a chaque vote
                for (String s : choix) sb.append(s).append(pref);
                String proposition  = sb.substring(0, sb.length()-1);
                println("j'ai propose ceci " + proposition);
                return proposition;
            }

            /**fonction lancee a la reception d'une acceptation de la proposition*/
            @Override
            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
                println("=".repeat(10));
                println(cfp.getSender().getLocalName() + " a lance un vote pour " + cfp.getContent());
                println(" j'ai propose " + propose.getContent());
                println(cfp.getSender().getLocalName() + " a accepte avec ce message " + accept.getContent());
                ACLMessage msg = accept.createReply();
                msg.setPerformative(ACLMessage.INFORM);
                msg.setContent("ok !");
                return msg;
            }

            /**prise en compte du refus*/
            @Override
            protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                println("=".repeat(10));
                println("VOTE REJETE");
                println(cfp.getSender().getLocalName() + " a lance un vote pour " + cfp.getContent());
                println(" j'ai propose " + propose.getContent());
                println(cfp.getSender().getLocalName() + " a refuse ! avec ce message " + reject.getContent());
            }


        };

        addBehaviour(comportementVote);

    }

    @Override
    public void takeDown() {
        AgentServicesTools.deregisterAll(this);
        System.err.println("moi " + this.getLocalName() + ", je quitte la plateforme...");
    }

}
