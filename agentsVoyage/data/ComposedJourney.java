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
	ArrayList<Journey> journeys;
	/** origin */
	protected String start;
	/** destination */
	protected String stop;
	/** transport means */
	protected String means;
	/** duration of the journey, in minutes */
	protected int duration;
	/** duration of the journey(normalized) */
	protected double normDuration;
	/** date of departure, format hhmm */
	protected int departureDate;
	/** date of arrival, format hhmm */
	protected int arrivalDate;
	/** cost in money */
	protected double cost;
	/** cost in money (normalized) */
	protected double normCost;
	/** cost in co2 */
	protected int co2;
	/** cost in co2 (normalized) */
	protected double normCo2;
	/** level of confort (0 = worst) */
	protected int confort;
	/** level of confort (0 = worst) (normalized) */
	protected double normConfort;

	/** nb of journeys */
	int nbVia = -1;
	/** list of proposers */
	ArrayList<String> proposedBy;
	/** responsible of the composed journey */
	AID proposer;

	public ComposedJourney() {
		journeys = new ArrayList<>();
		proposedBy = new ArrayList<>();
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
		proposedBy.add(oneJourney.proposedBy);

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
			proposedBy.add(j.proposedBy);
			nbVia++;
		}
		confort = confort / nb;
		start = journeys.get(0).start;
		departureDate = journeys.get(0).departureDate;
		stop = journeys.get(nb - 1).stop;
		arrivalDate = journeys.get(nb - 1).arrivalDate;
		duration = ((arrivalDate / 100) * 60 + arrivalDate % 100) - ((departureDate / 100) * 60 + departureDate % 100);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("journey from ").append(start).append(" to ").append(stop).append(", duration  ").append(duration);
		sb.append(" mn, departure: ").append(departureDate).append(", arrival:").append(arrivalDate);
		sb.append(", cost = ").append(cost).append("\n");
		for (Journey j : journeys)
			sb.append("-- ").append(j).append("\n");
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
		journeysList.findIndirectJourney("val", "bray-dunes", 1400, 60, new ArrayList<Journey>(),
				new ArrayList<String>(), journeys);
		System.out.println(journeys);
		// ComposedJourney cj = new ComposedJourney();
		// cj.addJourneys(journeys.get(0));
		// System.out.println(cj);
	}
}