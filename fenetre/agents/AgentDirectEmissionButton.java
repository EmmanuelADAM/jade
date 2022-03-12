package fenetre.agents;

import jade.core.AID;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;


/**
 * agents associé à une fenêtre, envoie un message direct aux agents b,c,d lorsque la fenêtre lui informe que le bouton a été cliqué
 * @author eadam
 */
@SuppressWarnings("serial")
public class AgentDirectEmissionButton extends AgentWindowed {

	/**
	 * initialize the agents <br>
	 * create the local dir to store data and roles <br>
	 * add the stack of behaviours (pileComportements)
	 */
	protected void setup() {  
		window = new SimpleWindow4Agent(getAID().getName(), this);
		println("Hello! Agent  " +  getLocalName() + " is ready, my address is " + this.getAID().getName());
		window.setButtonActivated(true);
	}
	
	  /**reaction to a gui event*/
	  protected void onGuiEvent(GuiEvent ev) {
	    switch(ev.getType()) {
			case SimpleWindow4Agent.OK_EVENT -> sendMessages();
	    }
	  }

	  /**send messages to agents b, c & d*/
	private void sendMessages()
	{
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent("salut");
		msg.addReceiver(new AID("b", false));
		msg.addReceiver(new AID("c", false));
		msg.addReceiver(new AID("d", false));
		send(msg);
	}


}
