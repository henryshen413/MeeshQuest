package cmsc420.meeshquest.part4;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.geom.Inclusive2DIntersectionVerifier;

public class PMBlackNode extends PMNode {

	public Set<Road> roadL;
	public List<GeomPoint> geomL;
	public List<GeomPoint> airportL;


	public PMBlackNode() {
		roadL = new TreeSet<Road>(new RoadNameComparator());
		geomL = new ArrayList<>();
		airportL = new ArrayList<>();
	}
	
	public int getNodeT() {
		return 2;
	}

	public Set<Road> getRoadList() {
		return roadL;
	}

	public List<GeomPoint> getAirportList() {
		return airportL;
	}
	
	public List<GeomPoint> getGeomList() {
		return geomL;
	}
	
	public GeomPoint getGeomPoint() {
		if (geomL.size() == 0) {
			return null;
		}
		return geomL.get(0);
	}

	public int getCardinality() {
		return roadL.size() + geomL.size() + airportL.size();
	}

	@Override
	public PMNode addRoad(final Road r, final Point2D.Float origin, final int w,
			final int h, Validator validator) {
		final Rectangle2D.Float rect = new Rectangle2D.Float(origin.x, origin.y, w, h);
		if (Inclusive2DIntersectionVerifier.intersects(r.getStart(), rect)) {
			if (!geomL.contains(r.getStart())) {
				geomL.add(r.getStart());
			}
		}
		if (Inclusive2DIntersectionVerifier.intersects(r.getEnd(), rect)) {
			if (!geomL.contains(r.getEnd())) {
				geomL.add(r.getEnd());
			}
		}
		roadL.add(r);
		if (validator.ifValid(this)) {
			return this;
		} else if (w == 1 && h == 1) {
			PMQuadTree.ifViolate = true;
			return this;
		} else {
			return partition(origin, w, h, validator);
		}
	}

	@Override
	public PMNode addAirport(final GeomPoint a, final Point2D.Float origin, final int w,
			final int h, Validator validator) {
		airportL.add(a);
		if (validator.ifValid(this)) {
			return this;
		} else if (w == 1 && h == 1) {
			PMQuadTree.ifViolate = true;
			return this;
		} else {
			return partition(origin, w, h, validator);
		}
	}
	
	@Override
	public PMNode removeRoad(final Road r, final Point2D.Float origin, final int w,
			final int h, Validator validator, Set<Road> roadSet) {
		boolean ifMappedS = false;
		for (Road road : roadSet) {
			if (r.getStart().x == road.getStart().x && r.getStart().y == road.getStart().y) {
				ifMappedS = true;
			}
			if (r.getStart().x == road.getEnd().x && r.getStart().y == road.getEnd().y) {
				ifMappedS = true;
			}
		}
		
		boolean ifMappedE = false;
		for (Road road : roadSet) {
			if (r.getEnd().x == road.getStart().x && r.getEnd().y == road.getStart().y) {
				ifMappedE = true;
			}
			if (r.getEnd().x == road.getEnd().x && r.getEnd().y == road.getEnd().y) {
				ifMappedE = true;
			}
		}
		
		roadL.remove(r);
		if (geomL.contains(r.getStart()) && !ifMappedS) {
			geomL.remove(r.getStart());
		}
		if (geomL.contains(r.getEnd()) && !ifMappedE) {
			geomL.remove(r.getEnd());
		}
		if (getCardinality() == 0) {
			return new PMWhiteNode();
		}
		return this;
	}
	
	@Override
	public PMNode removeAirport(final GeomPoint a, final Point2D.Float origin, final int w,
			final int h, Validator validator) {
		airportL.remove(a);
		if (getCardinality() == 0) {
			return new PMWhiteNode();
		}
		return this;
	}

	private PMNode partition(final Point2D.Float origin, final int w, final int h, Validator validator) {
		PMNode gray = new PMGrayNode(origin, w, h);
		for (Road r : roadL) {
			gray.addRoad(r, origin, w, h, validator);
		}
		for (GeomPoint c : airportL) {
			gray.addAirport(c, origin, w, h, validator);
		}
		return gray;
	}

	@Override
	public void printNode(Element parent, Document doc) {
		Element black = doc.createElement("black");
		black.setAttribute("cardinality", String.valueOf(getCardinality()));
		parent.appendChild(black);
		if (geomL.size() != 0) {
			GeomPoint gp = geomL.get(0);
			if (gp instanceof City) {
				City c = (City)gp;
				Element city = doc.createElement("city");
				city.setAttribute("name", c.getName());
				city.setAttribute("localX", String.valueOf(Math.round(c.getX())));
				city.setAttribute("localY", String.valueOf(Math.round(c.getY())));
				city.setAttribute("remoteX", String.valueOf(Math.round(c.getRemotePoint().getX())));
				city.setAttribute("remoteY", String.valueOf(Math.round(c.getRemotePoint().getY())));
				city.setAttribute("color", c.getColor());
				city.setAttribute("radius", String.valueOf(c.getRadius()));
				black.appendChild(city);
			} else if (gp instanceof Terminal) {
				Terminal t = (Terminal)gp;
				Element terminal = doc.createElement("terminal");
				terminal.setAttribute("airportName", t.airport.getName());
				terminal.setAttribute("localX", String.valueOf(Math.round(t.getX())));
				terminal.setAttribute("localY", String.valueOf(Math.round(t.getY())));
				terminal.setAttribute("remoteX", String.valueOf(Math.round(t.getRemotePoint().getX())));
				terminal.setAttribute("remoteY", String.valueOf(Math.round(t.getRemotePoint().getY())));
				terminal.setAttribute("cityName", t.connCity.getName());
				terminal.setAttribute("name", t.getName());
				black.appendChild(terminal);
			}
		}
		if (airportL.size() != 0) {
			Airport a = (Airport)airportL.get(0);
			Element airport = doc.createElement("airport");
			airport.setAttribute("name", a.getName());
			airport.setAttribute("localX", String.valueOf(Math.round(a.getX())));
			airport.setAttribute("localY", String.valueOf(Math.round(a.getY())));
			airport.setAttribute("remoteX", String.valueOf(Math.round(a.getRemotePoint().getX())));
			airport.setAttribute("remoteY", String.valueOf(Math.round(a.getRemotePoint().getY())));
			black.appendChild(airport);
		}
		for (Road r : roadL) {
			Element road = doc.createElement("road");
			road.setAttribute("start", r.getStart().getName());
			road.setAttribute("end", r.getEnd().getName());
			black.appendChild(road);
		}
	}

}
