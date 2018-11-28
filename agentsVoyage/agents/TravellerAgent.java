package agents;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Stream;

import comportements.ContractNetAchat;
import data.ComposedJourney;
import data.Journey;
import data.JourneysList;
import gui.TravellerGui;
import jade.core.AID;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionInitiator;

/**
 * Journey searcher
 * Agent that support the user to choose the best journey
 * @author Emmanuel ADAM
 */
@SuppressWarnings("serial")
public class TravellerAgent extends GuiAgent {
	/** code pour ajout de livre par la gui */
	public static final int EXIT = 0;
	/** code pour achat de livre par la gui */
	public static final int BUY_TRAVEL = 1;

	/** liste des vendeurs */
	protected ArrayList<AID> vendeurs;

	/**
	 * preference between journeys -, cost, co2, duration or confort ("-" = cost
	 * by defaul)}
	 */
	private String sortMode;

	/** catalog received by the sellers */
	protected JourneysList catalogs;

	/** the journey chosen by the agent*/
	ComposedJourney myJourney;

	/** topic from which the alert will be received */
	AID topic;

	/** gui */
	private TravellerGui window;

	/** Initialisation de l'agent */
	@Override
	protected void setup() {
		this.window = new TravellerGui(this);
		window.setColor(Color.cyan);
		window.println("Hello! AgentAcheteurCN " + this.getLocalName() + " est pret. ");
		window.setVisible(true);

		TopicManagementHelper topicHelper = null;
		try {
			topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
			topic = topicHelper.createTopic("TRAFFIC NEWS");
			topicHelper.register(topic);
		} catch (ServiceException e) {
			e.printStackTrace();
		}
		
		vendeurs = new ArrayList<>();
		detectAgences();

		addBehaviour(new CyclicBehaviour() {
			@Override
			public void action() {
				ACLMessage msg = myAgent.receive(MessageTemplate.MatchTopic(topic));
				if (msg != null) {
					println("Message recu sur le topic " + topic.getLocalName() + ". Contenu " + msg.getContent()
							+ " émis par " + msg.getSender().getLocalName());
					//TODO: verifier si l'info ne perturbe pas le voyage !! et agir en fonction !!
				} else { block();}
			}
		});
		
	}
	

	/**ecoute des evenement de type enregistrement en tant qu'agence aupres des pages jaunes*/
	private void detectAgences()
	{
		// Build the description used as template for the subscription
		final DFAgentDescription template = new DFAgentDescription();
		ServiceDescription templateSd = new ServiceDescription();
		String type = "travel agency";
		String name = "seller";
		templateSd.setType(type); templateSd.setName(name);
		template.addServices(templateSd);
		
		ACLMessage msg = DFService.createSubscriptionMessage(this, getDefaultDF(), template, null);

		vendeurs = new ArrayList<>();
		addBehaviour(new SubscriptionInitiator(this, msg) {
			@Override
			protected void handleInform(ACLMessage inform) {
				window.println("Agent "+getLocalName()+": information recues de DF");
				try {
					DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
					if (results.length > 0) {
						for (DFAgentDescription dfd:results) {
							vendeurs.add(dfd.getName());
							window.println(dfd.getName().getName() + " s'est inscrit au service "+type+", en tant que "+name);
						}
					}	
				}
				catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}
		} );		
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
	public TravellerGui getWindow() {
		return window;
	}


	/**calcule un voyage compose de trajets directs 
	 * @param from ville de depart
	 * @param to ville d'arrivee
	 * @param departure heure de depart (hhmm)
	 * @param preference type de tri (confort, duree, ...)
	 */
	public void computeComposedJourney(final String from, final String to, final int departure,
			final String preference) {
		final List<ComposedJourney> journeys = new ArrayList<>();
		final boolean result = catalogs.findIndirectJourney(from, to, departure, 120, new ArrayList<Journey>(),
				new ArrayList<String>(), journeys);

		if (!result) {
			println("no journey found !!!");
		}
		if (result) {
			println("here is my results : ");
         Stream<ComposedJourney> strCJ = journeys.stream();
         OptionalDouble moy = strCJ.mapToInt(ComposedJourney::getDuration).average();
         final double avg = moy.getAsDouble();
         println("duree moyenne = " + avg );
			journeys.forEach(j -> window.println(j.toString()));
			//TODO: complete the switch with the appropriate codes
			switch (preference) {
			case "duration":
				Collections.sort(journeys, Comparator.comparingInt(ComposedJourney::getDuration));
				break;
			case "confort":
			   //TODO: complete
				break;
			case "cost":
            //TODO: complete
				break;
			case "duration-cost":
            //TODO: complete
				break;
			default:
				Collections.sort(journeys, this::sortbyCostAndConfort);
				break;
			}
			myJourney = journeys.get(0);
			println("I choose this journey : " + myJourney);
		}
	}

	/** a comparator based equitably on the cost and the confort */
	private int sortbyCostAndConfort(ComposedJourney journeyToSort, ComposedJourney otherJourney) {
	   //TODO:complete the code
      double deltaCost = 0d;
      double deltaConfort = 0d;
		return (int) (deltaCost + deltaConfort);
	}

	/** get event from the GUI */
	@Override
	protected void onGuiEvent(final GuiEvent eventFromGui) {
		if (eventFromGui.getType() == TravellerAgent.EXIT) {
			doDelete();
		}
		if (eventFromGui.getType() == TravellerAgent.BUY_TRAVEL) {
			addBehaviour(new ContractNetAchat(this, new ACLMessage(ACLMessage.CFP),  (String) eventFromGui.getParameter(0), (String) eventFromGui.getParameter(1), (Integer) eventFromGui.getParameter(2), (String) eventFromGui.getParameter(3)));
		}
	}

	/**
	 * @return the vendeurs
	 */
	public List<AID> getVendeurs() {
		return (ArrayList<AID>)vendeurs.clone();
	}

	/**
	 * @param vendeurs
	 *            the vendeurs to set
	 */
	public void setVendeurs(final List<AID> _vendeurs) {
		vendeurs.clear();
		vendeurs.addAll(_vendeurs);
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


	public ComposedJourney getMyJourney() {
		return myJourney;
	}


	public void setMyJourney(ComposedJourney myJourney) {
		this.myJourney = myJourney;
	}

}
