package protocoles.requetes.agents;


import jade.core.AgentServicesTools;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.core.AID;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * classe d'un agent qui soumet une requête de somme à un autre agent et gère l'échange par le protocole AchieveRE
 * @author eadam
 */
public class AgentEmissionARE extends AgentWindowed {

    /**ajout du suivi de protocole AchieveRE*/
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.println("Hello! Agent  " +  getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setButtonActivated(true);
        window.setBackgroundTextColor(Color.CYAN);
    }

    /**add a AchieveREInitiator protocol to send a request */
    private void createRequest(String id, String computation) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.setConversationId(id);
        msg.setContent(computation);

        var adresses = AgentServicesTools.searchAgents(this, "calcul", "somme");
        msg.addReceivers(adresses);
        println("agents calculateurs trouves : " + Arrays.stream(adresses).map(AID::getLocalName).toList().toString());

        println("_".repeat(40));
        println("j'envoie une requete sur " + msg.getContent());
        println("_".repeat(40));
        AchieveREInitiator init = new AchieveREInitiator(this, msg) {
            //fonction lancée dès accord
            @Override
            protected void handleAgree(ACLMessage agree) {
                window.println("recu un accord de " + agree.getSender().getLocalName());
            }

            //fonction lancée dès accord
            @Override
            protected void handleRefuse(ACLMessage agree) {
                window.println("recu un refus de " + agree.getSender().getLocalName());
            }

            //fonction lancée dès reception information
            @Override
            protected void handleInform(ACLMessage inform) {
                window.println("recu  de " + inform.getSender().getLocalName() +
                        ", ce resultat " + inform.getContent());
            }


            //fonction lancée dès que toutes les réponses ont été reçues
            @Override
            protected void handleAllResultNotifications(List<ACLMessage> responses) {
                println("~".repeat(40));
                StringBuilder sb = new StringBuilder("c'est bon, j'ai toutes les reponses. Pour rappel : \n");
                for (ACLMessage msg : responses) {
                    sb.append("\t-de ").append(msg.getSender().getLocalName()).append(" : ").append(msg.getContent()).append("\n");
                }
                println(sb.toString());
                println("(o)".repeat(20));
            }
        };

        addBehaviour(init);
    }


    protected void onGuiEvent(GuiEvent arg0) {
        Random r = new Random();
        int nb = r.nextInt(3,7);
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<nb; i++)
            sb.append(r.nextInt(1,100)).append(",");
        createRequest("123", "sum "+sb);
    }

}
