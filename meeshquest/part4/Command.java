package cmsc420.meeshquest.part4;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;

import cmsc420.meeshquest.part4.MeeshQuest;

public class Command extends MeeshQuest {
	public static void createCity(Integer id, String n, Integer lx, Integer ly, Integer rx, Integer ry, Integer r,
			String c) {
		command = results.createElement("command");
		parameters = results.createElement("parameters");
		name = results.createElement("name");
		localX = results.createElement("localX");
		localY = results.createElement("localY");
		remoteX = results.createElement("remoteX");
		remoteY = results.createElement("remoteY");
		radius = results.createElement("radius");
		color = results.createElement("color");
		output = results.createElement("output");

		command.setAttribute("name", "createCity");
		name.setAttribute("value", n);
		localX.setAttribute("value", lx.toString());
		localY.setAttribute("value", ly.toString());
		remoteX.setAttribute("value", rx.toString());
		remoteY.setAttribute("value", ry.toString());
		radius.setAttribute("value", r.toString());
		color.setAttribute("value", c);

		parameters.appendChild(name);
		parameters.appendChild(localX);
		parameters.appendChild(localY);
		parameters.appendChild(remoteX);
		parameters.appendChild(remoteY);
		parameters.appendChild(radius);
		parameters.appendChild(color);

		if (id != null) {
			command.setAttribute("id", String.valueOf(id));
		}

		Point2D.Float p = new Point2D.Float(rx, ry);
		Metropole mpole;
		if (worldMap.containsKey(p)) {
			mpole = worldMap.get(p);
		} else {
			mpole = new Metropole(rx.floatValue(), ry.floatValue(), width, height, pmOrder);
			worldMap.put(p, mpole);
		}

		City city = new City(n, lx.floatValue(), ly.floatValue(), rx.floatValue(), ry.floatValue(), r, c);

		if (mpole.geomPtS.contains(city)) {
			error = results.createElement("error");
			error.setAttribute("type", "duplicateCityCoordinates");
			error.appendChild(command);
			error.appendChild(parameters);
			results_base.appendChild(error);
		} else if (geomNameMap.containsKey(n)) {
			error = results.createElement("error");
			error.setAttribute("type", "duplicateCityName");
			error.appendChild(command);
			error.appendChild(parameters);
			results_base.appendChild(error);
		} else {
			mpole.cityM.put(n, city);
			mpole.cityS.add(city);
			mpole.geomPtS.add(city);
			avlTree.put(n, city);
			geomNameMap.put(n, mpole);
			cityMap.put(n, city);

			success = results.createElement("success");

			success.appendChild(command);
			success.appendChild(parameters);
			success.appendChild(output);
			results_base.appendChild(success);
		}
	}

	public static void printAvlTree(Integer id) {
		command = results.createElement("command");
		parameters = results.createElement("parameters");

		command.setAttribute("name", "printAvlTree");

		if (id != null) {
			command.setAttribute("id", String.valueOf(id));
		}

		if (avlTree.size() == 0) {
			error = results.createElement("error");
			error.setAttribute("type", "emptyTree");
			error.appendChild(command);
			error.appendChild(parameters);
			results_base.appendChild(error);
		} else {
			success = results.createElement("success");
			output = results.createElement("output");

			avlTree.printXml(output, results);
			success.appendChild(command);
			success.appendChild(parameters);
			success.appendChild(output);
			results_base.appendChild(success);
		}
	}

	public static void nearestCity(Integer id, Integer lx, Integer ly, Integer rx, Integer ry) {
		command = results.createElement("command");
		parameters = results.createElement("parameters");
		localX = results.createElement("localX");
		localY = results.createElement("localY");
		remoteX = results.createElement("remoteX");
		remoteY = results.createElement("remoteY");

		command.setAttribute("name", "nearestCity");
		localX.setAttribute("value", String.valueOf(lx));
		localY.setAttribute("value", String.valueOf(ly));
		remoteX.setAttribute("value", String.valueOf(rx));
		remoteY.setAttribute("value", String.valueOf(ry));
		if (id != null) {
			command.setAttribute("id", String.valueOf(id));
		}

		parameters.appendChild(localX);
		parameters.appendChild(localY);
		parameters.appendChild(remoteX);
		parameters.appendChild(remoteY);

		Metropole m = worldMap.get(new Point2D.Float(rx, ry));
		if (m == null || m.cityS.size() == 0) {
			error = results.createElement("error");
			error.setAttribute("type", "cityNotFound");
			error.appendChild(command);
			error.appendChild(parameters);
			results_base.appendChild(error);
		} else {
			City c = m.pmQuadTree.nearestCity(lx, ly, false);
			if (c == null) {
				error = results.createElement("error");
				error.setAttribute("type", "cityNotFound");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else {
				success = results.createElement("success");
				output = results.createElement("output");
				Element city = results.createElement("city");

				city.setAttribute("name", c.getName());
				city.setAttribute("localX", String.valueOf(Math.round(c.getX())));
				city.setAttribute("localY", String.valueOf(Math.round(c.getY())));
				city.setAttribute("remoteX", String.valueOf(Math.round(m.getX())));
				city.setAttribute("remoteY", String.valueOf(Math.round(m.getY())));
				city.setAttribute("color", c.getColor());
				city.setAttribute("radius", c.getRadius().toString());

				output.appendChild(city);
				success.appendChild(command);
				success.appendChild(parameters);
				success.appendChild(output);
				results_base.appendChild(success);
			}
		}
	}

	public static void deleteCity(Integer id, String cName) {
		command = results.createElement("command");
		parameters = results.createElement("parameters");
		name = results.createElement("name");
	
		command.setAttribute("name", "deleteCity");
		name.setAttribute("value", cName);
	
		if (id != null) {
			command.setAttribute("id", String.valueOf(id));
		}
	
		parameters.appendChild(name);
	
		if (!cityMap.containsKey(cName)) {
			error = results.createElement("error");
			error.setAttribute("type", "cityDoesNotExist");
			error.appendChild(command);
			error.appendChild(parameters);
			results_base.appendChild(error);
		} else {
			City city = cityMap.get(cName);
			Metropole m = worldMap.get(new Point2D.Float(city.getRemotePoint().x, city.getRemotePoint().y));
			output = results.createElement("output");
			Set<Road> rList = m.pmQuadTree.removeCity(city);
	
			if (rList.size() != 0) {
				Element unmapped = results.createElement("cityUnmapped");
				unmapped.setAttribute("name", city.getName());
				unmapped.setAttribute("localX", String.valueOf(Math.round(city.getX())));
				unmapped.setAttribute("localY", String.valueOf(Math.round(city.getY())));
				unmapped.setAttribute("remoteX", String.valueOf(Math.round(city.getRemotePoint().getX())));
				unmapped.setAttribute("remoteY", String.valueOf(Math.round(city.getRemotePoint().getY())));
				unmapped.setAttribute("color", city.getColor());
				unmapped.setAttribute("radius", String.valueOf(city.getRadius()));
				output.appendChild(unmapped);
			}
			for (Road r : rList) {
				adjRoad.removeRoad(r);
				Element roadUnmapped = results.createElement("roadUnmapped");
				roadUnmapped.setAttribute("start", r.getStart().getName());
				roadUnmapped.setAttribute("end", r.getEnd().getName());
				output.appendChild(roadUnmapped);
			}
	
			cityMap.remove(cName);
			geomNameMap.remove(cName);
			m.cityM.remove(cName);
			m.cityS.remove(city);
			m.geomPtS.remove(city);
			avlTree.remove(cName);
	
			success = results.createElement("success");
	
			success.appendChild(command);
			success.appendChild(parameters);
			success.appendChild(output);
			results_base.appendChild(success);
		}
	}

	public static void clearAll(Integer id) {
		command = results.createElement("command");
		parameters = results.createElement("parameters");
	
		command.setAttribute("name", "clearAll");
	
		if (id != null) {
			command.setAttribute("id", String.valueOf(id));
		}
	
		worldTree = new PRQuadTree<Metropole>(r_width, 0, r_height, 0);
		worldMap.clear();
		avlTree.clear();
		cityMap.clear();
		geomNameMap.clear();
		airportMap.clear();
		terminalMap.clear();
		adjRoad.clear();
		metropoleMapped.clear();
	
		success = results.createElement("success");
		output = results.createElement("output");
	
		success.appendChild(command);
		success.appendChild(parameters);
		success.appendChild(output);
		results_base.appendChild(success);
	}

	public static void listCities(Integer id, String sb) throws ParserConfigurationException {
		// Prepare output format
		command = results.createElement("command");
		parameters = results.createElement("parameters");
		Element sortBy = results.createElement("sortBy");
	
		command.setAttribute("name", "listCities");
		sortBy.setAttribute("value", sb);
		if (id != null) {
			command.setAttribute("id", String.valueOf(id));
		}
	
		parameters.appendChild(sortBy);
	
		if (worldMap.size() == 0) {
			error = results.createElement("error");
			error.setAttribute("type", "noCitiesToList");
			error.appendChild(command);
			error.appendChild(parameters);
			results_base.appendChild(error);
		} else {
			output = results.createElement("output");
			Element cityList = results.createElement("cityList");
	
			output.appendChild(cityList);
	
			if (sb.equals("name")) {
				for (Map.Entry<String, Metropole> e : geomNameMap.entrySet()) {
					if (!cityMap.containsKey(e.getKey())) {
						continue;
					}
					Element city = results.createElement("city");
					city.setAttribute("name", cityMap.get(e.getKey()).getName());
					city.setAttribute("localX", String.valueOf(Math.round(cityMap.get(e.getKey()).getX())));
					city.setAttribute("localY", String.valueOf(Math.round(cityMap.get(e.getKey()).getY())));
					city.setAttribute("remoteX", String.valueOf(Math.round(e.getValue().getX())));
					city.setAttribute("remoteY", String.valueOf(Math.round(e.getValue().getY())));
					city.setAttribute("color", cityMap.get(e.getKey()).getColor());
					city.setAttribute("radius", cityMap.get(e.getKey()).getRadius().toString());
					cityList.appendChild(city);
				}
			} else if (sb.equals("coordinate")) {
				for (Map.Entry<Point2D.Float, Metropole> e : worldMap.entrySet()) {
					Iterator<City> it = e.getValue().cityS.iterator();
					while (it.hasNext()) {
						City c = it.next();
						Element city = results.createElement("city");
						city.setAttribute("name", c.getName());
						city.setAttribute("localX", String.valueOf(Math.round(c.getX())));
						city.setAttribute("localY", String.valueOf(Math.round(c.getY())));
						city.setAttribute("remoteX", String.valueOf(Math.round(e.getKey().getX())));
						city.setAttribute("remoteY", String.valueOf(Math.round(e.getKey().getY())));
						city.setAttribute("color", c.getColor());
						city.setAttribute("radius", c.getRadius().toString());
						cityList.appendChild(city);
					}
				}
			} else {
				throw new ParserConfigurationException();
			}
			success = results.createElement("success");
			success.appendChild(command);
			success.appendChild(parameters);
			success.appendChild(output);
			results_base.appendChild(success);
		}
	}

	public static void mapRoad(Integer id, String s, String e) {
		command = results.createElement("command");
		parameters = results.createElement("parameters");
		Element start = results.createElement("start");
		Element end = results.createElement("end");
	
		command.setAttribute("name", "mapRoad");
		start.setAttribute("value", s);
		end.setAttribute("value", e);
		if (id != null) {
			command.setAttribute("id", String.valueOf(id));
		}
	
		parameters.appendChild(start);
		parameters.appendChild(end);
	
		if (!cityMap.containsKey(s)) {
			error = results.createElement("error");
			error.setAttribute("type", "startPointDoesNotExist");
			error.appendChild(command);
			error.appendChild(parameters);
			results_base.appendChild(error);
		} else if (!cityMap.containsKey(e)) {
			error = results.createElement("error");
			error.setAttribute("type", "endPointDoesNotExist");
			error.appendChild(command);
			error.appendChild(parameters);
			results_base.appendChild(error);
		} else if (s.equals(e)) {
			error = results.createElement("error");
			error.setAttribute("type", "startEqualsEnd");
			error.appendChild(command);
			error.appendChild(parameters);
			results_base.appendChild(error);
		} else if (geomNameMap.get(e) != geomNameMap.get(s)) {
			error = results.createElement("error");
			error.setAttribute("type", "roadNotInOneMetropole");
			error.appendChild(command);
			error.appendChild(parameters);
			results_base.appendChild(error);
		} else {
			City startC = cityMap.get(s);
			City endC = cityMap.get(e);
			Road road = new Road(startC, endC);
			Metropole m = geomNameMap.get(e);
	
			if (m.getX() < 0 || m.getY() < 0 || m.getX() >= r_width || m.getY() >= r_height) {
				error = results.createElement("error");
				error.setAttribute("type", "roadOutOfBounds");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else if (m.pmQuadTree.ifRoadOutOfBounds(road)) {
				error = results.createElement("error");
				error.setAttribute("type", "roadOutOfBounds");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else if (m.pmQuadTree.ifRoadAlreadyMapped(road)) {
				error = results.createElement("error");
				error.setAttribute("type", "roadAlreadyMapped");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else if (m.pmQuadTree.ifRoadIntersects(road)) {
				error = results.createElement("error");
				error.setAttribute("type", "roadIntersectsAnotherRoad");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else if (!m.pmQuadTree.addRoad(road)) {
				error = results.createElement("error");
				error.setAttribute("type", "roadViolatesPMRules");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else {
				if (!metropoleMapped.contains(m)) {
					worldTree.insert(m, "Dummy");
					metropoleMapped.add(m);
				}
	
				adjRoad.addRoad(road);
	
				success = results.createElement("success");
				output = results.createElement("output");
				Element roadCreated = results.createElement("roadCreated");
	
				success.appendChild(command);
				success.appendChild(parameters);
				success.appendChild(output);
	
				roadCreated.setAttribute("start", s);
				roadCreated.setAttribute("end", e);
				output.appendChild(roadCreated);
				results_base.appendChild(success);
			}
		}
	}

	public static void mapAirport(Integer id, String n, Integer lx, Integer ly, Integer rx, Integer ry, String tName,
			Integer tx, Integer ty, String tCity) {
		success = results.createElement("success");
		command = results.createElement("command");
		parameters = results.createElement("parameters");
		name = results.createElement("name");
		localX = results.createElement("localX");
		localY = results.createElement("localY");
		remoteX = results.createElement("remoteX");
		remoteY = results.createElement("remoteY");
		Element terminalName = results.createElement("terminalName");
		Element terminalX = results.createElement("terminalX");
		Element terminalY = results.createElement("terminalY");
		Element terminalCity = results.createElement("terminalCity");

		command.setAttribute("name", "mapAirport");
		name.setAttribute("value", n);
		localX.setAttribute("value", String.valueOf(lx));
		localY.setAttribute("value", String.valueOf(ly));
		remoteX.setAttribute("value", String.valueOf(rx));
		remoteY.setAttribute("value", String.valueOf(ry));
		terminalName.setAttribute("value", tName);
		terminalX.setAttribute("value", String.valueOf(tx));
		terminalY.setAttribute("value", String.valueOf(ty));
		terminalCity.setAttribute("value", tCity);
		if (id != null) {
			command.setAttribute("id", String.valueOf(id));
		}

		parameters.appendChild(name);
		parameters.appendChild(localX);
		parameters.appendChild(localY);
		parameters.appendChild(remoteX);
		parameters.appendChild(remoteY);
		parameters.appendChild(terminalName);
		parameters.appendChild(terminalX);
		parameters.appendChild(terminalY);
		parameters.appendChild(terminalCity);

		if (geomNameMap.containsKey(n)) {
			error = results.createElement("error");
			error.setAttribute("type", "duplicateAirportName");
			error.appendChild(command);
			error.appendChild(parameters);
			results_base.appendChild(error);
		} else {
			Metropole m = worldMap.get(new Point2D.Float(rx.floatValue(), ry.floatValue()));
			Airport a = new Airport(lx.floatValue(), ly.floatValue(), rx.floatValue(), ry.floatValue(), n);
			Terminal t = new Terminal(tx.floatValue(), ty.floatValue(), rx.floatValue(), ry.floatValue(), tName);
			City c = cityMap.get(tCity);
			Road r = new Road(c, t);

			if (m != null && m.geomPtS.contains(a)) {
				error = results.createElement("error");
				error.setAttribute("type", "duplicateAirportCoordinates");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else if (rx < 0 || ry < 0 || rx >= r_width || ry >= r_height) {
				error = results.createElement("error");
				error.setAttribute("type", "airportOutOfBounds");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else if (m != null && m.pmQuadTree.ifGeomPointOutOfBounds(a)) {
				error = results.createElement("error");
				error.setAttribute("type", "airportOutOfBounds");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else if (geomNameMap.containsKey(tName)) {
				error = results.createElement("error");
				error.setAttribute("type", "duplicateTerminalName");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else if (m != null && m.geomPtS.contains(t)) {
				error = results.createElement("error");
				error.setAttribute("type", "duplicateTerminalCoordinates");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else if (m != null && m.pmQuadTree.ifGeomPointOutOfBounds(t)) {
				error = results.createElement("error");
				error.setAttribute("type", "terminalOutOfBounds");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else if (!cityMap.containsKey(tCity)) {
				error = results.createElement("error");
				error.setAttribute("type", "connectingCityDoesNotExist");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else if (geomNameMap.get(tCity) != m) {
				error = results.createElement("error");
				error.setAttribute("type", "connectingCityNotInSameMetropole");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else if (m.pmQuadTree.ifPointonExistingRoad(a)) {
				error = results.createElement("error");
				error.setAttribute("type", "airportViolatesPMRules");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else if (!m.pmQuadTree.addAirport(a)) {
				error = results.createElement("error");
				error.setAttribute("type", "airportViolatesPMRules");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else if (m == null || !m.pmQuadTree.ifGeomAlreadyMapped(c)) {
				error = results.createElement("error");
				error.setAttribute("type", "connectingCityNotMapped");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else if (m.pmQuadTree.ifRoadIntersects(r)) {
				error = results.createElement("error");
				error.setAttribute("type", "roadIntersectsAnotherRoad");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else if (!m.pmQuadTree.addRoad(r)) {
				error = results.createElement("error");
				error.setAttribute("type", "terminalViolatesPMRules");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else {
				a.addTerminal(t);
				t.setConnectedRoad(r);
				t.setConnectedCity(c);
				t.setAirport(a);

				airportMap.put(n, a);
				terminalMap.put(tName, t);
				geomNameMap.put(n, m);
				geomNameMap.put(tName, m);
				m.geomPtS.add(a);
				m.geomPtS.add(t);

				adjRoad.addRoad(r);
				Road dRoad = new Road(t, a);
				adjRoad.addRoad(dRoad);

				for (Map.Entry<String, Airport> e : airportMap.entrySet()) {
					if (e.getKey().equals(n)) {
						continue;
					}
					Metropole om = geomNameMap.get(e.getKey());
					if (om != m) {
						adjRoad.addEdge(n, e.getKey(), om.distance(m));
					} else {
						adjRoad.addEdge(n, e.getKey(), a.distance(e.getValue()));
					}
				}

				output = results.createElement("output");
				success.appendChild(command);
				success.appendChild(parameters);
				success.appendChild(output);
				results_base.appendChild(success);
			}
		}
	}

	public static void mapTerminal(Integer id, String n, Integer lx, Integer ly, Integer rx, Integer ry, String cName,
			String aName) {

		command = results.createElement("command");
		parameters = results.createElement("parameters");
		name = results.createElement("name");
		localX = results.createElement("localX");
		localY = results.createElement("localY");
		remoteX = results.createElement("remoteX");
		remoteY = results.createElement("remoteY");
		Element cityName = results.createElement("cityName");
		Element airportName = results.createElement("airportName");

		command.setAttribute("name", "mapTerminal");
		name.setAttribute("value", n);
		localX.setAttribute("value", String.valueOf(lx));
		localY.setAttribute("value", String.valueOf(ly));
		remoteX.setAttribute("value", String.valueOf(rx));
		remoteY.setAttribute("value", String.valueOf(ry));
		cityName.setAttribute("value", cName);
		airportName.setAttribute("value", aName);
		if (id != null) {
			command.setAttribute("id", String.valueOf(id));
		}

		parameters.appendChild(name);
		parameters.appendChild(localX);
		parameters.appendChild(localY);
		parameters.appendChild(remoteX);
		parameters.appendChild(remoteY);
		parameters.appendChild(cityName);
		parameters.appendChild(airportName);

		if (geomNameMap.containsKey(n)) {
			error.setAttribute("type", "duplicateTerminalName");
			error.appendChild(command);
			error.appendChild(parameters);
			results_base.appendChild(error);
		} else {
			Terminal t = new Terminal(lx.floatValue(), ly.floatValue(), rx.floatValue(), ry.floatValue(), n);
			Metropole m = worldMap.get(new Point2D.Float(rx.floatValue(), ry.floatValue()));
			if (m != null && m.geomPtS.contains(t)) {
				error = results.createElement("error");
				error.setAttribute("type", "duplicateTerminalCoordinates");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else if (rx < 0 || ry < 0 || rx >= r_width || ry >= r_height) {
				error = results.createElement("error");
				error.setAttribute("type", "terminalOutOfBounds");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else if (m != null && m.pmQuadTree.ifGeomPointOutOfBounds(t)) {
				error = results.createElement("error");
				error.setAttribute("type", "terminalOutOfBounds");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else if (!airportMap.containsKey(aName)) {
				error = results.createElement("error");
				error.setAttribute("type", "airportDoesNotExist");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else if (geomNameMap.get(aName) != m) {
				error = results.createElement("error");
				error.setAttribute("type", "airportNotInSameMetropole");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else {
				Airport a = airportMap.get(aName);
				if (!cityMap.containsKey(cName)) {
					error = results.createElement("error");
					error.setAttribute("type", "connectingCityDoesNotExist");
					error.appendChild(command);
					error.appendChild(parameters);
					results_base.appendChild(error);
				} else {
					City c = cityMap.get(cName);
					if (geomNameMap.get(cName) != m) {
						error = results.createElement("error");
						error.setAttribute("type", "connectingCityNotInSameMetropole");
						error.appendChild(command);
						error.appendChild(parameters);
						results_base.appendChild(error);
					} else if (m == null || !m.pmQuadTree.ifGeomAlreadyMapped(c)) {
						error = results.createElement("error");
						error.setAttribute("type", "connectingCityNotMapped");
						error.appendChild(command);
						error.appendChild(parameters);
						results_base.appendChild(error);
						return;
					} else {
						Road r = new Road(c, t);
						if (m.pmQuadTree.ifRoadIntersects(r)) {
							error = results.createElement("error");
							error.setAttribute("type", "roadIntersectsAnotherRoad");
							error.appendChild(command);
							error.appendChild(parameters);
							results_base.appendChild(error);
						} else if (!m.pmQuadTree.addRoad(r)) {
							error = results.createElement("error");
							error.setAttribute("type", "terminalViolatesPMRules");
							error.appendChild(command);
							error.appendChild(parameters);
							results_base.appendChild(error);
						} else {
							t.setConnectedCity(c);
							t.setAirport(a);
							t.setConnectedRoad(r);
							a.addTerminal(t);
							terminalMap.put(n, t);
							geomNameMap.put(n, m);
							m.geomPtS.add(t);

							adjRoad.addRoad(r);
							Road directRoad = new Road(t, a);
							adjRoad.addRoad(directRoad);

							success = results.createElement("success");
							output = results.createElement("output");

							success.appendChild(command);
							success.appendChild(parameters);
							success.appendChild(output);
							results_base.appendChild(success);
						}
					}
				}
			}
		}
	}

	public static void unmapRoad(Integer id, String s, String e) {
		command = results.createElement("command");
		parameters = results.createElement("parameters");
		Element start = results.createElement("start");
		Element end = results.createElement("end");
	
		command.setAttribute("name", "unmapRoad");
		command.setAttribute("id", String.valueOf(id));
		start.setAttribute("value", s);
		end.setAttribute("value", e);
	
		parameters.appendChild(start);
		parameters.appendChild(end);
	
		if (!cityMap.containsKey(s)) {
			error = results.createElement("error");
			error.setAttribute("type", "startPointDoesNotExist");
			error.appendChild(command);
			error.appendChild(parameters);
			results_base.appendChild(error);
		} else if (!cityMap.containsKey(e)) {
			error = results.createElement("error");
			error.setAttribute("type", "endPointDoesNotExist");
			error.appendChild(command);
			error.appendChild(parameters);
			results_base.appendChild(error);
		} else if (s.equals(e)) {
			error = results.createElement("error");
			error.setAttribute("type", "startEqualsEnd");
			error.appendChild(command);
			error.appendChild(parameters);
			results_base.appendChild(error);
		} else {
			City sCity = cityMap.get(s);
			City eCity = cityMap.get(e);
			if (geomNameMap.get(s) != geomNameMap.get(e)) {
				error = results.createElement("error");
				error.setAttribute("type", "roadNotMapped");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else {
				Metropole m = geomNameMap.get(s);
				Road r = new Road(sCity, eCity);
				if (!m.pmQuadTree.ifRoadAlreadyMapped(r)) {
					error = results.createElement("error");
					error.setAttribute("type", "roadNotMapped");
					error.appendChild(command);
					error.appendChild(parameters);
					results_base.appendChild(error);
				} else {
					m.pmQuadTree.removeRoad(r);
					adjRoad.removeRoad(r);
	
					success = results.createElement("success");
					output = results.createElement("output");
					Element roadDeleted = results.createElement("roadDeleted");
	
					success.appendChild(command);
					success.appendChild(parameters);
					success.appendChild(output);
	
					roadDeleted.setAttribute("start", s);
					roadDeleted.setAttribute("end", e);
					output.appendChild(roadDeleted);
					results_base.appendChild(success);
				}
			}
		}
	}

	public static void unmapAirport(Integer id, String n) {
		success = results.createElement("success");

		command = results.createElement("command");
		parameters = results.createElement("parameters");
		name = results.createElement("name");

		command.setAttribute("name", "unmapAirport");
		name.setAttribute("value", n);
		if (id != null) {
			command.setAttribute("id", String.valueOf(id));
		}

		parameters.appendChild(name);

		if (!airportMap.containsKey(n)) {
			error = results.createElement("error");
			error.setAttribute("type", "airportDoesNotExist");
			error.appendChild(command);
			error.appendChild(parameters);
			results_base.appendChild(error);
		} else {
			Airport a = airportMap.get(n);
			Metropole m = worldMap.get(new Point2D.Float(a.getRemotePoint().x, a.getRemotePoint().y));

			output = results.createElement("output");

			for (Terminal t : a.terminalL) {
				Element terminalunmapped = results.createElement("terminalUnmapped");
				terminalunmapped.setAttribute("name", t.getName());
				terminalunmapped.setAttribute("duplicateAirportName", t.airport.getName());
				terminalunmapped.setAttribute("name", t.connCity.getName());
				terminalunmapped.setAttribute("localX", String.valueOf(Math.round(t.x)));
				terminalunmapped.setAttribute("localY", String.valueOf(Math.round(t.y)));
				terminalunmapped.setAttribute("remoteX", String.valueOf(Math.round(t.getRemotePoint().x)));
				terminalunmapped.setAttribute("remoteY", String.valueOf(Math.round(t.getRemotePoint().y)));
				output.appendChild(terminalunmapped);
				terminalMap.remove(t.getName());
				geomNameMap.remove(t.getName());
				m.geomPtS.remove(t);
				m.pmQuadTree.removeRoad(t.connRoad);
				adjRoad.removeGeomPoint(t.getName());
			}
			airportMap.remove(n);
			geomNameMap.remove(n);
			m.geomPtS.remove(a);
			m.pmQuadTree.removeAirport(a);
			adjRoad.removeGeomPoint(n);

			success = results.createElement("success");

			success.appendChild(command);
			success.appendChild(parameters);
			success.appendChild(output);
			results_base.appendChild(success);
		}
	}

	public static void unmapTerminal(Integer id, String n) {

		command = results.createElement("command");
		parameters = results.createElement("parameters");
		name = results.createElement("name");

		command.setAttribute("name", "unmapTerminal");
		name.setAttribute("value", n);
		if (id != null) {
			command.setAttribute("id", String.valueOf(id));
		}

		parameters.appendChild(name);

		if (!terminalMap.containsKey(n)) {
			error = results.createElement("error");
			error.setAttribute("type", "airportDoesNotExist");
			error.appendChild(command);
			error.appendChild(parameters);
			results_base.appendChild(error);
		} else {
			Terminal t = terminalMap.get(n);
			Metropole m = worldMap.get(new Point2D.Float(t.getRemotePoint().x, t.getRemotePoint().y));
			m.pmQuadTree.removeRoad(t.connRoad);
			adjRoad.removeGeomPoint(t.getName());
			t.airport.removeTerminal(t);
			output = results.createElement("output");
			if (t.airport.terminalL.size() == 0) {
				airportMap.remove(t.airport.getName());
				geomNameMap.remove(t.airport.getName());
				m.geomPtS.remove(t.airport);
				adjRoad.removeGeomPoint(t.airport.getName());
				m.pmQuadTree.removeAirport(t.airport);
				Element airportUnmapped = results.createElement("airportUnmapped");
				airportUnmapped.setAttribute("name", t.airport.getName());
				airportUnmapped.setAttribute("localX", String.valueOf(Math.round(t.airport.x)));
				airportUnmapped.setAttribute("localY", String.valueOf(Math.round(t.airport.y)));
				airportUnmapped.setAttribute("remoteX", String.valueOf(Math.round(t.airport.getRemotePoint().x)));
				airportUnmapped.setAttribute("remoteY", String.valueOf(Math.round(t.airport.getRemotePoint().y)));
				output.appendChild(airportUnmapped);
			}
			terminalMap.remove(n);
			geomNameMap.remove(n);
			m.geomPtS.remove(t);

			success = results.createElement("success");

			success.appendChild(command);
			success.appendChild(parameters);
			success.appendChild(output);
			results_base.appendChild(success);
		}
	}

	public static void printPMQuadtree(Integer id, Integer rx, Integer ry) {
		command = results.createElement("command");
		parameters = results.createElement("parameters");
		remoteX = results.createElement("remoteX");
		remoteY = results.createElement("remoteY");
	
		command.setAttribute("name", "printPMQuadtree");
		remoteX.setAttribute("value", String.valueOf(rx));
		remoteY.setAttribute("value", String.valueOf(ry));
		if (id != null) {
			command.setAttribute("id", String.valueOf(id));
		}
	
		parameters.appendChild(remoteX);
		parameters.appendChild(remoteY);
	
		if (rx < 0 || ry < 0 || rx >= r_width || ry >= r_height) {
			error = results.createElement("error");
			error.setAttribute("type", "metropoleOutOfBounds");
			error.appendChild(command);
			error.appendChild(parameters);
			results_base.appendChild(error);
		} else {
			Metropole m = worldMap.get(new Point2D.Float(rx, ry));
			if (m == null || m.pmQuadTree.ifEmpty()) {
				error = results.createElement("error");
				error.setAttribute("type", "metropoleIsEmpty");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else {
				success = results.createElement("success");
				output = results.createElement("output");
				Element quadTreeElt = results.createElement("quadtree");
	
				quadTreeElt.setAttribute("order", String.valueOf(pmOrder));
	
				output.appendChild(quadTreeElt);
				m.pmQuadTree.printPMQuadTree(quadTreeElt, results);
				success.appendChild(command);
				success.appendChild(parameters);
				success.appendChild(output);
				results_base.appendChild(success);
			}
		}
	}

	public static void saveMap(Integer id, String n, Integer rx, Integer ry) throws IOException {
		command = results.createElement("command");
		parameters = results.createElement("parameters");
		remoteX = results.createElement("remoteX");
		remoteY = results.createElement("remoteY");
		Element name = results.createElement("name");
	
		command.setAttribute("name", "saveMap");
		remoteX.setAttribute("value", String.valueOf(rx));
		remoteY.setAttribute("value", String.valueOf(ry));
		name.setAttribute("value", n);
		if (id != null) {
			command.setAttribute("id", String.valueOf(id));
		}
	
		parameters.appendChild(remoteX);
		parameters.appendChild(remoteY);
		parameters.appendChild(name);
	
		if (rx < 0 || ry < 0 || rx >= r_width || ry >= r_height) {
			error = results.createElement("error");
			error.setAttribute("type", "metropoleOutOfBounds");
			error.appendChild(command);
			error.appendChild(parameters);
			results_base.appendChild(error);
		} else {
			Metropole m = worldMap.get(new Point2D.Float(rx, ry));
			if (m == null || m.geomPtS.size() == 0) {
				error = results.createElement("error");
				error.setAttribute("type", "metropoleIsEmpty");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else {
				success = results.createElement("success");
				output = results.createElement("output");
	
				m.pmQuadTree.drawMap(canvas);
				// canvas.draw();
				// canvas.save(name);
				// canvas.dispose();
	
				success.appendChild(command);
				success.appendChild(parameters);
				success.appendChild(output);
				results_base.appendChild(success);
			}
		}
	}

	public static void globalRangeCities(Integer id, Integer x, Integer y, Integer r) throws IOException {
		command = results.createElement("command");
		parameters = results.createElement("parameters");
		remoteX = results.createElement("remoteX");
		remoteY = results.createElement("remoteY");
		radius = results.createElement("radius");
	
		command.setAttribute("name", "globalRangeCities");
		remoteX.setAttribute("value", String.valueOf(x));
		remoteY.setAttribute("value", String.valueOf(y));
		radius.setAttribute("value", String.valueOf(r));
		if (id != null) {
			command.setAttribute("id", String.valueOf(id));
		}
	
		parameters.appendChild(remoteX);
		parameters.appendChild(remoteY);
		parameters.appendChild(radius);
	
		List<Metropole> inRangeMetropole = worldTree.rangeSearch(x, y, r);
		if (inRangeMetropole.size() == 0) {
			error = results.createElement("error");
			error.setAttribute("type", "noCitiesExistInRange");
			error.appendChild(command);
			error.appendChild(parameters);
			results_base.appendChild(error);
		} else {
			TreeSet<City> inRange = new TreeSet<City>(new GeomNameComparator());
			for (Metropole m : inRangeMetropole) {
				for (City c : m.pmQuadTree.getCitySet()) {
					inRange.add(c);
				}
			}
			if (inRange.size() == 0) {
				error = results.createElement("error");
				error.setAttribute("type", "noCitiesExistInRange");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else {
				Iterator<City> it = inRange.iterator();
				output = results.createElement("output");
				Element cityList = results.createElement("cityList");
				while (it.hasNext()) {
					City c = it.next();
					Element city = results.createElement("city");
					city.setAttribute("name", c.getName());
					city.setAttribute("localX", String.valueOf(Math.round(c.getX())));
					city.setAttribute("localY", String.valueOf(Math.round(c.getY())));
					city.setAttribute("remoteX", String.valueOf(Math.round(c.getRemotePoint().getX())));
					city.setAttribute("remoteY", String.valueOf(Math.round(c.getRemotePoint().getY())));
					city.setAttribute("radius", c.getRadius().toString());
					city.setAttribute("color", c.getColor());
					cityList.appendChild(city);
				}
	
				success = results.createElement("success");
	
				output.appendChild(cityList);
				success.appendChild(command);
				success.appendChild(parameters);
				success.appendChild(output);
				results_base.appendChild(success);
			}
		}
	}

	public static void mst(Integer id, String s) {
		command = results.createElement("command");
		parameters = results.createElement("parameters");
		Element start = results.createElement("start");

		command.setAttribute("name", "mst");
		start.setAttribute("value", s);
		if (id != null) {
			command.setAttribute("id", String.valueOf(id));
		}

		parameters.appendChild(start);

		if (!cityMap.containsKey(s)) {
			error = results.createElement("error");
			error.setAttribute("type", "cityDoesNotExist");
			error.appendChild(command);
			error.appendChild(parameters);
			results_base.appendChild(error);
		} else {
			City c = cityMap.get(s);
			Metropole m = geomNameMap.get(s);
			if (m == null || !m.pmQuadTree.ifGeomAlreadyMapped(c)) {
				error = results.createElement("error");
				error.setAttribute("type", "cityNotMapped");
				error.appendChild(command);
				error.appendChild(parameters);
				results_base.appendChild(error);
			} else {
				MST solver = new MST();
				solver.solveMST(geomNameMap, adjRoad, c);

				success = results.createElement("success");
				output = results.createElement("output");
				Element mst = results.createElement("mst");

				mst.setAttribute("distanceSpanned", String.format("%.3f", solver.getTotal()));

				output.appendChild(mst);
				solver.printXML(s, results, mst);
				success.appendChild(command);
				success.appendChild(parameters);
				success.appendChild(output);
				results_base.appendChild(success);
			}
		}
	}
}
