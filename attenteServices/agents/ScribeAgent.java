package attenteServices.agents;

import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.domain.DFSubscriber;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * agents associé à une fenêtre, envoie un message radio sur un canal
 *
 * @author eadam
 */
@SuppressWarnings("serial")
public class ScribeAgent extends AgentWindowed {
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
        venants = new ArrayList<>();

        //souscription au service des pages jaunes pour recevoir une alerte en cas mouvement sur le service balladeur'ouvert
        addBehaviour(new DFSubscriber(this, model) {
            @Override
            public void onRegister(DFAgentDescription dfd) {
                venants.add(dfd.getName());
                window.println(dfd.getName().getLocalName() + " s'est inscrit avec " + model);
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                //on transforme la liste des venants en un tableau pour passage en parametre
                msg.addReceivers(venants.toArray(AID[]::new));
                msg.setContent("membres du groupe : " + Arrays.toString(venants.stream().map(AID::getLocalName).toArray()));
                send(msg);
            }

            @Override
            public void onDeregister(DFAgentDescription dfd) {
                venants.remove(dfd.getName());
                window.println(dfd.getName().getLocalName() + " s'est desinscrit de " + model);
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceivers(venants.toArray(AID[]::new));
                msg.setContent("membres du groupe : " + Arrays.toString(venants.stream().map(AID::getLocalName).toArray()));
                send(msg);
            }

        });
    }


}
