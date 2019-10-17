package data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jade.core.AID;

/**
 * a composed journey is composed of several journeys ! it represents a junction
 * of journey from a departure to an arrival. the arrival date is the arrival
 * date of the last journey, the cost is the sum of the cost, the confort is the
 * average of the confort..
 */
@SuppressWarnings("serial")
public class ComposedJourney implements Serializable {
	private List<Journey> journeys;
	/** origin */
	private String start;
	/** destination */
	private String stop;
	/** duration of the journey, in minutes */
	private int duration;
	/** duration of the journey(normalized) */
	private double normDuration;
	/** date of departure, format hhmm */
	private int departureDate;
	/** date of arrival, format hhmm */
	private int arrivalDate;
	/** cost in money */
	private double cost;
	/** cost in money (normalized) */
	private double normCost;
	/** cost in co2 */
	private int co2;
	/** cost in co2 (normalized) */
	private double normCo2;
	/** level of confort (0 = worst) */
	private int confort;
	/** level of confort (0 = worst) (normalized) */
	private double normConfort;

	/** nb of journeys */
	private int nbVia = -1;

	/** responsible of the composed journey */
	private AID proposer;


    public ComposedJourney() {
		journeys = new ArrayList<>();
	}

	/**
	 * add a journey add the end of the list of journeys (the departure of the
	 * added journey does not have to be the arrival of the last journey of the
	 * list)
	 * 
	 * @param oneJourney
	 *            journey to add at the end of the current composed journey
	 */
	public void add(Journey oneJourney) {
		journeys.add(oneJourney);
		nbVia++;
		cost += oneJourney.getCost();
		co2 += oneJourney.getCo2();
		confort *= (nbVia - 1);
		confort += oneJourney.getConfort();
		confort /= nbVia;

		if (nbVia == 0) {
			start = journeys.get(0).start;
			departureDate = journeys.get(0).departureDate;
		}
		stop = oneJourney.stop;
		arrivalDate = oneJourney.arrivalDate;
		duration = ((arrivalDate / 100) * 60 + arrivalDate % 100) - ((departureDate / 100) * 60 + departureDate % 100);
	}

	/**
	 * add a list of journeys (supposed to be arranged consecutively)
	 * 
	 * @param journeysList
	 *            list of journeys to add at the end of the current composed
	 *            journey
	 */
	public void addJourneys(List<Journey> journeysList) {
		journeys.addAll(journeysList);
		int nb = journeys.size();
		for (Journey j : journeys) {
			cost += j.getCost();
			co2 += j.getCo2();
			confort += j.getConfort();
			nbVia++;
		}
		confort = confort / nb;
		start = journeys.get(0).start;
		departureDate = journeys.get(0).departureDate;
		stop = journeys.get(nb - 1).stop;
		arrivalDate = journeys.get(nb - 1).arrivalDate;
		duration = ((arrivalDate / 100) * 60 + arrivalDate % 100) - ((departureDate / 100) * 60 + departureDate % 100);
	}

	//some String constants to improve the memory management
	private static String JOURNEYFROM = "journey from ";
	private static String TO = " to ";
	private static String DURATION = ", duration  ";
	private static String DEPARTURE = " mn, departure: ";
	private static String ARRIVAL = ", arrival:";
	private static String COST = ", cost = ";
	private static String LINE = "\n";
	private static String SEP = "--";
    @Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(ComposedJourney.JOURNEYFROM).append(start).append(ComposedJourney.TO).append(stop).
				append(ComposedJourney.DURATION).append(duration).append(ComposedJourney.DEPARTURE).
				append(departureDate).append(ComposedJourney.ARRIVAL).append(arrivalDate).
				append(ComposedJourney.COST).append(cost).append(ComposedJourney.LINE);
		for (Journey j : journeys)
			sb.append(ComposedJourney.SEP).append(j).append(ComposedJourney.LINE);
		return sb.toString();
	}

	public int getDuration() {
		return duration;
	}

	public double getCost() {
		return cost;
	}

	public int getCo2() {
		return co2;
	}

	public int getConfort() {
		return confort;
	}

	public int getNbVia() {
		return nbVia;
	}

	public AID getProposer() {
		return proposer;
	}

	public void setProposer(AID proposer) {
		this.proposer = proposer;
	}

	

	/**
	 * @return the normCost
	 */
	public double getNormCost() {
		return normCost;
	}

	/**
	 * @param _normCost
	 *            the normCost to set
	 */
	public void setNormCost(double _normCost) {
		normCost = _normCost;
	}

	/**
	 * @return the normCo2
	 */
	public double getNormCo2() {
		return normCo2;
	}

	/**
	 * @param _normCo2
	 *            the normCo2 to set
	 */
	public void setNormCo2(double _normCo2) {
		normCo2 = _normCo2;
	}

	/**
	 * @return the normConfort
	 */
	public double getNormConfort() {
		return normConfort;
	}

	/**
	 * @param _normConfort
	 *            the normConfort to set
	 */
	public void setNormConfort(double _normConfort) {
		normConfort = _normConfort;
	}

	/**
	 * @return the normDuration
	 */
	public double getNormDuration() {
		return normDuration;
	}

	/**
	 * @return the normDuration
	 */
	public List<Journey> getJourneys() {
		return journeys;
	}


	/**
	 * @param _normDuration
	 *            the normDuration to set
	 */
	public void setNormDuration(double _normDuration) {
		normDuration = _normDuration;
	}
	public static void main(String[] args) {
		JourneysList journeysList = new JourneysList();
		journeysList.addJourney(new Journey("Val", "Lille", "car", 1440, 30, 10));
		journeysList.addJourney(new Journey("Val", "Lille", "train", 1440, 40, 10));
		journeysList.addJourney(new Journey("Val", "Lille", "car", 1510, 30, 10));
		journeysList.addJourney(new Journey("Lille", "Dunkerque", "car", 1500, 40, 10));
		journeysList.addJourney(new Journey("Lille", "Dunkerque", "car", 1600, 40, 10));
		journeysList.addJourney(new Journey("Lille", "Dunkerque", "car", 1630, 40, 10));
		journeysList.addJourney(new Journey("Dunkerque", "Bray-Dunes", "car", 1700, 10, 10));
		journeysList.addJourney(new Journey("Dunkerque", "Bray-Dunes", "car", 1710, 20, 10));
		System.out.println(journeysList);
		List<ComposedJourney> journeys = new ArrayList<>();
		journeysList.findIndirectJourney("val", "bray-dunes", 1400, 60, new ArrayList<>(),
				new ArrayList<>(), journeys);
		System.out.println(journeys);
		// ComposedJourney cj = new ComposedJourney();
		// cj.addJourneys(journeys.get(0));
		// System.out.println(cj);
	}
}
