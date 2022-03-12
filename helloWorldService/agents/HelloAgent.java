package helloWorldService.agents;

import helloWorldService.gui.SimpleGui4Agent;
import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.behaviours.CyclicBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

/**
 * des agents qui s'enregistrent comme membres de services et qui se saluent.<br>
 * Utilisation d'une fenetre dediee
 *
 * @author eadam
 */
public class HelloAgent extends GuiAgent {

    /**
     * little gui to display and send messages
     */
    SimpleGui4Agent window;

    /**
     * address (aid) of the other agents
     */
    AID[] neighbourgs;

    /**
     * msg to send
     */
    String helloMsg;

    /**
     * initialize the agent <br>
     */
    @Override
    protected void setup() {
        String[] args = (String[]) this.getArguments();
        helloMsg = ((args != null && args.length > 0) ? args[0] : "Hello");
        window = new SimpleGui4Agent(this);
        window.println(helloMsg);
        //on s'enregistre au hasard dans des services differents
        if (Math.random() < 0.5) {
            //s'enregistrer en tant d'agent d'accueil dans le service de cordialite
            AgentServicesTools.register(this, "cordialite", "accueil");
        } else {
            //s'enregistrer en tant d'agent de reception dans le service de cordialite
            AgentServicesTools.register(this, "cordialite", "reception");
        }


        //rester a l'ecoute des messages recus
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive();
                if (msg != null) {
                    window.println("j'ai recu un message de " + msg.getSender(), true);
                    window.println("voici le contenu : " + msg.getContent(), true);
                }
            }
        });

    }

    /**
     * envoi un message a tous les agents du service cordialite,
     * au hasard aux agents d'accueil ou de reception
     *
     * @param text texte envoye par le message
     */
    private void sendHello(String text) {
        String nameService = "accueil";
        if (Math.random() < 0.5) nameService = "reception";
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent(text + " pour service " + nameService);
        neighbourgs = AgentServicesTools.searchAgents(this, "cordialite", nameService);
        msg.addReceivers(neighbourgs);
        send(msg);
        window.lowTextArea.setText(text + " envoye au service " + nameService);
    }

    /**
     * reaction a l'evenement transmis par la fenetre associee
     *
     * @param ev evenement
     */
    protected void onGuiEvent(GuiEvent ev) {
        switch (ev.getType()) {
            case SimpleGui4Agent.SENDCODE -> sendHello(window.lowTextArea.getText());
            case SimpleGui4Agent.QUITCODE -> {
                window.dispose();
                doDelete();
            }
            default -> {
            }
        }
    }

    // Fermeture de l'agent
    @Override
    protected void takeDown() {
        // S'effacer du service pages jaunes
        AgentServicesTools.deregisterAll(this);
        //fermer la fenetre
        window.dispose();
        //bye
        System.err.println("Agent : " + getAID().getName() + " quitte la plateforme.");
    }

}
