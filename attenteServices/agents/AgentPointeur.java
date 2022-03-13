package attenteServices.agents;

import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * agents associé à une fenêtre, envoie un message radio sur un canal
 *
 * @author eadam
 */
@SuppressWarnings("serial")
public class AgentPointeur extends AgentWindowed {
    /**
     * liste des agents venant
     */
    List<AID> venants;
    /**
     * no du msg envoyé
     */
    int i = 0;

    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setBackgroundTextColor(Color.YELLOW);
        detectVenants();
    }
    /**
     * ecoute des evenement de type enregistrement aupres des pages jaunes en tant qu'agent venant<br>
     * A chaque nouvelle arrivee, la liste des agents du groupe est envoyee a ces agents
     */
    private void detectVenants() {
        var model = AgentServicesTools.createAgentDescription("balladeur", "ouvert");
        var msg = DFService.createSubscriptionMessage(this, getDefaultDF(), model, null);
        venants = new ArrayList<>();
        addBehaviour(new SubscriptionInitiator(this, msg) {
            /**reaction au message d'information envoye par de DF (pages jaunes)*/
            @Override
            protected void handleInform(ACLMessage inform) {
                window.println("Agent " + getLocalName() + ": information recues de DF");
                try {
                    //le DF envoie en un msg les descriptions de service des agents inscrits OU DESINCRITS pdt la mm millisenconde
                    var results = DFService.decodeNotification(inform.getContent());
                    if (results.length > 0) {
                        //pour chaque agent
                        for (DFAgentDescription dfd : results) {
                            var l = dfd.getAllServices();
                            //si la liste des services est non vide alors l'agent s'est inscrit
                            if(l.hasNext()){
                                var service = l.next();
                                venants.add(dfd.getName());
                                window.println(dfd.getName().getLocalName() + " s'est inscrit a " + service.getName() + "-"+service.getType());
                            }
                            //si la liste des services est vide alors l'agent s'est desinscrit
                            else {
                                venants.remove(dfd.getName());
                                window.println(dfd.getName().getLocalName() + " s'est desinscrit  " );
                            }
                        }
                        window.println("~".repeat(20));
                        //on envoit un petit message sur l'etat du groupe a tous les agents de ce groupe
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.addReceivers(venants.toArray(AID[]::new));
                        msg.setContent("membres du groupe : " + Arrays.toString(venants.stream().map(AID::getLocalName).toArray()));
                        send(msg);
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });
    }


}
