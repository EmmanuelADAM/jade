package protocoles.voteBorda.agents;


import jade.core.AID;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPANames;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import protocoles.voteBorda.gui.SimpleWindow4Agent;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * classe d'un agent qui soumet un appel au vote a d'autres agents  par le protocole ContractNet
 * @author eadam
 */
public class AgentBureauVote extends AgentWindowed {

    /**ajout du suivi de protocole AchieveRE*/
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.println("Hello! Agent  " +  getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setButtonActivated(true);
        window.setBackgroundTextColor(Color.CYAN);
    }

    /**add a ContractNet protocol to launch a vote */
    private void createVote(String id, String objet) {

        println("_/ \\".repeat(20));
        println("/ \\_".repeat(20));
        println("debut d'un vote pour les options " + objet);
        HashMap<String, Integer> votes = new HashMap<>();
        for(Resto r:Resto.values()) votes.put(r.toString(), 0);
        votes.forEach((k,v)->println("nb votes pour " + k + " = " + v));
        println("-".repeat(40));

        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setConversationId(id);
        msg.setContent(objet);

        var adresses = AgentToolsEA.searchAgents(this, "vote", "participant");
        for (AID dest : adresses) msg.addReceiver(dest);
        println("destinataires trouves : " + Arrays.stream(adresses).map(AID::getLocalName).toList().toString());
        println("-".repeat(40));

        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));


        ContractNetInitiator init = new ContractNetInitiator(this, msg) {
            /**fonction lancee a chaque proposition*/
            @Override
            public void handlePropose(ACLMessage propose, Vector v)
            {
                println("l'agent %s propose %s ".formatted(propose.getSender().getLocalName(), propose.getContent()) );
            }

            /**fonction lancee quand un participant refuse de continuer*/
            @Override
            protected void handleRefuse(ACLMessage refuse) {
                println("REFUS ! j'ai recu un refus  de " + refuse.getSender().getLocalName());
            }

            /**fonction lancee quand toutes les reponses ont ete recues*/
            @Override
            protected void handleAllResponses(Vector leursVotes, Vector mesRetours) {
                ArrayList<ACLMessage> listeVotes = new ArrayList<ACLMessage>(leursVotes.stream().toList());
                //on ne garde que les propositions
                listeVotes.removeIf(v->v.getPerformative()!=ACLMessage.PROPOSE);

                List<ACLMessage> retours = new ArrayList<>();

                for (ACLMessage vote : listeVotes) {
                    //par defaut, on accepte tout vote
                    var retour = vote.createReply();
                    retour.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    retours.add(retour);
                    var content = vote.getContent();
                    //analyse du contenu sous la forme resto1_valeur1,resto2_valeur2,...
                    String[] sesVotes = content.split(",");
                    for(String s:sesVotes)
                    {
                        String[] detail = s.split("_");
                        //on ajoute la valeur du vote de chaque resto dans la map des votes
                        votes.computeIfPresent(detail[0], (k,v)->v+Integer.parseInt(detail[1]));
                    }
                }

                println("-".repeat(40));
                //affichage du total des votes
                votes.forEach((k,v)->println(k+ " a obtenu "+ v + " points"));
                //récupération du plus haut score
                int highScore = Collections.max(votes.values());
                //récuperation des elus
                StringBuffer best = new StringBuffer();
                votes.forEach((k,v)->{if(v==highScore) best.append(k).append(",");});
                println("-".repeat(40));
                println("Résultat du vote " + best);
                println("-".repeat(40));

                //placement du nom des elus dans les messages à retourner
                for(ACLMessage m:retours)
                    m.setContent(best.toString());
                mesRetours.addAll(retours);

                //si ex-aequo, on relance un vote avec ceux-ci
                if((best.toString()).split(",").length>1)
                {
                    println("-".repeat(30));
                    println("un nouveau tour va etre lance ces choix : " + best);
                    println("-".repeat(30));
                    myAgent.addBehaviour(new WakerBehaviour(myAgent, 100) {
                        @Override
                        protected void onWake() {
                            createVote("voteNo1", best.toString());
                        }
                    });
                }
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


    public void launchRequest()
    {
        StringBuilder sb = new StringBuilder();
        for(Resto r:Resto.values()) sb.append(r).append(",");
        createVote("voteNo1", sb.toString());
    }

}
