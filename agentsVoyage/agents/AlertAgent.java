package agents;

import java.awt.Color;

import data.JourneysList;
import gui.AlertGui;
import jade.core.AID;
import jade.core.ServiceException;
import jade.core.messaging.TopicManagementHelper;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

/**
 * Journey searcher
 * 
 * @author Emmanuel ADAM
 */
@SuppressWarnings("serial")
public class AlertAgent extends GuiAgent {
	/** code pour ajout de livre par la gui */
	public static final int EXIT = 0;
	/** code pour achat de livre par la gui */
	public static final int ALERT = 1;

	/** liste des vendeurs */
	protected AID[] vendeurs;

	/**
	 * preference between journeys -, cost, co2, duration or confort ("-" = cost
	 * by defaul)}
	 */
	private String sortMode;

	/** catalog received by the sellers */
	protected JourneysList catalogs;

	/** topic on which the alert will be send */
	AID topic;

	/** gui */
	private AlertGui window;

	/** Initialisation de l'agent */
	@Override
	protected void setup() {
		this.window = new AlertGui(this);
		window.setColor(Color.cyan);
		window.println("Hello!  Agent d'alertes " + this.getLocalName() + " est pret. ");
		window.setVisible(true);

		TopicManagementHelper topicHelper = null;
		try {
			topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
			topic = topicHelper.createTopic("TRAFFIC NEWS");
			topicHelper.register(topic);
		} catch (ServiceException e) {
			e.printStackTrace();
		}

	}

	// 'Nettoyage' de l'agent
	@Override
	protected void takeDown() {
		window.println("Je quitte la plateforme. ");
	}

	///// SETTERS AND GETTERS
	/**
	 * @return agent gui
	 */
	public AlertGui getWindow() {
		return window;
	}

	/** get event from the GUI */
	@Override
	protected void onGuiEvent(final GuiEvent eventFromGui) {
		if (eventFromGui.getType() == AlertAgent.EXIT) {
			doDelete();
		}
		if (eventFromGui.getType() == AlertAgent.ALERT) {
			ACLMessage alert = new ACLMessage(ACLMessage.INFORM);
			alert.setContent("une alerte meteo !! ");
			alert.addReceiver(topic);
			send(alert);
			println("j'ai envoyé une alerte...");
		}
	}

	/**
	 * @return the vendeurs
	 */
	public AID[] getVendeurs() {
		return vendeurs.clone();
	}

	/**
	 * @param vendeurs
	 *            the vendeurs to set
	 */
	public void setVendeurs(final AID... vendeurs) {
		this.vendeurs = vendeurs;
	}

	/** -, cost, co2, duration or confort */
	public String getSortMode() {
		return sortMode;
	}

	/**
	 * print a message on the window lined to the agent
	 * 
	 * @param msg
	 *            text to display in th window
	 */
	public void println(final String msg) {
		window.println(msg);
	}

	/** @return the list of journeys */
	public JourneysList getCatalogs() {
		return catalogs;
	}

	/** set the list of journeys */
	public void setCatalogs(final JourneysList catalogs) {
		this.catalogs = catalogs;
	}

}
