package protocoles.anglaisesscellees.agents;


import protocoles.anglaisesscellees.gui.SimpleWindow4Agent;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

import java.awt.*;
import java.util.*;
import java.util.List;


/**
 * classe d'un agent qui soumet un appel d'offres a d'autres agents  par le protocole ContractNet
 * @author eadam
 */
public class AgentCommissairePriseur extends AgentWindowed {

    /**ajout du suivi de protocole AchieveRE*/
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent  " +  getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setButtonActivated(true);
        window.setBackgroundTextColor(Color.CYAN);
    }

    /**add a ContractNet protocol to launch a... */
    private void createOffer(String id, String objet) {

        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setConversationId(id);
        msg.setContent(objet);

        var adresses = AgentToolsEA.searchAgents(this, "enchere", "participant");
        for (AID dest : adresses) msg.addReceiver(dest);
        println("(o)".repeat(30));

        println("destinataires trouves : " + Arrays.stream(adresses).map(AID::getLocalName).toList().toString());

        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));

        println("je lance les encheres pour " + msg.getContent());


        ContractNetInitiator init = new ContractNetInitiator(this, msg) {
            /**fonction lancee a chaque proposition*/
            @Override
            public void handlePropose(ACLMessage propose, Vector v)
            {
                println(String.format("l'agent %s propose %s ", propose.getSender().getLocalName(), propose.getContent()) );
            }

            /**fonction lancee quand un participant refuse de continuer*/
            @Override
            protected void handleRefuse(ACLMessage refuse) {
                println("REFUS ! j'ai recu un refus  de " + refuse.getSender().getLocalName());
            }

            /**fonction lancee quand toutes les reponses ont ete recues*/
            @Override
            protected void handleAllResponses(Vector leursOffres, Vector mesRetours) {
                int maxi = Integer.MIN_VALUE;
                ACLMessage msgPourMeilleurOffreur = null;
                List<ACLMessage> listeOffres = new ArrayList<>((List<ACLMessage>)leursOffres.stream().toList());

                //on ne garde que les propositions
                listeOffres.removeIf(msg->msg.getPerformative()!=ACLMessage.PROPOSE);
                List<ACLMessage> listeReponses = new ArrayList<>(listeOffres.size());

                StringBuilder sb = new StringBuilder("En resume : \n");
                for (ACLMessage offre : listeOffres) {
                    //par défaut on rejette, on gardera la meilleure ensuite
                    var retour = offre.createReply();
                    retour.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    listeReponses.add(retour);
                    int valeurProposition = Integer.parseInt(offre.getContent());
                    sb.append("\trecu cette proposition de ").append(offre.getSender().getLocalName()).append(" :: ").append(valeurProposition).append("\n");
                    if(valeurProposition>maxi)
                    {
                        maxi=valeurProposition;
                        msgPourMeilleurOffreur = retour;
                    }
                }
                sb.append("~".repeat(30)).append("\n");
                sb.append("meilleur offre = ").append(maxi).append("\n");
                println(sb.toString());

                int finalMaxi = maxi;
                listeReponses.forEach(msg->msg.setContent("Refusé, dsl, la meilleure offre etait de " + finalMaxi));
                //on avait garde un pointeur vers le message envoye a la meilleure offre, qu'un accepte finalement
                if(msgPourMeilleurOffreur!=null) {
                    msgPourMeilleurOffreur.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    msgPourMeilleurOffreur.setContent("ADJUGE VENDU pour " + finalMaxi);
                }
                mesRetours.addAll(listeReponses);
            }

            /**fonction lancee quand le meilleur offreur confirme son intention*/
            @Override
            protected void handleInform(ACLMessage inform) {
                println("_".repeat(30));
                println("la vente a bien eu lieu aupres de " + inform.getSender().getLocalName());
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


    public void launchRequest()
    {
        createOffer("echereNo1", "livre");
    }

}
