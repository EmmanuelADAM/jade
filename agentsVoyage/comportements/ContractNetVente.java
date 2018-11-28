/*
 * Created on 12 avr. 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package comportements;

import gui.AgenceGui;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

import java.io.IOException;

import agents.AgenceAgent;
import data.JourneysList;;

/**
 * Comportement de reponse a appels d'offres pour les agences de voyage
 * @author Emmanuel ADAM
 */
@SuppressWarnings("serial")
public class ContractNetVente extends  ContractNetResponder
{

	/** catalog of the proposed journeys */
	private JourneysList catalog;

	/** agent gui*/
	AgenceGui window;

	/**agent lie au comportement*/
	AgenceAgent monAgent;

	/**
	 * Initialisation du contract net
	 * @param agent agent vendeur lie
	 * @param template modele de message a attendre
	 * @param _catalogue catalogue trajets simples proposes
	 */
	public ContractNetVente(Agent agent, MessageTemplate template, JourneysList _catalog)
	{
		super(agent, template);
		monAgent = (AgenceAgent)agent;
		window = monAgent.getWindow();
		catalog = _catalog;
	}

	/** methode lancee a la reception d'un appel d'offre
	 * @see jade.proto.ContractNetResponder#prepareResponse(jade.lang.acl.ACLMessage)
	 * @param cfp l'appel recu
	 * @throws NotUnderstoodException si le message n'est pas compris
	 * @throws RefuseException si le trajet demande n'est pas en catalogue
	 */
	protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
		window.println("Agent "+myAgent.getLocalName()+": CFP recu de "+cfp.getSender().getLocalName() );
		ACLMessage propose = cfp.createReply();
		propose.setPerformative(ACLMessage.PROPOSE);
		try { propose.setContentObject(catalog); } 
		catch (IOException e) { e.printStackTrace(); }
		return propose;
	}



	/** methode lancee suite a la reception d'une acceptation de l'offre par l'acheteur
	 * @see jade.proto.ContractNetResponder#prepareResultNotification(jade.lang.acl.ACLMessage, jade.lang.acl.ACLMessage, jade.lang.acl.ACLMessage)
	 * @param cfp l'appel a proposition initial
	 * @param propose la proposition retourne par l'agent
	 * @param accept le message d'acceptation de l'offre
	 * @throws FailureException si le livre n'est plus disponible (si la transaction echoue)
	 * @return le message de confirmation de la vente
	 */
	protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException 
	{
		ACLMessage inform = accept.createReply();
		inform.setPerformative(ACLMessage.INFORM);
		//TODO: devrementer le nb de trajets disponibles suite a la vente
		return inform;
	}

	/** methode lancee suite a la reception d'un refus de l'offre par l'acheteur
	 * @see jade.proto.SSContractNetResponder#handleRejectProposal(jade.lang.acl.ACLMessage, jade.lang.acl.ACLMessage, jade.lang.acl.ACLMessage)
	 * @param cfp l'appel a proposition initial
	 * @param propose la proposition retourne par l'agent
	 * @param reject le message de refus de l'offre
	 */
	protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
		window.println("Agent "+reject.getSender().getLocalName()+" a rejete la proposition pour " + reject.getContent());
	}	
} 
