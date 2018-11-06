package agents;

import gui.SimpleGui4Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

/**
 * an agent that say hello * @author eadam
 */
@SuppressWarnings("serial")
public class HelloAgent extends GuiAgent {

	/** little gui to display debug messages */
	 SimpleGui4Agent window;

	/** address (aid) of the other agents */
	AID[] neighbourgs;

	/** msg to send */
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
		AgentToolsEA.register(this, "cordialite", "accueil");

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

	private void sendHello(String text) {
		neighbourgs = AgentToolsEA.searchAgents(this, "cordialite", null);
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		for (AID other : neighbourgs)
			msg.addReceiver(other);
		msg.setContent(text);
		send(msg);
	}

	protected void onGuiEvent(GuiEvent ev) {
		switch (ev.getType()) {
		case SimpleGui4Agent.SENDCODE:
			sendHello(window.lowTextArea.getText());
			break;
		case SimpleGui4Agent.QUITCODE:
			window.dispose();
			doDelete();
			break;
		default:
		}
	}

	// Fermeture de l'agent
	@Override
	protected void takeDown() {
		// S'effacer du service pages jaunes
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		System.err.println("Agent : " + getAID().getName() + " quitte la plateforme.");
		window.dispose();
	}

}
