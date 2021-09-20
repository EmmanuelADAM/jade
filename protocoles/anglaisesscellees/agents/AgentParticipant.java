package encheres.anglaisesscellees.agents;


import encheres.anglaisesscellees.gui.SimpleWindow4Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetResponder;

import java.util.Random;



/**
 * agent qui attend un message à partir du protocole CFP, prépare la réponse et la retourne
 * @author eadam
 */
@SuppressWarnings("serial")
public class AgentParticipant extends AgentWindowed {

    /**ajout du suivi de protocole AchieveRE*/
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.println("Hello! Agent  " +  getLocalName() + " is ready, my address is " + this.getAID().getName());

        AgentToolsEA.register(this,"enchere", "participant");
        Random hasard = new Random();


        MessageTemplate model = MessageTemplate.MatchConversationId("echereNo1");

        ContractNetResponder encherissement = new ContractNetResponder(this, model) {

            /**fonction lancee a la reception d'un appel d'offre*/
            @Override
            protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
                ACLMessage reponse = cfp.createReply();
                int offre = hasard.nextInt(0, 100);
                if(offre<33){
                    reponse.setPerformative(ACLMessage.REFUSE);
                }
                else {
                    println(String.format("je propose %d pour acheter %s a l'agent : %s", offre, cfp.getContent(), cfp.getSender().getLocalName()));
                    reponse.setPerformative(ACLMessage.PROPOSE);
                    reponse.setContent(String.valueOf(offre));
                }
                return reponse;
            }

            /**fonction lancee a la reception d'une acceptation de la proposition*/
            @Override
            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
                println("=".repeat(10));
                println(cfp.getSender().getLocalName() + " a lance une enchere pour " + cfp.getContent());
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
                println("OFFRE REJETEE");
                println(cfp.getSender().getLocalName() + " a lance une enchere pour " + cfp.getContent());
                println(" j'ai propose " + propose.getContent());
                println(cfp.getSender().getLocalName() + " a refuse ! avec ce message " + reject.getContent());
            }



        };

        addBehaviour(encherissement);

    }

    @Override
    public void takeDown()
    {
        window.println("je pars et je me desinscris...");
        try { DFService.deregister(this); }
        catch (FIPAException fe) { fe.printStackTrace(); }
    }
}
