package agencesVoyages.comportements;

import agencesVoyages.agents.AgenceAgent;
import agencesVoyages.data.Journey;
import agencesVoyages.data.JourneysList;
import agencesVoyages.gui.AgenceGui;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Journeys Seller Behaviour by contract net
 *
 * @author revised by Emmanuel ADAM
 * @version 191017
 */
@SuppressWarnings("serial")
public class ContractNetVente extends ContractNetResponder {

    /**
     * catalog of the proposed journeys
     */
    private final JourneysList catalog;

    /**
     * agent gui
     */
    private final AgenceGui window;

    /**
     * Initialisation du contract net
     *
     * @param agent    agent agence lie
     * @param template modele de message a attendre
     * @param _catalog catalogue des voyages
     */
    public ContractNetVente(Agent agent, MessageTemplate template, JourneysList _catalog) {
        super(agent, template);
        var monAgent = (AgenceAgent) agent;
        window = monAgent.getWindow();
        catalog = _catalog;
    }

    /**
     * methode lancee a la reception d'un appel d'offre
     *
     * @param cfp l'appel recu
     * @throws NotUnderstoodException si le message n'est pas compris
     * @throws RefuseException        s'il n'y a pas de trajet en catalogue
     * @see ContractNetResponder#handleCfp(ACLMessage)
     */
    protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
        window.println("Agent " + myAgent.getLocalName() + ": CFP recu de " + cfp.getSender().getLocalName());
        if (catalog.isEmpty()) throw new RefuseException("no journey !");
        var propose = cfp.createReply();
        propose.setPerformative(ACLMessage.PROPOSE);
        try {
            propose.setContentObject(catalog);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return propose;
    }


    /**
     * methode lancee suite a la reception d'une acceptation de l'offre par l'acheteur
     *
     * @param cfp     l'appel a proposition initial
     * @param propose la proposition retourne par l'agent
     * @param accept  le message d'acceptation de l'offre
     * @return le message de confirmation de la vente
     * @throws FailureException si le livre n'est plus disponible (si la transaction echoue)
     * @see ContractNetResponder#handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept)
     */
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
        ACLMessage inform = accept.createReply();
        inform.setPerformative(ACLMessage.INFORM);
        window.println(" RECU UN ACCORD DE " + accept.getSender().getLocalName() + " !!!");
        ArrayList<Journey> liste = null;
        try {
            liste = (ArrayList<Journey>) accept.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
        if (liste != null) {
            window.println("Il veut ");
            liste.forEach(j -> window.println(j.toString()));
            window.println("  !!!!");
            inform.setContent("ok pour ces " + liste.size() + " tickets...");
        }
        return inform;
    }

    /**
     * methode lancee suite a la reception d'un refus de l'offre par l'acheteur
     *
     * @param cfp     l'appel a proposition initial
     * @param propose la proposition retourne par l'agent
     * @param reject  le message de refus de l'offre
     * @see jade.proto.ContractNetResponder#handleRejectProposal(ACLMessage, ACLMessage, ACLMessage)
     */
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        window.println("Agent " + reject.getSender().getLocalName() + " a rejete la proposition pour " + reject.getContent());
    }
} 
