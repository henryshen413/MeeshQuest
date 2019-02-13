package cmsc420.meeshquest.part4;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class RoadAdjacencyList {

	/** map of city names to connected roads */
	final protected TreeMap<String, TreeMap<String, Double>> adjacencyList = new TreeMap<String, TreeMap<String, Double>>(
			new CityNameComparator());
	
	/**
	 * Adds an undirected road to the adjacency list.
	 * 
	 * @param road
	 *            road to be added to adjacency list
	 */
	public void addRoad(Road road) {
		final String start, end;

		start = road.getStart().getName();
		end = road.getEnd().getName();

		addRoadForGeomPoint(road.getDistance(), start, end);
		addRoadForGeomPoint(road.getDistance(), end, start);
	}
	
	public void addEdge(String start, String end, Double distance) {
		addRoadForGeomPoint(distance, start, end);
		addRoadForGeomPoint(distance, end, start);
	}
	
	public void removeGeomPoint(String name) {
		if (!containsGeomPoint(name)) {
			return;
		}
		for (Map.Entry<String, TreeMap<String, Double>> e: adjacencyList.entrySet()) {
			if (e.getValue().containsKey(name)) {
				e.getValue().remove(name);
			}
		}
		adjacencyList.remove(name);
	}

	/**
	 * Returns if the adjacency list contains the road.
	 * 
	 * @param road
	 *            road to be checked
	 * @return <code>true</code> if the road is in the list,
	 *         <code>false</code> otherwise
	 */
	public boolean containsRoad(final Road road) {
		TreeMap<String, Double> roadsForCity = adjacencyList.get(road.getStart().getName());
		if (roadsForCity == null) {
			return false;
		} else {
			return roadsForCity.containsKey(road.getEnd().getName());
		}
	}

	/**
	 * Adds a road to the city's road list.
	 * 
	 * @param road
	 *            road to be added
	 * @param point
	 *            city whose road list the road will be added to
	 */
	private void addRoadForGeomPoint(final Double distance, final String start, final String end) {
		TreeMap<String, Double> roadsForCity = adjacencyList.get(start);

		if (roadsForCity == null) {
			roadsForCity = new TreeMap<String, Double>(new CityNameComparator());
			adjacencyList.put(start, roadsForCity);
		}

		roadsForCity.put(end, distance);
	}

	/**
	 * Removes an undirected road from the adjacency list.
	 * 
	 * @param road
	 *            road to be removed form the adjacency list
	 * @return road removed from the adjacency list
	 */
	public void removeRoad(Road road) {
		if (!containsRoad(road)) {
			return;
		} else {
			final String start, end;

			start = road.getStart().getName();
			end = road.getEnd().getName();

			removeRoadForGeomPoint(start, end);
			removeRoadForGeomPoint(end, start);
		}
	}

	/**
	 * Removes a road from the city's road list.
	 * 
	 * @param road
	 *            road to be removed from the adjacency list
	 * @param city
	 *            city whose road list the road will be removed from
	 */
	private void removeRoadForGeomPoint(final String start, final String end) {
		TreeMap<String, Double> roadsForCity = adjacencyList.get(start);

		if (roadsForCity != null) {
			roadsForCity.remove(end);
			if (roadsForCity.size() == 0) {
				adjacencyList.remove(start);
			}
		}
	}

	/**
	 * Gets a list of the roads connected to a given city.
	 * 
	 * @param city
	 *            city
	 * @return set of connected roads to the city
	 */
	public TreeMap<String, Double> getRoadSet(final String start) {
		final TreeMap<String, Double> roadsForCity = adjacencyList.get(start);

		if (roadsForCity == null) {
			return new TreeMap<String, Double>(new CityNameComparator());
		} else {
			return roadsForCity;
		}
	}

	/**
	 * Gets the number of cities in the road adjacency list.
	 * 
	 * @return number of cities in the list
	 */
	public int getNumberOfGeomPoint() {
		return adjacencyList.size();
	}

	/**
	 * Gets a set of the names of all cities in the adjacency list.
	 * 
	 * @return list of all city names in the adjacency list
	 */
	public Set<String> getGeomPointSet() {
		return adjacencyList.keySet();
	}
	
	public boolean containsGeomPoint(String name) {
		return getGeomPointSet().contains(name);
	}

	/**
	 * Clears the road adjacency list.
	 */
	public void clear() {
		adjacencyList.clear();
	}

	/**
	 * Gets all the connected roads for each city. Useful for debugging
	 * purposes.
	 * 
	 * @return string representing the road adjacency list
	 */
	public String toString() {
		final StringBuilder s = new StringBuilder();
		for (final String startName : adjacencyList.keySet()) {
			s.append(startName);
			s.append(":\n");
			for (Map.Entry<String, Double> e : adjacencyList.get(startName).entrySet()) {
				s.append(e.getKey() + ": " + e.getValue());
				s.append("\n");
			}
			s.append("\n");
		}
		return s.toString();
	}
}

