package protocoles.anglaisesscellees.agents;


import jade.core.AgentServicesTools;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAException;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
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
        println("Hello! Agent  " +  getLocalName() + " is ready, my address is " + this.getAID().getName());
        println("- ".repeat(20));

        AgentServicesTools.register(this,"enchere", "participant");
        Random hasard = new Random();


        MessageTemplate model = MessageTemplate.MatchConversationId("echereNo1");

        ContractNetResponder encherissement = new ContractNetResponder(this, model) {

            /**fonction lancee a la reception d'un appel d'offre*/
            @Override
            protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
                ACLMessage reponse = cfp.createReply();
                println("'%s' propose l'objet '%s' a la vente..".formatted(cfp.getSender().getLocalName(),cfp.getContent()));
                int offre = hasard.nextInt(0, 100);
                //ici l'agent refuse 1 fois sur 3 (lorsque la valeur aleatoire offre est < a 33)
                if(offre<33){
                    println("je n'ai pas envie d'encherir pour cet objet.");
                    reponse.setPerformative(ACLMessage.REFUSE);
                }
                else {
                    println(String.format("je propose %d pour acheter '%s' a l'agent : '%s'", offre, cfp.getContent(), cfp.getSender().getLocalName()));
                    reponse.setPerformative(ACLMessage.PROPOSE);
                    reponse.setContent(String.valueOf(offre));
                }
                println("-".repeat(30));
                return reponse;
            }

            /**fonction lancee a la reception d'une acceptation de la proposition*/
            @Override
            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
                println("-".repeat(30));
                println("OFFRE ACCEPTEE, pour rappel : ");
                println("'%s' a lance une enchere pour '%s'".formatted(cfp.getSender().getLocalName(), cfp.getContent()));
                println(" j'ai propose " + propose.getContent());
                println("'%s' a accepte avec ce message '%s'".formatted(cfp.getSender().getLocalName(), accept.getContent()));
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
                println("-".repeat(30));
                println("OFFRE REJETEE, pour rappel : ");
                println("'%s' a lance une enchere pour '%s'".formatted(cfp.getSender().getLocalName(), cfp.getContent()));
                println(" j'ai propose " + propose.getContent());
                println("'%s' a decline avec ce message '%s'".formatted(cfp.getSender().getLocalName(), reject.getContent()));
                println("_".repeat(40));
                println("");

            }



        };

        addBehaviour(encherissement);

    }
    @Override
    public void onGuiEvent(GuiEvent event)
    {
        if(event.getType()==SimpleWindow4Agent.QUIT_EVENT) doDelete();
    }

    @Override
    public void takeDown()
    {
        AgentServicesTools.deregisterAll(this);
        System.err.println("moi " + this.getLocalName() + ", je quitte la plateforme...");
    }

}
