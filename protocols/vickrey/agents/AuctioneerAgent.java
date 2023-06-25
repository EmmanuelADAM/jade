package protocols.vickrey.agents;


import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.domain.FIPANames;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * classe d'un agent qui soumet un appel d'offres a d'autres agents  par le protocole ContractNet
 *
 * @author eadam
 */
public class AuctioneerAgent extends AgentWindowed {

    /**
     * ajout du suivi de protocole AchieveRE
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setButtonActivated(true);
        window.setBackgroundTextColor(Color.CYAN);
    }

    /**
     * add a ContractNet protocol to launch a...
     */
    private void createOffer(String id, String objet) {

        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setConversationId(id);
        msg.setContent(objet);

        var adresses = AgentServicesTools.searchAgents(this, "auction", "participant");
        msg.addReceivers(adresses);
        println("(o)".repeat(30));

        println("bidders found: " + Arrays.stream(adresses).map(AID::getLocalName).toList().toString());

        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));

        println("I launch an auction for  " + msg.getContent());


        ContractNetInitiator init = new ContractNetInitiator(this, msg) {
            /**fonction lancee a chaque proposition*/
            @Override
            public void handlePropose(ACLMessage propose, List<ACLMessage> acceptations) {
                println(String.format("l'agent %s propose %s ", propose.getSender().getLocalName(), propose.getContent()));
            }

            /**fonction lancee quand un participant refuse de continuer*/
            @Override
            protected void handleRefuse(ACLMessage refuse) {
                println("REFUSE ! I receive a refuse from " + refuse.getSender().getLocalName());
            }

            /**fonction lancee quand toutes les reponses ont ete recues*/
            @Override
            protected void handleAllResponses(List<ACLMessage> leursOffres, List<ACLMessage> mesRetours) {
                int maxi = Integer.MIN_VALUE;
                int presqueMaxi = Integer.MIN_VALUE;
                ACLMessage msgPourMeilleurOffreur = null;
                List<ACLMessage> listeOffres = new ArrayList<>(leursOffres);

                //we keep only the proposals
                listeOffres.removeIf(msg -> msg.getPerformative() != ACLMessage.PROPOSE);
                List<ACLMessage> listeReponses = new ArrayList<>(listeOffres.size());

                StringBuilder sb = new StringBuilder("To summarize: \n");
                for (ACLMessage offre : listeOffres) {
                    //by default, we build a reject answer for each proposal; we go back to the best offer later
                    var retour = offre.createReply();
                    retour.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    listeReponses.add(retour);
                    int valeurProposition = Integer.parseInt(offre.getContent());
                    sb.append("\treceived this proposal from  ").append(offre.getSender().getLocalName()).append(" :: ").append(valeurProposition).append("\n");
                    if (valeurProposition > presqueMaxi) {
                        if (valeurProposition > maxi) {
                            presqueMaxi = maxi;
                            maxi = valeurProposition;
                            msgPourMeilleurOffreur = retour;
                        }
                        else presqueMaxi = valeurProposition;
                    }
                }
                sb.append("~".repeat(30)).append("\n");
                sb.append("best bidding = ").append(maxi).append("\n");
                sb.append("nearest best bidding = ").append(presqueMaxi).append("\n");
                println(sb.toString());

                int finalMaxi = maxi;
                listeReponses.forEach(msg -> msg.setContent("Refuse, sorry, your bidding as not been kept..."));
                //on avait garde un pointeur vers le message envoye a la meilleure offre, qu'un accepte finalement
                if (msgPourMeilleurOffreur != null) {
                    msgPourMeilleurOffreur.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    msgPourMeilleurOffreur.setContent("**Auctioned-sold** for " + presqueMaxi);
                }
                mesRetours.addAll(listeReponses);
            }


            //function triggered by a INFORM msg : the best bidder confirm the sale
            @Override
            protected void handleInform(ACLMessage inform) {
                println("_".repeat(30));
                println("Sale confirmed with  " + inform.getSender().getLocalName());
                println("_".repeat(30));
                println("");
            }

            @Override
            //function triggered by a FAILURE msg : the best bidder cancel the sale
            protected void handleFailure(ACLMessage failure) {
                println("_".repeat(30));
                println("PB : Sale cancelled with  " + failure.getSender().getLocalName());
                println("_".repeat(30));
                println("");
            }
        };

        addBehaviour(init);

    }

    @Override
    protected void onGuiEvent(GuiEvent arg0) {
        launchRequest();
    }


    public void launchRequest() {
        createOffer("echereNo1", "livre");
    }

}
