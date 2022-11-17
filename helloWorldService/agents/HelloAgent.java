package helloWorldService.agents;

import helloWorldService.gui.SimpleGui4Agent;
import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.behaviours.ReceiverBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

import java.awt.*;

/**
 * class for an agent that registers to a sevice and that messages to agents according to the service they belong
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
     * agent set-up.
     * - Register the agent in the yellow pages for a service chosen between cordiality-lobby or cordiality-reception.
     * - Add a behavior that listens and displays the received message
     */
    @Override
    protected void setup() {
        String[] args = (String[]) this.getArguments();
        helloMsg = ((args != null && args.length > 0) ? args[0] : "Hello");
        window = new SimpleGui4Agent(this);
        window.println(helloMsg, false);
        //choose to register to lobby or reception-desk
        if (Math.random() < 0.5) {
            //Register as a lobby agent in the cordiality service
            AgentServicesTools.register(this, "cordiality", "lobby");
            window.mainTextArea.setBackground(Color.pink);
            window.println("I'm registered in the service 'lobby' inside the 'cordiality' type of service");
        } else {
            //Register as a reception agent in the cordiality service
            AgentServicesTools.register(this, "cordiality", "receptiondesk");
            window.mainTextArea.setBackground(Color.lightGray);
            window.println("I'm registered in the service 'reception desk' inside the 'cordiality' type of service");
        }

        //Stay continuously attentive to messages received of all types, without limit of duration
        addBehaviour(new ReceiverBehaviour(this, -1, null, true, (a,msg)->{
                    window.println("I've received a message from " + msg.getSender().getLocalName(),
                            true);
                    window.println("  Here is the content: \"" + msg.getContent()+"\"", true);
                    window.println("-".repeat(30));
                }));

    }

    /**
     * Send a message to the agents registered under a given service
     * @param text text to send
     * @param nameService Name of the service to which the recipients of the message belong
     */
    private void sendMessage(String text, String nameService) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent(text);
        neighbourgs = AgentServicesTools.searchAgents(this, "cordiality", nameService);
        msg.addReceivers(neighbourgs);
        send(msg);
        window.println("-> \""+text + "\" sent to agents of the service '" + nameService+"'");
    }


    /**
     * Reaction to the event transmitted by the window
     *
     * @param ev evenement
     */
    protected void onGuiEvent(GuiEvent ev) {
        switch (ev.getType()) {
            case SimpleGui4Agent.SENDLOBBY -> sendMessage(window.lowTextArea.getText(),"lobby");
            case SimpleGui4Agent.SENDRECEPTIONDESK -> sendMessage(window.lowTextArea.getText(), "receptiondesk");
            case SimpleGui4Agent.QUITCODE -> doDelete();
        }
    }

    /**deregister to the service and close the window before leaving
     * */
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
