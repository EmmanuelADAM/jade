/*
 * Created on 12 avr. 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package comportements;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import agents.TravellerAgent;
import data.JourneysList;
import gui.TravellerGui;


/**
 * Comportement d'appel d'offres pour un voyageur vers les agences de voyage
 * @author Emmanuel ADAM
 */
@SuppressWarnings("serial")
public class ContractNetAchat extends ContractNetInitiator{
	/** nb responses*/
	private int nbRepondants = 0;
	/**ville de depart*/
	private String from;
   /**ville d'arrivee*/
	private String to;
	/**date de depart (hhmm)*/
	private int departure;
	/**methode de choix (par cout, duree, confort, ...)*/
	private String preference;
	
	/** agent gui*/
	private TravellerGui window;
	
	/**voyageur associe a ce comportement*/
	private TravellerAgent monAgent;
	
	/**
	 * @param agent agent associe au comportement
	 * @param msg message d'appel d'offres a envoyer
	 * @param _from ville de depart
	 * @param _to ville d'arrivee
	 * @param _departure heure de depart (hhmm)
	 * @param _preference (critere de choix)
	 */
	public ContractNetAchat(Agent agent, ACLMessage msg, final String _from, final String _to, final int _departure, final String _preference)
	{
		super(agent, msg);
		from = _from;
		to = _to;
		departure = _departure;
		preference = _preference;
		monAgent = (TravellerAgent) agent;
		window = monAgent.getWindow();
		// définition du prococole
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
		// Réponse plus tard dans 10 secs
		msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));
		List<AID> vendeurs = monAgent.getVendeurs();
		vendeurs.forEach(msg::addReceiver);
		// relacer le comportement
		this.reset(msg);
	}
	
	
	/** methode lancee a la reception de chaque refus
	 * @param refuse refus recu
	 */
	@Override
	protected void handleRefuse(ACLMessage refuse) {
		window.println("Agent "+refuse.getSender().getLocalName()+" refuse");
	}

	/** methode lancee a la reception d'un message d'erreur (impossibilite de poursuivre la vente)
	 * @param failure erreur recue
	 */
	@Override
	protected void handleFailure(ACLMessage failure) {
		if (failure.getSender().equals(myAgent.getAMS())) {
			// ERREUR : le destinataire n'existe pas
			window.println("Le destinataire n'existe pas...");
		}
		else 
			window.println("Agent "+failure.getSender().getLocalName()+" a echoue");
		
		// nombre de répondants décrémenté
		nbRepondants --;
	}
	
	/**
	 *  methode lancee si toutes les reponses sont arrivees ou si le temps est ecoule<br>
	 *  choisit les trajets qui conviennent pour le voyage<br>
	 *  peut accepter des trajets de differentes agences si ils permettent de couvrir el voyage
	 * @see jade.proto.ContractNetInitiator#handleAllResponses(java.util.Vector, java.util.Vector)
	 * @param responses reponses recues
	 * @param acceptances vecteur des messages a transmettre en retour aux reponses recues
	 */
	@SuppressWarnings({ "rawtypes" })
	@Override
	protected void handleAllResponses(Vector responses, Vector acceptances) {
		final int nbAnswers = responses.size();
		final JourneysList catalogs = new JourneysList();
		for (int i = 0; i < nbAnswers; i++) {
			final ACLMessage ans = (ACLMessage) responses.get(i);
			if (ans.getPerformative() == ACLMessage.PROPOSE) {
				JourneysList receivedCatalog = null;
				try {
					receivedCatalog = (JourneysList) ans.getContentObject();
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
				if (receivedCatalog != null)
					catalogs.addJourneys(receivedCatalog);
			}
			ACLMessage reply = ans.createReply();
			reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
			acceptances.add(reply);
		}
		monAgent.setCatalogs(catalogs);
		monAgent.println("j'ai bien recu les calalogues : ");
		monAgent.println(catalogs.toString());
		monAgent.println("je fais mon choix...");
      monAgent.computeComposedJourney(from, to, departure, preference);
		//TODO: faire son choix, accepter les trajets directs retenus, rejeter les autres


	}
	
	/** methode lancee a la reception d'un message d'information (vente confirmee)
	 * @param inform message recu
	 */
	@Override
	protected void handleInform(ACLMessage inform) {
		window.println("Agent "+inform.getSender().getLocalName()+" a effectue l'action avec succes");
	}
}
