package protocols.vickrey.agents;

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
 * agent qui attend un message à partir du protocole CFP, prépare la réponse et la retourne
 *
 * @author eadam
 */
@SuppressWarnings("serial")
public class ParticipantAgent extends AgentWindowed {

    /**
     * ajout du suivi de protocole AchieveRE
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        println("- ".repeat(20));

        AgentServicesTools.register(this, "auction", "participant");
        Random hasard = new Random();

        AgentServicesTools.register(this, "auction", "participantS");

        MessageTemplate model = MessageTemplate.MatchConversationId("echereNo1");

        ContractNetResponder encherissement = new ContractNetResponder(this, model) {

            /**fonction lancee a la reception d'un appel d'offre*/
            @Override
            protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
                window.setBackgroundTextColor(Color.WHITE);
                ACLMessage reponse = cfp.createReply();
                println("'%s' proposes  '%s' to the auction..".formatted(cfp.getSender().getLocalName(),
                        cfp.getContent()));
                int offre = hasard.nextInt(0, 100);
                //ici l'agent refuse 1 fois sur 3 (lorsque la valeur aleatoire offre est < a 33)
                if (offre < 33) {
                    window.setBackgroundTextColor(Color.LIGHT_GRAY);
                    println("I don't want to bid for this object.");
                    reponse.setPerformative(ACLMessage.REFUSE);
                } else {
                    println(String.format("I propose %d to buy '%s' to agent : '%s'", offre, cfp.getContent(),
                            cfp.getSender().getLocalName()));
                    reponse.setPerformative(ACLMessage.PROPOSE);
                    reponse.setContent(String.valueOf(offre));
                }
                println("-".repeat(30));
                return reponse;
            }

            /**fonction lancee a la reception d'une acceptation de la proposition*/
            @Override
            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
                window.setBackgroundTextColor(Color.YELLOW);
                println("-".repeat(30));
                println("BIDDING ACCEPTED, as a reminder : ");
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

            /**prise en compte du refus*/
            @Override
            protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                window.setBackgroundTextColor(Color.RED);
                println("-".repeat(30));
                println("BIDDING REJECTED, as a reminder : ");
                println("'%s' launched an auction for '%s'".formatted(cfp.getSender().getLocalName(),
                        cfp.getContent()));
                println(" I've proposed " + propose.getContent());
                println("'%s' has declined with this message '%s'".formatted(cfp.getSender().getLocalName(), reject.getContent()));
                println("_".repeat(40));
                println("");

            }


        };

        addBehaviour(encherissement);

    }



    @Override
    public void takeDown() {
        //on se desinscrit du service des encheres avant de partir
        AgentServicesTools.deregisterAll(this);
        System.err.println("moi " + this.getLocalName() + ", je quitte la plateforme...");
    }

}
