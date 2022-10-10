package agencesVoyages.data;

import java.io.Serializable;
import java.util.Objects;

/**
 * class that represents a journey<br>
 * departure and arrival point, departure and arrival date, duration, means
 * (car, bus, ..), cost, co2, confort, provider
 *
 * @author emmanueladam
 */
@SuppressWarnings("serial")
public class Journey implements Cloneable, Serializable, Comparable<Journey> {
    /**
     * some fields to improve the memory management
     */
    private static final String TO = " to ";
    private static final String TRAJECTFROM = "traject from ";
    private static final String BY = " by ";
    private static final String DEPARTURE = ", departure: ";
    private static final String ARRIVAL = ", arrival:";
    private static final String COST = ", cost = ";
    private static final String PROPOSEDBY = ", proposed by ";
    /**
     * origin
     */
    String start;
    /**
     * destination
     */
    String stop;
    /**
     * transport means
     */
    String means;
    /**
     * duration of the journey, in minutes
     */
    int duration;
    /**
     * date of departure, format hhmm
     */
    int departureDate;
    /**
     * date of arrival, format hhmm
     */
    int arrivalDate;
    /**
     * cost in money
     */
    double cost;
    /**
     * cost in co2
     */
    int co2;
    /**
     * level of confort (0 = worst)
     */
    int confort;
    /**
     * name of the service that propose the journey
     */
    String proposedBy;
    /**
     * nb of remaining places (not used)
     */
    private int places = 1;

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

    /**
     * @param time    a date in the format hhmm (the two last digits corresponds to
     *                the minutes)
     * @param minutes nb of mn to add
     * @return the result of the addition time + x in the format hhmm
     */
    public static int addTime(final int time, final int minutes) {
        final int h = time / 100;
        final int mn = time - h * 100;
        return (h + (mn + minutes) / 60) * 100 + (mn + minutes) % 60;
    }

    public static void main(final String... args) {
        final Journey test = new Journey("Val", "Lille", "car", 1440, 90);
        System.out.println(test);
    }

    public Journey clone() {
        Journey clone = null;
        try {
            clone = (Journey) super.clone();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clone;
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
     * @param departureDate departure date to set
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


    public int getPlaces() {
        return places;
    }

    public void setPlaces(int places) {
        this.places = places;
    }

    @Override
    public String toString() {
        return new StringBuilder(Journey.TRAJECTFROM).append(start).append(Journey.TO).
                append(stop).append(Journey.BY).append(means).append(Journey.DEPARTURE).
                append(departureDate).append(Journey.ARRIVAL).append(arrivalDate).
                append(Journey.COST).append(cost).append(Journey.PROPOSEDBY).append(proposedBy).toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Journey journey = (Journey) o;

        if (departureDate != journey.departureDate) return false;
        if (!Objects.equals(start, journey.start)) return false;
        if (!Objects.equals(stop, journey.stop)) return false;
        return Objects.equals(means, journey.means);
    }



    @Override
    public int compareTo(Journey o) {
        return (int) (cost - o.cost);
    }
}
