package protocols.voteDoubleBorda.agents;


import jade.core.AID;
import jade.core.AgentServicesTools;
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
 * classe for the polling station agent that launch a call for vote via  ContractNet
 *
 * @author eadam
 */
public class PollingStationAgent extends AgentWindowed {

    /**
     * ajout du suivi de protocole AchieveRE
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setButtonActivated(true);
        window.setBackgroundTextColor(Color.CYAN);
    }

    /**
     * add a ContractNet protocol to launch a vote
     */
    private void createVote(String id, String objet) {

        println("_/ \\".repeat(20));
        println("/ \\_".repeat(20));
        println("-> start a vote for these options " + objet);
        HashMap<String, Integer> votes = new HashMap<>();
        HashMap<String, Integer> lastPosition = new HashMap<>();
        for (Restaurant r : Restaurant.values()) {
            votes.put(r.toString(), 0);
            lastPosition.put(r.toString(), 0);
        }
        println("-".repeat(40));

        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setConversationId(id);
        msg.setContent(objet);

        var adresses = AgentServicesTools.searchAgents(this, "vote", "participant");
        msg.addReceivers(adresses);
        println("participant found : " + Arrays.stream(adresses).map(AID::getLocalName).toList().toString());
        println("-".repeat(40));

        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));


        ContractNetInitiator init = new ContractNetInitiator(this, msg) {
            /**fonction lancee a chaque proposition*/
            @Override
            public void handlePropose(ACLMessage propose, List<ACLMessage> acceptations) {
                println("agent %s proposes %s ".formatted(propose.getSender().getLocalName(), propose.getContent()));
            }

            /**fonction lancee quand un participant refuse de continuer*/
            @Override
            protected void handleRefuse(ACLMessage refuse) {
                println("REFUSZ ! I recveived a refuse from " + refuse.getSender().getLocalName());
            }

            /**fonction lancee quand toutes les reponses ont ete recues*/
            @Override
            protected void handleAllResponses(List<ACLMessage> leursVotes, List<ACLMessage> mesRetours) {
                ArrayList<ACLMessage> listeVotes = new ArrayList<>(leursVotes);
                //on ne garde que les propositions
                listeVotes.removeIf(v -> v.getPerformative() != ACLMessage.PROPOSE);

                //par defaut, on accepte tout vote
                listeVotes.forEach(v -> {
                    var ret = v.createReply();
                    ret.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    mesRetours.add(ret);
                });

                var selectedResto = getRestoElected(leursVotes, Restaurant.values().length);

                //gestion des ex-aequo : on recompte sans prendre en compte les autres restos
                if (selectedResto.size() > 1) {
                    println("-".repeat(30));
                    println("we try to separate the ex-aequo : " + selectedResto);
                    println("Retry Borda counting by removing the other options...");
                    println("-".repeat(20));
                    votes.clear();
                    for (String r : selectedResto) votes.put(r, 0);
                    selectedResto = getRestoElected(leursVotes, selectedResto.size());

                }

                //gestion des ex-aequo : cette fois on r�alise un tirage al�atoire...
                if (selectedResto.size() > 1) {
                    println("-".repeat(30));
                    println("Ex-aequos again, we try to separate them with a random draw : " + selectedResto);
                    println("-".repeat(20));
                    var finalChoice = selectedResto.stream().findAny();
                    selectedResto.clear();
                    selectedResto.add(finalChoice.orElse("-nothing-"));
                }

                //placement du nom des elus dans les messages � retourner
                String strSelectedOption = selectedResto.toString();
                strSelectedOption = strSelectedOption.substring(1, strSelectedOption.length() - 1);
                for (ACLMessage m : mesRetours)
                    m.setContent(strSelectedOption);

                println("~".repeat(20));
            }

            /**
             * lit les messages de preferences exprim�es (a>c>d ....)
             * affecte les points d�croissant si les choix existe dans la map(string, int) votes
             *
             * @param listeVotes liste de messages contenant les votes exprimes
             * @param maxPoints points maxi attribu� au 1er choix
             * @return liste des options (restos) ayant le plus de points */
            private List<String> getRestoElected(List<ACLMessage> listeVotes, int maxPoints) {

                for (ACLMessage vote : listeVotes) {
                    var content = vote.getContent();
                    //analyse du contenu sous la forme resto1>resto2>,...
                    String[] sesVotes = content.split(">");
                    int[] points = {maxPoints};
                    for (String s : sesVotes) {
                        //on ajoute la valeur du vote de chaque resto dans la map des votes
                        var r = votes.computeIfPresent(s, (k, v) -> v + points[0]);
                        if (r != null) points[0]--;
                    }
                }

                println("-".repeat(40));
                //affichage du total des votes
                votes.forEach((k, v) -> println(k + " obtained " + v + " points"));
                //r�cup�ration du plus haut score
                final int[] highScore = {Collections.max(votes.values())};
                //r�cuperation des elus
                final var selectedResto = new ArrayList<String>();
                votes.forEach((k, v) -> {
                    if (v == highScore[0]) selectedResto.add(k);
                });
                println("-".repeat(40));
                println("[[[ Voting result " + selectedResto + "]]]");
                println("-".repeat(40));
                return selectedResto;
            }

            /**fonction lancee quand le meilleur offreur confirme son intention*/
            @Override
            protected void handleInform(ACLMessage inform) {
                println("vote has been accepted by " + inform.getSender().getLocalName());
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
