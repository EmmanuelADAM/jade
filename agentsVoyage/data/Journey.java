package data;

import java.io.Serializable;

/**
 * class that represents a journey<br>
 * departure and arrival point, departure and arrival date, duration, means
 * (car, bus, ..), cost, co2, confort, provider
 * 
 * @author emmanueladam
 *
 */
@SuppressWarnings("serial")
public class Journey implements Cloneable, Serializable, Comparable<Journey> {
	/** origin */
	protected String start;
	/** destination */
	protected String stop;
	/** transport means */
	protected String means;
	/** duration of the journey, in minutes */
	protected int duration;
	/** date of departure, format hhmm */
	protected int departureDate;
	/** date of arrival, format hhmm */
	protected int arrivalDate;
	/** cost in money */
	protected double cost;
	/** cost in co2 */
	protected int co2;
	/** level of confort (0 = worst) */
	protected int confort;
	/** name of the service that propose the journey */
	protected String proposedBy;
	/** nb of remaining places (not used) */
	protected int places = 1;

	Journey() {
	}

	public Journey(final String _start, final String _stop, final String _means, final int _departureDate,
			final int _duration) {
		start = _start;
		stop = _stop;
		means = _means;
		departureDate = _departureDate;
		duration = _duration;
		arrivalDate = Journey.addTime(departureDate, duration);
	}

	Journey(final String _start, final String _stop, final String _means, final int _departureDate, final int _duration,
			final double _cost) {
		this(_start, _stop, _means, _departureDate, _duration);
		cost = _cost;
	}

	public Journey(final String _start, final String _stop, final String _means, final int _departureDate,
			final int _duration, final double _cost, final int _co2, final int _confort) {
		this(_start, _stop, _means, _departureDate, _duration, _cost);
		co2 = _co2;
		confort = _confort;
	}

	public Journey(final String _start, final String _stop, final String _means, final int _departureDate,
		       final int _duration, final double _cost, final int _co2, final int _confort, String _proposedBy) {
		this(_start, _stop, _means, _departureDate, _duration, _cost, _co2, _confort);
		proposedBy = _proposedBy;
	}

	public Journey clone() {
		Journey clone = null;
		try { clone = (Journey) super.clone(); }
		catch (Exception e){e.printStackTrace();}
		return clone;
	}


	/**
	 * @param time
	 *            a date in the format hhmm (the two last digits corresponds to
	 *            the minutes)
	 * @param minutes
	 *            nb of mn to add
	 * @return the result of the addition time + x in the format hhmm
	 */
	public static int addTime(final int time, final int minutes) {
		final int h = time / 100;
		final int mn = time - h * 100;
		return (h + (mn + minutes) / 60) * 100 + (mn + minutes) % 60;
	}

	public String getStart() {
		return start;
	}

	public void setStart(final String start) {
		this.start = start;
	}

	public String getStop() {
		return stop;
	}

	public void setStop(final String stop) {
		this.stop = stop;
	}

	public String getMeans() {
		return means;
	}

	public void setMeans(final String means) {
		this.means = means;
	}

	public int getTime() {
		return duration;
	}

	public void setTime(final int time) {
		this.duration = time;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(final double cost) {
		this.cost = cost;
	}

	public int getCo2() {
		return co2;
	}

	public void setCo2(final int co2) {
		this.co2 = co2;
	}

	public int getConfort() {
		return confort;
	}

	public void setConfort(final int confort) {
		this.confort = confort;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(final int duration) {
		this.duration = duration;
	}

	public int getDepartureDate() {
		return departureDate;
	}

	/**
	 * set the departure date, and set the arrival date related
	 * 
	 * @param departureDate
	 *            departure date to set
	 */
	public void setDepartureDate(final int departureDate) {
		this.departureDate = departureDate;
		this.arrivalDate = Journey.addTime(departureDate, duration);
	}

	public int getArrivalDate() {
		return arrivalDate;
	}

	public void setArrivalDate(final int arrivalDate) {
		this.arrivalDate = arrivalDate;
	}

	public String getProposedBy() {
		return proposedBy;
	}

	public void setProposedBy(final String proposedBy) {
		this.proposedBy = proposedBy;
	}

    /**some fields to improve the memory management*/
    private static String TO = " to ";
    private static String TRAJECTFROM = "traject from ";
    private static String BY = " by ";
    private static String DEPARTURE = ", departure: ";
    private static String ARRIVAL = ", arrival:";
    private static String COST = ", cost = ";
    private static String PROPOSEDBY = ", proposed by ";
	@Override
	public String toString() {
	    return new StringBuilder(TRAJECTFROM).append(start).append(TO).
                append(stop).append(BY).append(means).append(DEPARTURE).
                append(departureDate).append(ARRIVAL).append(arrivalDate).
                append(COST).append(cost).append(PROPOSEDBY).append(proposedBy).toString();
@	}

	public int getPlaces() {
		return places;
	}

	public void setPlaces(int places) {
		this.places = places;
	}

	@Override
	public int compareTo(Journey o) {
		return (int) (cost - o.cost);
	}

}
