package agents;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Stream;

import comportements.ContractNetAchat;
import data.ComposedJourney;
import data.JourneysList;
import gui.TravellerGui;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionInitiator;

/**
 * Journey searcher
 * 
 * @author Emmanuel ADAM
 */
@SuppressWarnings("serial")
public class TravellerAgent extends GuiAgent {
	/** code pour ajout de livre par la gui */
	public static final int EXIT = 0;
	/** code pour achat de livre par la gui */
	public static final int BUY_TRAVEL = 1;

	/** liste des vendeurs */
	private ArrayList<AID> vendeurs;

	/** catalog received by the sellers */
	private JourneysList catalogs;

	/** the journey chosen by the agent*/
	private ComposedJourney myJourney;

	/** topic from which the alert will be received */
	private AID topic;

	/** gui */
	private TravellerGui window;

	/** Initialisation de l'agent */
	@Override
	protected void setup() {
		this.window = new TravellerGui(this);
		window.setColor(Color.cyan);
		window.println("Hello! AgentAcheteurCN " + this.getLocalName() + " est pret. ");
		window.setVisible(true);

		vendeurs = new ArrayList<>();
		detectAgences();

		topic = AgentToolsEA.generateTopicAID(this,"TRAFFIC NEWS");
		//ecoute des messages radio
		addBehaviour(new CyclicBehaviour() {
			@Override
			public void action() {
				var msg = myAgent.receive(MessageTemplate.MatchTopic(topic));
				if (msg != null) {
					println("Message recu sur le topic " + topic.getLocalName() + ". Contenu " + msg.getContent()
							+ " émis par " + msg.getSender().getLocalName());
				} else { block();}
			}
		});
		
	}




	/**ecoute des evenement de type enregistrement en tant qu'agence aupres des pages jaunes*/
	private void detectAgences()
	{
		var model = AgentToolsEA.createAgentDescription("travel agency", "seller");
		var msg = DFService.createSubscriptionMessage(this, getDefaultDF(), model, null);
		vendeurs = new ArrayList<>();
		addBehaviour(new SubscriptionInitiator(this, msg) {
			@Override
			protected void handleInform(ACLMessage inform) {
				window.println("Agent "+getLocalName()+": information recues de DF");
				try {
					var results = DFService.decodeNotification(inform.getContent());
					if (results.length > 0) {
						for (DFAgentDescription dfd:results) {
							vendeurs.add(dfd.getName());
							window.println(dfd.getName().getName() + " s'est inscrit en tant qu'agence");
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

	public void computeComposedJourney(final String from, final String to, final int departure,
			final String preference) {
		final List<ComposedJourney> journeys = new ArrayList<>();
		final boolean result = catalogs.findIndirectJourney(from, to, departure, 120, new ArrayList<>(),
				new ArrayList<>(), journeys);

		if (!result) {
			println("no journey found !!!");
		}
		if (result) {
			// println("here is my results : ");
			// journeys.forEach(j -> window.println(j.toString()));
			switch (preference) {
			case "duration":
				Stream<ComposedJourney> strCJ = journeys.stream();
				OptionalDouble moy = strCJ.mapToInt(ComposedJourney::getDuration).average();
				final double avg = moy.getAsDouble();
				println("duree moyenne = " + avg );//+ ", moy au carre = " + avg * avg);
				journeys.sort(Comparator.comparingInt(ComposedJourney::getDuration));
				break;
			case "confort":
				journeys.sort(Comparator.comparingInt(ComposedJourney::getConfort).reversed());
				break;
			case "cost":
				journeys.sort(Comparator.comparingDouble(ComposedJourney::getCost));
				break;
			case "duration-cost":
				//TODO: replace below to make a compromise between cost and confort...
				journeys.sort(Comparator.comparingDouble(ComposedJourney::getCost));
				break;
			default:
				journeys.sort(Comparator.comparingDouble(ComposedJourney::getCost));
				break;
			}
			myJourney = journeys.get(0);
			println("I choose this journey : " + myJourney);
		}
	}

	/** get event from the GUI */
	@Override
	protected void onGuiEvent(final GuiEvent eventFromGui) {
		if (eventFromGui.getType() == TravellerAgent.EXIT) {
			doDelete();
		}
		if (eventFromGui.getType() == TravellerAgent.BUY_TRAVEL) {
			addBehaviour(new ContractNetAchat(this, new ACLMessage(ACLMessage.CFP),
					(String) eventFromGui.getParameter(0), (String) eventFromGui.getParameter(1),
					(Integer) eventFromGui.getParameter(2), (String) eventFromGui.getParameter(3)));
		}
	}

	/**
	 * @return the vendeurs
	 */
	public List<AID> getVendeurs() {
		return (ArrayList<AID>)vendeurs.clone();
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

	/** set the list of journeys */
	public void setCatalogs(final JourneysList catalogs) {
		this.catalogs = catalogs;
	}


	public ComposedJourney getMyJourney() {
		return myJourney;
	}

}
