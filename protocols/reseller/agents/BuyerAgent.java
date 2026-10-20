package protocols.reseller.agents;

import jade.core.AgentServicesTools;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPANames;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

import java.awt.*;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static protocols.reseller.agents.SellerAgent.reply;

/**
 * A buyer : it launches a call for proposals to the sellers of the market and accepts the cheapest croissant.
 * A first purchase 2 s after the start, then one at each click on the button.
 *
 * @author eadam
 */
public class BuyerAgent extends AgentWindowed {

    @Override
    protected void setup() {
        window = new SimpleWindow4Agent(getLocalName(), this);
        window.setBackgroundTextColor(Color.CYAN);
        window.setButtonActivated(true);
        println("Hello! I want a croissant (click to buy another one)");
        addBehaviour(new WakerBehaviour(this, 2000, a -> buy("croissant")));
    }

    private void buy(String item) {
        var cfp = new ACLMessage(ACLMessage.CFP);
        cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        cfp.addReceivers(AgentServicesTools.searchAgents(this, "market", item));
        cfp.setContent(item);
        cfp.setReplyByDate(new Date(System.currentTimeMillis() + 3000)); // the reseller needs time to buy first
        println("-".repeat(30));
        println("CFP : " + item);

        addBehaviour(new ContractNetInitiator(this, cfp) {
            /** accepts the cheapest proposal, refuses the others */
            @Override
            protected void handleAllResponses(List<ACLMessage> responses, List<ACLMessage> acceptances) {
                var proposals = responses.stream().filter(r -> r.getPerformative() == ACLMessage.PROPOSE).toList();
                proposals.forEach(p -> println("  " + p.getSender().getLocalName() + " : " + p.getContent() + " €"));
                if (proposals.isEmpty()) return;
                ACLMessage best = proposals.stream().min(Comparator.comparingDouble(p -> Double.parseDouble(p.getContent()))).get();
                proposals.forEach(p -> acceptances.add(
                        reply(p, p == best ? ACLMessage.ACCEPT_PROPOSAL : ACLMessage.REJECT_PROPOSAL, "")));
            }

            @Override
            protected void handleInform(ACLMessage inform) {
                println(inform.getSender().getLocalName() + " confirms : I have my croissant");
            }
        });
    }

    @Override
    protected void onGuiEvent(GuiEvent ev) {
        buy("croissant");
    }
}
