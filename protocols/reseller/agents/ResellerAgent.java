package protocols.reseller.agents;

import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.domain.FIPANames;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import jade.proto.ContractNetResponder;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static protocols.reseller.agents.SellerAgent.reply;

/**
 * A reseller : it is on the market, but it has no croissant.
 * When the buyer asks a croissant, BEFORE answering, it launches its own call for proposals
 * to the wholesaler and to the other sellers, buys the cheapest one, and proposes it to the buyer with a margin.
 * <p>
 * The inner Contract Net (reseller -> suppliers) is NESTED in the outer one (buyer -> reseller) :
 * it is registered as the step "handle CFP" of the responder. Both behaviours share their maps of messages :
 * the inner Contract Net reads the CFP of the buyer ({@code CFP_KEY}) and writes the answer to the buyer ({@code REPLY_KEY}).
 * Argument : the margin (in €).
 *
 * @author eadam
 */
public class ResellerAgent extends AgentWindowed {
    private double margin;
    /** price of the croissant bought to the supplier */
    private double purchasePrice;

    @Override
    protected void setup() {
        margin = Double.parseDouble((String) getArguments()[0]);
        window = new SimpleWindow4Agent(getLocalName(), this);
        window.setBackgroundTextColor(Color.YELLOW);
        println("Hello! I resell croissants with a margin of " + margin + " €");
        AgentServicesTools.register(this, "market", "croissant");

        // 1. the OUTER protocol : the call for proposals of the buyer
        var responder = new ContractNetResponder(this, ContractNetResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET)) {
            @Override
            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
                println("accepted by " + accept.getSender().getLocalName() + " : I confirm");
                return reply(accept, ACLMessage.INFORM, "confirm");
            }

            @Override
            protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                println("refused by " + reject.getSender().getLocalName());
            }
        };

        // 2. the INNER protocol : to answer the CFP of the buyer, the reseller first buys to its suppliers
        responder.registerHandleCfp(new ContractNetInitiator(this, new ACLMessage(ACLMessage.CFP)) {

            /** reads the CFP of the buyer in the SHARED maps, and asks the same item to the suppliers */
            @Override
            protected List<ACLMessage> prepareCfps(ACLMessage ignored) {
                ACLMessage buyerCfp = getMapMessages().get(responder.CFP_KEY);
                println(buyerCfp.getSender().getLocalName() + " asks a " + buyerCfp.getContent() + " : I ask my suppliers");
                var cfp = new ACLMessage(ACLMessage.CFP);
                cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                cfp.addReceivers(suppliers());
                cfp.setContent(buyerCfp.getContent());
                cfp.setReplyByDate(new Date(System.currentTimeMillis() + 1000));
                return List.of(cfp);
            }

            /** accepts the cheapest proposal, refuses the others */
            @Override
            protected void handleAllResponses(List<ACLMessage> responses, List<ACLMessage> acceptances) {
                var proposals = responses.stream().filter(r -> r.getPerformative() == ACLMessage.PROPOSE).toList();
                if (proposals.isEmpty()) {
                    getMapMessages().put(responder.REPLY_KEY, reply(getMapMessages().get(responder.CFP_KEY), ACLMessage.REFUSE, "none"));
                    return;
                }
                ACLMessage best = proposals.stream().min(Comparator.comparingDouble(p -> Double.parseDouble(p.getContent()))).get();
                purchasePrice = Double.parseDouble(best.getContent());
                proposals.forEach(p -> acceptances.add(
                        reply(p, p == best ? ACLMessage.ACCEPT_PROPOSAL : ACLMessage.REJECT_PROPOSAL, "")));
            }

            /** the supplier confirms : writes the proposal to the buyer in the SHARED maps (the responder sends it) */
            @Override
            protected void handleInform(ACLMessage inform) {
                String price = String.valueOf(purchasePrice + margin);
                println("bought to " + inform.getSender().getLocalName() + " for " + purchasePrice + " € : I propose " + price + " €");
                ACLMessage buyerCfp = getMapMessages().get(responder.CFP_KEY);
                getMapMessages().put(responder.REPLY_KEY, reply(buyerCfp, ACLMessage.PROPOSE, price));
            }
        });

        addBehaviour(responder);
    }

    /** the wholesalers and the other sellers of the market */
    private AID[] suppliers() {
        return Stream.concat(Arrays.stream(AgentServicesTools.searchAgents(this, "wholesale", "croissant")),
                        Arrays.stream(AgentServicesTools.searchAgents(this, "market", "croissant")))
                .filter(aid -> !aid.equals(getAID()))
                .toArray(AID[]::new);
    }

    @Override
    public void takeDown() {
        AgentServicesTools.deregisterAll(this);
        window.dispose();
    }
}
