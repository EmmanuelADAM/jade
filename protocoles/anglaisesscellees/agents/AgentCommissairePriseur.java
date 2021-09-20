package encheres.anglaisesscellees.agents;


import encheres.anglaisesscellees.gui.SimpleWindow4Agent;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.proto.ContractNetInitiator;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * classe d'un agent qui soumet un appel d'offres a d'autres agents  par le protocole ContractNet
 * @author eadam
 */
public class AgentCommissairePriseur extends AgentWindowed {

    /**ajout du suivi de protocole AchieveRE*/
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.println("Hello! Agent  " +  getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setButtonActivated(true);
//        addBehaviour(new WakerBehaviour(this, 1000) {
//            public void  onWake() {
//                createRequest("123", "sum 4,5,6,7");
//            }});

    }

    /**add a ContractNet protocol to launch a... */
    private void createOffer(String id, String objet) {

        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setConversationId(id);
        msg.setContent(objet);

        var adresses = AgentToolsEA.searchAgents(this, "enchere", "participant");
        for (AID dest : adresses) msg.addReceiver(dest);
        println("destinataires trouves : " + Arrays.toString(adresses));

        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));

        window.println("je lance les encheres pour " + msg.getContent());


        ContractNetInitiator init = new ContractNetInitiator(this, msg) {
            /**fonction lancee a chaque proposition*/
            @Override
            public void handlePropose(ACLMessage propose, Vector v)
            {
                println(String.format("l'agent %s propose %s ", propose.getSender().getLocalName(), propose.getContent()) );
            }

            /**fonction lancee quand toutes les reponses ont ete recues*/
            @Override
            protected void handleAllResponses(Vector leursOffres, Vector mesRetours) {
                int maxi = Integer.MIN_VALUE;
                ACLMessage msgPourMeilleurOffreur = null;
                List<ACLMessage> liste = (List<ACLMessage>)leursOffres.stream().toList();
                for (ACLMessage offre : liste) {
                    if (offre.getPerformative() == ACLMessage.PROPOSE) {
                        var retour = offre.createReply();
                        retour.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        mesRetours.add(retour);
                        int valeurProposition = Integer.parseInt(offre.getContent());
                        window.println("recu cette proposition de " + offre.getSender().getLocalName() + " :: " + valeurProposition);
                        if(valeurProposition>maxi)
                        {
                            maxi=valeurProposition;
                            msgPourMeilleurOffreur = retour;
                        }
                    }
                }
                if(msgPourMeilleurOffreur!=null) {
                    msgPourMeilleurOffreur.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    msgPourMeilleurOffreur.setContent("ADJUGE VENDU");
                }
            }

            /**fonction lancee quand le meilleur offreur confirme son intention*/
            @Override
            protected void handleInform(ACLMessage inform) {
                println("la vente a bien eu lieu aupres de " + inform.getSender().getLocalName());
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
