package agencesVoyages.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * class that represents a catalog of journeys<br>
 * contains function to search all the possible ways between two point start and
 * stop
 *
 * @author emmanueladam
 * @version 1.1
 */
@SuppressWarnings("serial")
public class JourneysList implements Serializable {
    /**
     * catalog of journeys from a departure (the key of the hashtable)
     */
    private final Map<String, ArrayList<Journey>> catalog;

    public JourneysList() {
        catalog = new HashMap<>();
    }

    public static void main(String[] args) {
        JourneysList journeysList = new JourneysList();
        journeysList.addJourney("Val", "Lille", "car", 1440, 30);
        journeysList.addJourney("Val", "Lille", "train", 1440, 40);
        journeysList.addJourney("Val", "Lille", "car", 1510, 30);
        journeysList.addJourney("Lille", "Dunkerque", "car", 1500, 40);
        journeysList.addJourney("Lille", "Dunkerque", "car", 1600, 40);
        journeysList.addJourney("Lille", "Dunkerque", "car", 1630, 40);
        journeysList.addJourney("Dunkerque", "Bray-Dunes", "car", 1700, 10);
        journeysList.addJourney("Dunkerque", "Bray-Dunes", "car", 1710, 20);
        System.out.println(journeysList);
        ArrayList<Journey> search = journeysList.findDirectJourneys("val", "lille");
        System.out.println(search);
        System.out.println("----");
        // search = journeysList.findIndirectJourney("val", "dunkerque", 1450);
        // System.out.println(search);
        System.out.println("----");
        ArrayList<ComposedJourney> journeys = new ArrayList<>();
        journeysList.findIndirectJourney("val", "Bray-Dunes", 1400, 90, new ArrayList<>(),
                new ArrayList<>(), journeys);
        System.out.println(journeys);

    }

    /**
     * add a journey into the catalog
     *
     * @param _start         departure
     * @param _stop          arrival
     * @param _means         car, bus, train, ....
     * @param _departureDate departure date of the journey
     * @param _duration      duration of the journey
     */
    public void addJourney(String _start, String _stop, String _means, int _departureDate, int _duration) {
        var j = new Journey(_start.toUpperCase(), _stop.toUpperCase(), _means.toUpperCase(), _departureDate,
                _duration);
        catalog.compute(j.start, (s, l) -> {
            if (l == null) l = new ArrayList<>();
            l.add(j);
            return l;
        });
    }

    /**
     * add a journey into the catalog
     *
     * @param j the journey to add
     */
    public void addJourney(Journey j) {
        catalog.compute(j.start, (s, l) -> {
            if (l == null) l = new ArrayList<>();
            l.add(j);
            return l;
        });
    }

    /**
     * add catalog of journeys into the catalog
     *
     * @param _list catalog of journeys to add
     */
    public void addJourneys(JourneysList _list) {
        _list.catalog.forEach(
                (s, l) -> catalog.merge(s, l, (l1, l2) -> {
                    l1.addAll(l2);
                    return l1;
                }));
    }

    /**
     * find all the existing direct journeys between 'start' and 'stop'
     *
     * @param start departure
     * @param stop  arrival
     * @return list of all the direct journeys between start and stop
     */
    ArrayList<Journey> findDirectJourneys(String start, String stop) {
        ArrayList<Journey> result = null;
        var list = catalog.get(start.toUpperCase());
        if (list != null) {
            result = new ArrayList<>(List.copyOf(list));
            result.removeIf(v -> !v.stop.equalsIgnoreCase(stop));
            if (result.isEmpty()) result = null;
        }
        return result;
    }

    /**
     * compute a direct or indirect journey from start to stop from a given date
     * to the given date + late (in mn.)<br>
     * in the following usage, if result is true, then journeys contains all the
     * possible composed journeys from 'from' to 'to' between departure and
     * departure + 120mn
     *
     * <pre>
     *  <code>
     * final {@code ArrayList<ComposedJourney>} journeys = new ArrayList<>();
     * final boolean result = catalogs.findIndirectJourney(from, to, departure, 120,
     *                                          {@code new ArrayList<Journey>(), new ArrayList<String>(), journeys});
     * </code>
     * </pre>
     *
     * @param start          departure
     * @param stop           arrival
     * @param date           ideal departure date
     * @param late           additional allowed time added to the departure date (in mn.)
     *                       or to wait between 2 journeys
     * @param currentJourney current journey being build
     * @param via            list of cities include in the journey
     * @param results        list of all the possible journeys
     * @return true if at least one journey has been found
     */
    public boolean findIndirectJourney(String start, String stop, int date, int late, ArrayList<Journey> currentJourney,
                                       List<String> via, List<ComposedJourney> results) {
        boolean result;
        via.add(start.toUpperCase());
        var list = catalog.get(start.toUpperCase());
        if (list == null) return false;
        for (Journey j : list) {
            if (j.departureDate >= date
                    && j.departureDate <= Journey.addTime(date, late))
                if (j.stop.equalsIgnoreCase(stop)) {
                    currentJourney.add(j);
                    ComposedJourney compo = new ComposedJourney();
                    compo.addJourneys((ArrayList<Journey>) currentJourney.clone());
                    results.add(compo);
                    currentJourney.remove(currentJourney.size() - 1);
                } else {
                    if (!via.contains(j.stop.toUpperCase())) {
                        currentJourney.add(j);
                        findIndirectJourney(j.stop.toUpperCase(), stop.toUpperCase(), j.arrivalDate, late, currentJourney, via, results);
                        via.remove(j.stop.toUpperCase());
                        currentJourney.remove(j);
                    }
                }
        }
        result = !results.isEmpty();
        return result;
    }

    /**
     * @return true is the catalog is null or contains no key
     */
    public boolean isEmpty() {
        var empty = (this.catalog == null);
        empty = empty || catalog.keySet().isEmpty();
        return empty;
    }

    /**
     * @return the journeys from a city
     */
    public ArrayList<Journey> getJourneysFrom(String from) {
        return catalog.get(from);
    }

    /**
     * remove from all the journeys of the map, those which respect the predicate
     *
     * @param p predicate used to filter the journey
     */
    public void removeIf(Predicate<Journey> p) {
        catalog.values().forEach(l -> l.removeIf(p));
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        var lists = catalog.values();
        ArrayList<Journey> list = new ArrayList<>();
        lists.forEach(list::addAll);
        list.forEach(j -> sb.append(j).append("\n"));
        sb.append("---end---");
        return "list of journeys:\n" + sb;
    }

}
