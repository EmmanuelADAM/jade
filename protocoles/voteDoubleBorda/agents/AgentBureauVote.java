package protocoles.voteDoubleBorda.agents;


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
 * classe d'un agent qui soumet un appel au vote a d'autres agents  par le protocole ContractNet
 *
 * @author eadam
 */
public class AgentBureauVote extends AgentWindowed {

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
        println("debut d'un vote pour les options " + objet);
        HashMap<String, Integer> votes = new HashMap<>();
        HashMap<String, Integer> lastPosition = new HashMap<>();
        for (Resto r : Resto.values()) {
            votes.put(r.toString(), 0);
            lastPosition.put(r.toString(), 0);
        }
        println("-".repeat(40));

        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setConversationId(id);
        msg.setContent(objet);

        var adresses = AgentServicesTools.searchAgents(this, "vote", "participant");
        msg.addReceivers(adresses);
        println("destinataires trouves : " + Arrays.stream(adresses).map(AID::getLocalName).toList().toString());
        println("-".repeat(40));

        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));


        ContractNetInitiator init = new ContractNetInitiator(this, msg) {
            /**fonction lancee a chaque proposition*/
            @Override
            public void handlePropose(ACLMessage propose, List<ACLMessage> acceptations) {
                println("l'agent %s propose %s ".formatted(propose.getSender().getLocalName(), propose.getContent()));
            }

            /**fonction lancee quand un participant refuse de continuer*/
            @Override
            protected void handleRefuse(ACLMessage refuse) {
                println("REFUS ! j'ai recu un refus  de " + refuse.getSender().getLocalName());
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

                var selectedResto = getRestoElected(leursVotes, Resto.values().length);

                //gestion des ex-aequo : on recompte sans prendre en compte les autres restos
                if (selectedResto.size() > 1) {
                    println("-".repeat(30));
                    println("on tente de séparer les ex-aequos : " + selectedResto);
                    println("On recompte par Borda en ôtant les autres options des choix envoyés...");
                    println("-".repeat(20));
                    votes.clear();
                    for (String r : selectedResto) votes.put(r, 0);
                    selectedResto = getRestoElected(leursVotes, selectedResto.size());

                }

                //gestion des ex-aequo : cette fois on réalise un tirage aléatoire...
                if (selectedResto.size() > 1) {
                    println("-".repeat(30));
                    println("on tente de séparer aléatoirement les derniers ex-aequos : " + selectedResto);
                    println("-".repeat(20));
                    var finalChoice = selectedResto.stream().findAny();
                    selectedResto.clear();
                    selectedResto.add(finalChoice.orElse("-rien-"));
                }

                //placement du nom des elus dans les messages à retourner
                String strSelectedOption = selectedResto.toString();
                strSelectedOption = strSelectedOption.substring(1, strSelectedOption.length() - 1);
                for (ACLMessage m : mesRetours)
                    m.setContent(strSelectedOption);

                println("~".repeat(20));
            }

            /**
             * lit les messages de preferences exprimées (a>c>d ....)
             * affecte les points décroissant si les choix existe dans la map(string, int) votes
             *
             * @param listeVotes liste de messages contenant les votes exprimes
             * @param maxPoints points maxi attribué au 1er choix
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
                votes.forEach((k, v) -> println(k + " a obtenu " + v + " points"));
                //récupération du plus haut score
                final int[] highScore = {Collections.max(votes.values())};
                //récuperation des elus
                final var selectedResto = new ArrayList<String>();
                votes.forEach((k, v) -> {
                    if (v == highScore[0]) selectedResto.add(k);
                });
                println("-".repeat(40));
                println("Résultat du vote " + selectedResto);
                println("-".repeat(40));
                return selectedResto;
            }

            /**fonction lancee quand le meilleur offreur confirme son intention*/
            @Override
            protected void handleInform(ACLMessage inform) {
                println("le vote a bien ete accepte par " + inform.getSender().getLocalName());
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
        for (Resto r : Resto.values()) sb.append(r).append(",");
        createVote("voteNo1", sb.toString());
    }

}
