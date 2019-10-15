package data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

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
	/** catalog of journeys from a departure (the key of the hashtable) */
	Map<String, ArrayList<Journey>> catalog;

	public JourneysList() {
		catalog = new HashMap<>();
	}

	/**@return the catalog of journeys in a HashMap (Town, Journeys from Town)*/
	public Map<String, ArrayList<Journey>> getCatalog() {return catalog;}


	/**
	 * add a journey into the catalog
	 * 
	 * @param j
	 *            the journey to add
	 */
	public void addJourney(Journey j) {
	   catalog.compute(j.getStart(), 
	         (k,v)-> { if(v==null) {v= new ArrayList<>();}
	         v.add(new Journey(j)); return v;});
	}

	/**
	 * add catalog of journeys into the catalog
	 * 
	 * @param _list
	 *            catalog of journeys to add
	 */
	public void addJourneys(JourneysList _list) {
		for (List<Journey> l : _list.catalog.values())
			for (Journey j : l) addJourney(j);
	}
	/**
	 * add a journey into the catalog
	 *
	 * @param _start
	 *            departure
	 * @param _stop
	 *            arrival
	 * @param _means
	 *            car, bus, train, ....
	 * @param _departureDate
	 *            departure date of the journey
	 * @param _duration
	 *            duration of the journey 
	 * @param _cost 
	 *            cost of the journey 
	 * @param _co2 
	 *            cost in co2 for the journey 
	 * @param _confort 
	 *            confort of the journey 
	 * @param _proposedBy 
	 *            name of the agent that proposes the journey 
	 */
	public void addJourney(String _start, String _stop, String _means, int _departureDate, int _duration, double _cost,
						   int _co2, int _confort, String _proposedBy) {
		ArrayList<Journey> list = catalog.get(_start.toUpperCase());
		if (list == null) {
			list = new ArrayList<Journey>();
			list.add(new Journey(_start.toUpperCase(), _stop.toUpperCase(), _means.toUpperCase(), _departureDate,
					_duration, _cost, _co2, _confort, _proposedBy));
			catalog.put(_start.toUpperCase(), list);
		} else
			list.add(new Journey(_start.toUpperCase(), _stop.toUpperCase(), _means.toUpperCase(), _departureDate,
					_duration, _cost, _co2, _confort, _proposedBy));
	}

	/**
	 * find all the existing direct journeys between 'start' and 'stop'
	 * 
	 * @param start
	 *            departure
	 * @param stop
	 *            arrival
	 * @return list of all the direct journeys between start and stop
	 */
	List<Journey> findDirectJourneys(String start, String stop) {
		List<Journey> result = new ArrayList<>();
		List<Journey> list = catalog.get(start.toUpperCase());
		if (list != null) {
			for (Journey j : list) {
				if (j.start.equalsIgnoreCase(start) && j.stop.equalsIgnoreCase(stop))
					result.add(j);
			}
		}
		if (result.isEmpty())
			result = null;
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
	*final {@code ArrayList<ComposedJourney>} journeys = new ArrayList<>();
	*final boolean result = catalogs.findIndirectJourney(from, to, departure, 120, 
	*                                          {@code new ArrayList<Journey>(), new ArrayList<String>(), journeys});
	* </code>
	 * </pre>
	 * 
	 * @param start
	 *            departure
	 * @param stop
	 *            arrival
	 * @param date
	 *            ideal departure date
	 * @param late
	 *            additional allowed time added to the departure date (in mn.)
	 *            or to wait between 2 journeys
	 * @param currentJourney
	 *            current journey being build
	 * @param via
	 *            list of cities include in the journey
	 * @param results
	 *            list of all the possible journeys
	 * @return true if at least one journey has been found
	 */
	public boolean findIndirectJourney(String start, String stop, int date, int late, ArrayList<Journey> currentJourney,
			List<String> via, List<ComposedJourney> results) {
		boolean result;
		via.add(start.toUpperCase());
		ArrayList<Journey> list = catalog.get(start.toUpperCase());
		if (list == null)
			return false;
		for (Journey j : list) {
			if (j.start.equalsIgnoreCase(start) && j.departureDate >= date
					&& j.departureDate <= Journey.addTime(date, late))
				if (j.stop.equalsIgnoreCase(stop)) {
					currentJourney.add(j);
					ComposedJourney compo = new ComposedJourney();
					compo.addJourneys((ArrayList<Journey>) currentJourney.clone());
					results.add(compo);
					currentJourney.remove(currentJourney.size() - 1);
				} else {
					if (!via.contains(j.stop)) {
						currentJourney.add(j);
						findIndirectJourney(j.stop, stop, j.arrivalDate, late, currentJourney, via, results);
						via.remove(j.stop);
						currentJourney.remove(j);
					}
				}
		}
		result = !results.isEmpty();
		return result;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		Collection<ArrayList<Journey>> lists = catalog.values();
		ArrayList<Journey> list = new ArrayList<>();
		for (ArrayList<Journey> l : lists)
			list.addAll(l);
		for (Journey j : list) {
			sb.append(j).append("\n");
		}
		sb.append("---end---");
		return "list of journeys:\n" + sb.toString();
	}

}
