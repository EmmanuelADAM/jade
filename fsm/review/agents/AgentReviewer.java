package fsm.review.agents;


import jade.core.AgentServicesTools;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;

import java.awt.*;
import java.util.Random;


/**
 * classe d'un agent qui soumet un appel d'offres a d'autres agents  par le protocole ContractNet
 *
 * @author eadam
 */
public class AgentReviewer extends AgentWindowed {

    ACLMessage message;
    boolean busy;
    Random random;

    Behaviour bReception;

    /**
     * un reviewer attends un article puis retourne une note
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setButtonActivated(true);
        window.setBackgroundTextColor(Color.CYAN);
        random = new Random();
        AgentServicesTools.register(this, "journal", "reviewer");
        var dfd  = AgentServicesTools.createAgentDescription("journal", "reviewer");
        this.addService("reviewer", dfd);
        addBehaviour(new BehaviourReviewer());
        window.setButtonActivated(false);
    }

    /**un clic sur le bouton declenche cette fonction qui envoie une note en retour de l'artcile recu*/
    @Override
    protected void onGuiEvent(GuiEvent arg0) {
        if(message!=null) {
            var reply = message.createReply();
            reply.setContent(Integer.toString(random.nextInt(0, 3)));
            println("j'envoie cette note %s avec la cle %s ".formatted(reply.getContent(), reply.getConversationId()));
            println("-".repeat(40));
            send(reply);
            busy = false;
            addBehaviour(new BehaviourReviewer());
            window.setButtonActivated(false);
        }
    }


    class BehaviourReviewer extends CyclicBehaviour {
        @Override
        public void action() {
            if(!busy) {
                message = receive();
                if (message != null) {
                    println("j'ai recu ce contenu à reviewer : " + message.getContent());
                    println("cliquez pour envoyer une evaluation... ");
                    busy = true;
                    window.setButtonActivated(true);
                } else block();
            }
        }
    }

    @Override
    protected void takeDown()
    {
        AgentServicesTools.deregisterService(this, mapServices.get("reviewer"));
    }
}
