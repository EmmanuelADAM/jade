
package agents;

import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;

import com.opencsv.CSVReader;

import comportements.ContractNetVente;
import data.Journey;
import data.JourneysList;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import launch.LaunchSimu;

/**
 * Journey Seller
 * 
 * @author Emmanuel ADAM
 */
@SuppressWarnings("serial")
public class AgenceAgent extends GuiAgent {
	/** code shared with the gui to add a journey */
	public static final int ADD_TRAVEL = 1;
	/** code shared with the gui to quit the agent */
	public static final int EXIT = 0;

	/** catalog of the proposed journeys */
	private JourneysList catalog;
	/** graphical user interface linked to the seller agent */
	private gui.AgenceGui window;

	// Initialisation de l'agent
	@Override
	protected void setup() {
		final Object[] args = getArguments(); // Recuperation des arguments
		catalog = new JourneysList();
		window = new gui.AgenceGui(this);
		window.display();

		if (args != null && args.length > 0) {
			fromCSV2Catalog((String) args[0]);
		}

		AgentToolsEA.register(this, "travel agency", "seller");

		// attendre une demande de catalogue
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
				MessageTemplate.MatchPerformative(ACLMessage.CFP) );
		addBehaviour(new ContractNetVente(this, template, catalog));
		
		//TODO: ajouter l'ecoute de msg radio lies aux alertes et gerer ces alertes !

	}
	// Fermeture de l'agent
	@Override
	protected void takeDown() {
		// S'effacer du service pages jaunes
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			LaunchSimu.logger.log(Level.SEVERE, fe.getMessage());
		}
		LaunchSimu.logger.log(Level.INFO, "Agent Agence : " + getAID().getName() + " quitte la plateforme.");
		window.dispose();
	}

	/**
	 * methode invoquee par la gui
	 */
	@Override
	protected void onGuiEvent(GuiEvent guiEvent) {
		if (guiEvent.getType() == AgenceAgent.EXIT) {
			doDelete();
		}
	}

	/**
	 * initialize the catalog from a cvs file<br>
	 * csv line = origine, destination,means,departureTime,duration,financial
	 * cost, co2, confort, nbRepetitions(optional),frequence(optional)
	 * 
	 * @param file
	 *            name of the cvs file
	 */
	void fromCSV2Catalog(final String file) {
		try (CSVReader cvsReader = new CSVReader(new FileReader(file), ',', '\'', 1)){
			String[] nextLine;
			while ((nextLine = cvsReader.readNext()) != null) {
				String origine = nextLine[0].trim().toUpperCase();
				String destination = nextLine[1].trim().toUpperCase();
				String means = nextLine[2].trim();
				final int departureDate = Integer.parseInt(nextLine[3].trim());
				final int duration = Integer.parseInt(nextLine[4].trim());
				final int cost = Integer.parseInt(nextLine[5].trim());
				final int co2 = Integer.parseInt(nextLine[6].trim());
				final int confort = Integer.parseInt(nextLine[7].trim());
				final int nbRepetitions = (nextLine.length == 9) ? 0 : Integer.parseInt(nextLine[8].trim());
				final int frequence = (nbRepetitions == 0) ? 0 : Integer.parseInt(nextLine[9].trim());
				final Journey firstJourney = new Journey(origine, destination, means, departureDate, duration, cost,
						co2, confort);
				window.println(firstJourney.toString());
				catalog.addJourney(firstJourney);
				if (nbRepetitions > 0) {
					repeatJourney(departureDate, nbRepetitions, frequence, firstJourney);
				}
			}
		} catch (NumberFormatException | IOException e) { window.println(e.getMessage()); }
	}

	/**
	 * repeat a journey on a sequence of dates into a catalog
	 * 
	 * @param departureDate
	 *            date of the first journey
	 * @param nbRepetitions
	 *            nb of journeys to add
	 * @param frequence
	 *            frequency of the journeys in minutes
	 * @param journey
	 *            the first journey to clone
	 */
	private void repeatJourney(final int departureDate, final int nbRepetitions, final int frequence,
			final Journey journey) {
		int nextDeparture = departureDate;
		for (int i = 0; i < nbRepetitions; i++) {
			final Journey cloneJ = new Journey(journey);
			nextDeparture = Journey.addTime(nextDeparture, frequence);
			cloneJ.setDepartureDate(nextDeparture);
			window.println(cloneJ.toString());
			catalog.addJourney(cloneJ);
		}
	}

	///// GETTERS AND SETTERS
	public gui.AgenceGui getWindow() {
		return window;
	}

	/**
	 * @return the catalogue
	 */
	public JourneysList getCatalog() {
		return catalog;
	}

}
