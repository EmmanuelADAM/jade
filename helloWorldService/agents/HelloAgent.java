package helloWorldService.agents;

import helloWorldService.gui.SimpleGui4Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

/**
 * an agent that say hello *
 * @author eadam
 */
public class HelloAgent extends GuiAgent {

	/** little agencesVoyages.gui to display debug messages */
	 SimpleGui4Agent window;

	/** address (aid) of the other agencesVoyages.agents */
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
		//s'enregistrer en tant d'agent d'accueil dans le service de cordialite
		AgentToolsEA.register(this, "cordialite", "accueil");

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

	/**envoi un message a tous les agencesVoyages.agents du service cordialite
	 * @param text texte envoye par le message
	 * */
	private void sendHello(String text) {
		neighbourgs = AgentToolsEA.searchAgents(this, "cordialite", null);
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent(text);
		for (AID other : neighbourgs) msg.addReceiver(other);
		send(msg);
	}

	/**reaction a l'evenement transmis par la fenetre associee
	 * @param ev evenement
	 * */
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
		try { DFService.deregister(this);}
		catch (FIPAException fe) { fe.printStackTrace();}
		System.err.println("Agent : " + getAID().getName() + " quitte la plateforme.");
		window.dispose();
	}

}
