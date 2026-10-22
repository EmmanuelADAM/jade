package protocols.nestedProtocol.agents;

import jade.core.AgentServicesTools;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPANames;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetResponder;
import jade.proto.SSContractNetResponder;
import jade.proto.SSResponderDispatcher;

import java.awt.*;

/**
 * A seller of croissants (on the market, or a wholesaler).
 * Arguments : its price, and its service : "market" (known by the buyer) or "wholesale" (known only by the reseller).
 *
 * @author eadam
 */
public class SellerAgent extends AgentWindowed {

    @Override
    protected void setup() {
        String price = (String) getArguments()[0];
        String service = (String) getArguments()[1];
        window = new SimpleWindow4Agent(getLocalName(), this);
        window.setBackgroundTextColor(service.equals("market") ? Color.LIGHT_GRAY : Color.PINK);
        println("Hello! I sell croissants for " + price + " € (" + service + ")");
        AgentServicesTools.register(this, service, "croissant");

        // one session per CFP : this seller may be asked by the buyer AND, at the same time, by the reseller
        addBehaviour(new SSResponderDispatcher(this, ContractNetResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET)) {
            @Override
            protected Behaviour createResponder(ACLMessage cfp) {
                return new SSContractNetResponder(myAgent, cfp) {
                    @Override
                    protected ACLMessage handleCfp(ACLMessage cfp) {
                        println(cfp.getSender().getLocalName() + " asks a " + cfp.getContent() + " : I propose " + price + " €");
                        return reply(cfp, ACLMessage.PROPOSE, price);
                    }

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
            }
        });
    }

    /** a reply to a message, with a performative and a content */
    static ACLMessage reply(ACLMessage msg, int performative, String content) {
        ACLMessage answer = msg.createReply();
        answer.setPerformative(performative);
        answer.setContent(content);
        return answer;
    }

    @Override
    public void takeDown() {
        AgentServicesTools.deregisterAll(this);
        window.dispose();
    }
}
