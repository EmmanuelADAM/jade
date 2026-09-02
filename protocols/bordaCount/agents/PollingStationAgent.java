package protocols.bordaCount.agents;


import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPANames;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * class of an agent that proposes a call for proposal using the ContractNet protocol
 *
 * @author eadam
 */
public class PollingStationAgent extends AgentWindowed {

    /**
     * setup the gui
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setButtonActivated(true);
        window.setBackgroundTextColor(Color.CYAN);
    }

    /**
     * add a ContractNet protocol to launch a call for proposal for each vote
     */
    private void createVote(String id, String object) {

        println("_/ \\".repeat(20));
        println("/ \\_".repeat(20));
        println("Strat a vote for this options " + object);
        HashMap<String, Integer> votes = new HashMap<>();
        for (Restaurant r : Restaurant.values()) votes.put(r.toString(), 0);
        votes.forEach((k, v) -> println("nb votes for " + k + " = " + v));
        println("-".repeat(40));

        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setConversationId(id);
        msg.setContent(object);

        var adresses = AgentServicesTools.searchAgents(this, "vote", "participant");
        msg.addReceivers(adresses);
        println("Participants found : " + Arrays.stream(adresses).map(AID::getLocalName).toList());
        println("-".repeat(40));

        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));


        ContractNetInitiator init = new ContractNetInitiator(this, msg) {
            //function triggered by a PROPOSE msg
            // @param propose     the received propose message
            // @param acceptances the list of ACCEPT/REJECT_PROPOSAL to be sent back.
            //                    list that can be modified here or at once when all the messages are received
            @Override
            public void handlePropose(ACLMessage propose, List<ACLMessage> acceptations) {
                println("Agent %s proposes %s ".formatted(propose.getSender().getLocalName(), propose.getContent()));
            }

            //function triggered by a REFUSE msg
            @Override
            protected void handleRefuse(ACLMessage refuse) {
                println("REFUSE ! I received a refuse from " + refuse.getSender().getLocalName());
            }

            //function triggered when all the responses are received (or after the waiting time)
            //@param theirVotes the list of message sent by the voters
            //@param myAnswers the list of answers for each voter
            @Override
            protected void handleAllResponses(List<ACLMessage> theirVotes, List<ACLMessage> myAnswers) {
                //we keep only the proposals, and tally each one into the votes map
                List<ACLMessage> proposals = theirVotes.stream()
                        .filter(v -> v.getPerformative() == ACLMessage.PROPOSE)
                        .toList();
                for (ACLMessage vote : proposals) addBordaPoints(vote.getContent());

                println("-".repeat(40));
                votes.forEach((k, v) -> println(k + " obtained " + v + " points"));
                List<String> winners = winningOptions();
                String winnersList = String.join(",", winners);
                println("-".repeat(40));
                println("Result of the vote :  " + winnersList);
                println("-".repeat(40));

                //accept every proposal, telling each voter the winning option(s)
                for (ACLMessage vote : proposals) {
                    ACLMessage answer = vote.createReply();
                    answer.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    answer.setContent(winnersList);
                    myAnswers.add(answer);
                }

                //If tied, we relaunch a vote with them
                if (winners.size() > 1) {
                    println("-".repeat(30));
                    println("A new round will be launched these choices : " + winnersList);
                    println("-".repeat(30));
                    myAgent.addBehaviour(new WakerBehaviour(myAgent, 100) {
                        @Override
                        protected void onWake() {
                            createVote("voteNo1", winnersList);
                        }
                    });
                }
            }

            //reads one voter's ranking ("resto1>resto2>...") and adds its Borda points into the votes map:
            //n points to the 1st choice, n-1 to the 2nd, ..., 1 to the last
            private void addBordaPoints(String ranking) {
                String[] ranked = ranking.split(">");
                int points = ranked.length;
                for (String option : ranked) {
                    if (votes.containsKey(option)) {
                        votes.put(option, votes.get(option) + points);
                    }
                    points--;
                }
            }

            //returns the option(s) with the highest score in the votes map (more than one if tied)
            private List<String> winningOptions() {
                int highScore = Collections.max(votes.values());
                return votes.entrySet().stream()
                        .filter(e -> e.getValue() == highScore)
                        .map(Map.Entry::getKey)
                        .toList();
            }

            //function triggered by a INFORM msg : a voter accept the result
            // @Override
            protected void handleInform(ACLMessage inform) {
                println("the vote is accepted by " + inform.getSender().getLocalName());
            }


        };

        addBehaviour(init);

    }

    @Override
    protected void onGuiEvent(GuiEvent arg0) {
        launchRequest();
    }


    public void launchRequest() {
        StringBuilder sb = new StringBuilder();
        for (Restaurant r : Restaurant.values()) sb.append(r).append(",");
        createVote("voteNo1", sb.toString());
    }

}
