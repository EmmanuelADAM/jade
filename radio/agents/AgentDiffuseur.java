package radio.agents;

import radio.gui.SimpleWindow4Agent;
import jade.core.AID;
import jade.core.messaging.TopicManagementHelper;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;


/**
 * agents associé à une fenêtre, envoie un message radio sur un canal
 * @author eadam
 */
@SuppressWarnings("serial")
public class AgentDiffuseur extends AgentWindowed {
	/**adresse du topic radio*/
	AID topic;
	/**no du msg envoyé*/
	int i=0;

	protected void setup() {
		window = new SimpleWindow4Agent(getAID().getName(), this);
		println("Hello! Agent  " +  getLocalName() + " is ready, my address is " + this.getAID().getName());
		window.setButtonActivated(true);
		//Creation d'un "canal radio" de nom InfoRadio
		try {
			TopicManagementHelper topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
			topic = topicHelper.createTopic("InfoRadio");
		}
		catch (Exception e) {
			System.err.println("Agent "+getLocalName()+": ERROR creating topic \"InfoRadio\"");
			e.printStackTrace();
		}
	}

	  /**reaction to a gui event*/
	  protected void onGuiEvent(GuiEvent ev) {
	    switch(ev.getType()) {
	      case -1:  sendMessages(); break;
	    }
	  }

	  /**send messages to the topic*/
	private void sendMessages()
	{
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(topic);
		msg.setContent("hello "+i);
		i++;
		send(msg);
	}

}
