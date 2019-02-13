package cmsc420.meeshquest.part4;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cmsc420.drawing.CanvasPlus;
import cmsc420.sortedmap.AvlGTree;
import cmsc420.xml.XmlUtility;

public class MeeshQuest {

	public static Document results;
	public static CanvasPlus canvas;
	public static Element results_base, parameters, command, name, localX, localY, remoteX, remoteY, radius, color,
			success, error, output;
	public static AvlGTree<String, City> avlTree;
	public static Integer width, height, r_height, r_width, g, pmOrder;
	public static PRQuadTree<Metropole> worldTree;
	public static RoadAdjacencyList adjRoad = new RoadAdjacencyList();
	public static Map<String, City> cityMap = new HashMap<>();
	public static TreeMap<Point2D.Float, Metropole> worldMap = new TreeMap<Point2D.Float, Metropole>(
			new PointComparator());
	public static Map<String, Airport> airportMap = new HashMap<>();
	public static Map<String, Terminal> terminalMap = new HashMap<>();
	public static Map<String, Metropole> geomNameMap = new TreeMap<>(new CityNameComparator());
	public static Set<Point2D.Float> metropoleMapped = new HashSet<>();

	public static void main(String[] args) throws ParserConfigurationException, TransformerException {

		results = XmlUtility.getDocumentBuilder().newDocument();
		results_base = results.createElement("results");
		results.appendChild(results_base);

		try {
			Document doc = XmlUtility.validateNoNamespace(System.in);
			Element commandNode = doc.getDocumentElement();

			width = Integer.parseInt(commandNode.getAttribute("localSpatialWidth"));
			height = Integer.parseInt(commandNode.getAttribute("localSpatialHeight"));
			r_width = Integer.parseInt(commandNode.getAttribute("remoteSpatialWidth"));
			r_height = Integer.parseInt(commandNode.getAttribute("remoteSpatialHeight"));
			g = Integer.parseInt(commandNode.getAttribute("g"));
			pmOrder = Integer.parseInt(commandNode.getAttribute("pmOrder"));
			avlTree = new AvlGTree<>(new CityNameComparator(), g);
			worldTree = new PRQuadTree<Metropole>(r_width, 0, r_height, 0);
			canvas = new CanvasPlus("MeeshQuest", width, height);
			canvas.addRectangle(0, 0, width, height, Color.BLACK, false);
			
			NodeList nl = commandNode.getChildNodes();

			for (int i = 0; i < nl.getLength(); i++) {
				if (nl.item(i).getNodeType() == Document.ELEMENT_NODE) {
					commandNode = (Element) nl.item(i);
					if (commandNode.getNodeName().equals("createCity")) {
						Integer id = null;
						
						if (!commandNode.getAttribute("id").equals("")) {
							id = Integer.parseInt(commandNode.getAttribute("id"));
						}
						
						Integer lx = Integer.parseInt(commandNode.getAttribute("localX"));
						Integer ly = Integer.parseInt(commandNode.getAttribute("localY"));
						Integer rx = Integer.parseInt(commandNode.getAttribute("remoteX"));
						Integer ry = Integer.parseInt(commandNode.getAttribute("remoteY"));
						Integer r = Integer.parseInt(commandNode.getAttribute("radius"));
						Command.createCity(id, commandNode.getAttribute("name"), lx, ly, rx, ry, r, commandNode.getAttribute("color"));
						
					} else if (commandNode.getNodeName().equals("listCities")) {
						Integer id = null;
						
						if (!commandNode.getAttribute("id").equals("")) {
							id = Integer.parseInt(commandNode.getAttribute("id"));
						}
						
						Command.listCities(id, commandNode.getAttribute("sortBy"));

					} else if (commandNode.getNodeName().equals("clearAll")) {
						Integer id = null;
						
						if (!commandNode.getAttribute("id").equals("")) {
							id = Integer.parseInt(commandNode.getAttribute("id"));
						}
						
						Command.clearAll(id);

					} else if (commandNode.getNodeName().equals("printAvlTree")) {
						Integer id = null;
						
						if (!commandNode.getAttribute("id").equals("")) {
							id = Integer.parseInt(commandNode.getAttribute("id"));
						}
						
						Command.printAvlTree(id);

					} else if (commandNode.getNodeName().equals("deleteCity")) {
						Integer id = null;
						
						if (!commandNode.getAttribute("id").equals("")) {
							id = Integer.parseInt(commandNode.getAttribute("id"));
						}
						
						Command.deleteCity(id, commandNode.getAttribute("name"));

					} else if (commandNode.getNodeName().equals("mapRoad")) {
						Integer id = null;
						
						if (!commandNode.getAttribute("id").equals("")) {
							id = Integer.parseInt(commandNode.getAttribute("id"));
						}
						
						Command.mapRoad(id, commandNode.getAttribute("start"), commandNode.getAttribute("end"));

					} else if (commandNode.getNodeName().equals("unmapRoad")) {
						Integer id = null;
						
						if (!commandNode.getAttribute("id").equals("")) {
							id = Integer.parseInt(commandNode.getAttribute("id"));
						}
						
						Command.unmapRoad(id, commandNode.getAttribute("start"), commandNode.getAttribute("end"));

					} else if (commandNode.getNodeName().equals("printPMQuadtree")) {
						Integer id = null;
						
						if (!commandNode.getAttribute("id").equals("")) {
							id = Integer.parseInt(commandNode.getAttribute("id"));
						}
						
						Command.printPMQuadtree(id, Integer.parseInt(commandNode.getAttribute("remoteX")), 
								Integer.parseInt(commandNode.getAttribute("remoteY")));

					} else if (commandNode.getNodeName().equals("saveMap")) {
						Integer id = null;
						
						if (!commandNode.getAttribute("id").equals("")) {
							id = Integer.parseInt(commandNode.getAttribute("id"));
						}
						
						Command.saveMap(id, commandNode.getAttribute("name"), Integer.parseInt(commandNode.getAttribute("remoteX")), 
								Integer.parseInt(commandNode.getAttribute("remoteY")));

					} else if (commandNode.getNodeName().equals("globalRangeCities")) {		
						Integer id = null;
						
						if (!commandNode.getAttribute("id").equals("")) {
							id = Integer.parseInt(commandNode.getAttribute("id"));
						}
						
						Command.globalRangeCities(id, Integer.parseInt(commandNode.getAttribute("remoteX")), 
								Integer.parseInt(commandNode.getAttribute("remoteY")), Integer.parseInt(commandNode.getAttribute("radius")));

					} else if (commandNode.getNodeName().equals("nearestCity")) {
						Integer id = null;
						
						if (!commandNode.getAttribute("id").equals("")) {
							id = Integer.parseInt(commandNode.getAttribute("id"));
						}
						
						Command.nearestCity(id, Integer.parseInt(commandNode.getAttribute("localX")), 
								Integer.parseInt(commandNode.getAttribute("localY")), Integer.parseInt(commandNode.getAttribute("remoteX")), 
								Integer.parseInt(commandNode.getAttribute("remoteY")));

					} else if (commandNode.getNodeName().equals("mapAirport")) {
						Integer id = null;
						
						if (!commandNode.getAttribute("id").equals("")) {
							id = Integer.parseInt(commandNode.getAttribute("id"));
						}
						
						Command.mapAirport(id, commandNode.getAttribute("name"), Integer.parseInt(commandNode.getAttribute("localX")), 
								Integer.parseInt(commandNode.getAttribute("localY")), Integer.parseInt(commandNode.getAttribute("remoteX")), 
								Integer.parseInt(commandNode.getAttribute("remoteY")), commandNode.getAttribute("terminalName"), 
								Integer.parseInt(commandNode.getAttribute("terminalX")), Integer.parseInt(commandNode.getAttribute("terminalY")), 
								commandNode.getAttribute("terminalCity"));
						
					} else if (commandNode.getNodeName().equals("mapTerminal")) {
						Integer id = null;
						
						if (!commandNode.getAttribute("id").equals("")) {
							id = Integer.parseInt(commandNode.getAttribute("id"));
						}
						
						Command.mapTerminal(id, commandNode.getAttribute("name"), Integer.parseInt(commandNode.getAttribute("localX")), 
								Integer.parseInt(commandNode.getAttribute("localY")), Integer.parseInt(commandNode.getAttribute("remoteX")), 
								Integer.parseInt(commandNode.getAttribute("remoteY")), commandNode.getAttribute("cityName"), 
								commandNode.getAttribute("airportName"));
						
					} else if (commandNode.getNodeName().equals("unmapAirport")) {
						Integer id = null;
						
						if (!commandNode.getAttribute("id").equals("")) {
							id = Integer.parseInt(commandNode.getAttribute("id"));
						}
						
						Command.unmapAirport(id, commandNode.getAttribute("name"));
						
					} else if (commandNode.getNodeName().equals("unmapTerminal")) {
						Integer id = null;
						
						if (!commandNode.getAttribute("id").equals("")) {
							id = Integer.parseInt(commandNode.getAttribute("id"));
						}
						
						Command.unmapTerminal(id, commandNode.getAttribute("name"));
						
					} else if (commandNode.getNodeName().equals("mst")) {
						Integer id = null;
						
						if (!commandNode.getAttribute("id").equals("")) {
							id = Integer.parseInt(commandNode.getAttribute("id"));
						}
						
						Command.mst(id, commandNode.getAttribute("start"));
						
					}
				}
			}
		} catch (ParserConfigurationException | IOException | SAXException e) {
			results = XmlUtility.getDocumentBuilder().newDocument();
			Element fatalError = results.createElement("fatalError");
			results.appendChild(fatalError);
		} finally {
			try {
				XmlUtility.print(results);
			}

			catch (TransformerException e) {
				e.printStackTrace();
			}
		}

	}

}
