package protocols.sealedEnglishAuction.agents;


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
 * class of an agent that proposes a call for proposal using the ContractNet protocol
 *
 * @author eadam
 */
public class AuctioneerAgent extends AgentWindowed {

    /**
     * setup the gui
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setButtonActivated(true);
        window.setBackgroundTextColor(Color.CYAN);
    }

    /**
     * add a ContractNet protocol to launch a call for proposal
     */
    private void createOffer(String id, String object) {

        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setConversationId(id);
        msg.setContent(object);

        var adresses = AgentServicesTools.searchAgents(this, "auction", "bidder");
        msg.addReceivers(adresses);
        println("(o)".repeat(30));

        println("bidders found: " + Arrays.stream(adresses).map(AID::getLocalName).toList());

        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));

        println(" I start the auction for " + msg.getContent());


        ContractNetInitiator init = new ContractNetInitiator(this, msg) {
            //function triggered by a PROPOSE msg 
            // @param propose     the received propose message
            // @param acceptances the list of ACCEPT/REJECT_PROPOSAL to be sent back.
            //                    list that can be modified here or at once when all the messages are received
            public void handlePropose(ACLMessage propose, List<ACLMessage> acceptations) {
                println(String.format("agent %s proposes %s ", propose.getSender().getLocalName(),
                        propose.getContent()));
            }

            //function triggered by a REFUSE msg 
            @Override
            protected void handleRefuse(ACLMessage refuse) {
                println("REFUSE ! I received a refuse from " + refuse.getSender().getLocalName());
            }

            //function triggered when all the responses are received (or after the waiting time)
            //@param theirOffers the list of message sent by the bidders
            //@param myAnswers the list of answers for each proposal
            @Override
            protected void handleAllResponses(List<ACLMessage> theirOffers, List<ACLMessage> myAnswers) {
                int maxi = Integer.MIN_VALUE;
                ACLMessage msgPourMeilleurOffreur = null;
                List<ACLMessage> listeOffres = new ArrayList<>(theirOffers);

                //we keep only the proposals only
                listeOffres.removeIf(msg -> msg.getPerformative() != ACLMessage.PROPOSE);
                List<ACLMessage> listeReponses = new ArrayList<>(listeOffres.size());

                StringBuilder sb = new StringBuilder("In short: \n");
                for (ACLMessage offre : listeOffres) {
                    //by default, we build a reject answer for each proposal; we go back to the best offer later
                    var retour = offre.createReply();
                    retour.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    listeReponses.add(retour);
                    int valeurProposition = Integer.parseInt(offre.getContent());
                    sb.append("\treceived this proposal from ").append(offre.getSender().getLocalName()).append(" :: ").append(valeurProposition).append("\n");
                    if (valeurProposition > maxi) {
                        maxi = valeurProposition;
                        msgPourMeilleurOffreur = retour;
                    }
                }
                sb.append("~".repeat(30)).append("\n");
                sb.append("best offer = ").append(maxi).append("\n");
                println(sb.toString());

                int finalMaxi = maxi;
                listeReponses.forEach(msg -> msg.setContent("Refused, sorry, the best offer was " + finalMaxi));
                //we go back to the msg for the best bidder that we finally accept
                if (msgPourMeilleurOffreur != null) {
                    msgPourMeilleurOffreur.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    msgPourMeilleurOffreur.setContent("SOLD for " + finalMaxi);
                }
                myAnswers.addAll(listeReponses);
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
        createOffer("auction1", "book");
    }

}
