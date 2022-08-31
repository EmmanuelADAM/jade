package attenteServices.agents;


import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * agent lié à une fenêtre qui ecoute en boucle des messages sur un topic
 *
 * @author eadam
 */
public class AgentVenant extends AgentWindowed {
    AID topic = null;


    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent  " + getAID().getName() + " is ready. ");
        AgentServicesTools.register(this, "unServiceQuelconque", "unTypedeService");

        //s'inscrire au service de passage au bout de quelques temps
        addBehaviour(new WakerBehaviour(this, (long)(Math.random()*10000d),
                a->AgentServicesTools.register(a, "balladeur", "ouvert")));


        //ecoute cyclique de message sans limite de duree, de tout type, en continu
        addBehaviour(new ReceiverBehaviour(this, -1, null, true,
                (a,msg)-> println("recu de " + msg.getSender().getLocalName() + ", ceci : \n" + msg.getContent() +
                        "\n" + "-".repeat(20))));
    }

    @Override
    public void takeDown() {
        AgentServicesTools.deregisterAll(this);
        System.err.println("moi " + this.getLocalName() + ", je quitte la plateforme...");
    }


}
