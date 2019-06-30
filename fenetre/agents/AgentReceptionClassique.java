package fenetre.agents;


import fenetre.gui.SimpleWindow4Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * agent lié à une fenêtre qui attend des messages et les affiche
 * @author eadam
 */
@SuppressWarnings("serial")
public class AgentReceptionClassique extends AgentWindowed{


	/**
	 * initialize the agent <br>  
	 * create the local dir to store data and roles <br>
	 * add the stack of behaviours (pileComportements)
	 */
	protected void setup() {  
		window = new SimpleWindow4Agent(getAID().getName(), this);
		println("Hello! Agent  " + getAID().getName() + " is ready. ");

			// comportement cyclique d'affichage de messages
			 addBehaviour(new CyclicBehaviour(this) {
			  public void action() {  
			   ACLMessage msg = receive();
			   if (msg != null) {
				// recuperation du contenu : 
				   String contenu  = msg.getContent();
				   // recuperation de l'adresse de l'emetteur 
				   AID adresseEmetteur = msg.getSender();
				   // recuperation du nom déclaré en local de l'emetteur 
				   String nomEmetteur  = adresseEmetteur.getLocalName();

				   println("recu " + contenu +", de "+nomEmetteur);}
			   block();
			  }});
			
	}



	/**
	 * @return the window
	 */
	public SimpleWindow4Agent getWindow() {
		return window;
	}


	/**
	 * @param window the window to set
	 */
	public void setWindow(SimpleWindow4Agent window) {
		this.window = window;
	}


}
