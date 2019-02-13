package cmsc420.meeshquest.part4;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.drawing.CanvasPlus;
import cmsc420.geom.Inclusive2DIntersectionVerifier;
import cmsc420.geom.Shape2DDistanceCalculator;

public class PMQuadTree {
	private PMWhiteNode white = new PMWhiteNode();

	private Validator validator;
	private PMNode root;

	private int sw, sh;
	private Point2D.Float origin;
	private Rectangle2D.Float region;

	private Set<GeomPoint> airportS;
	public Set<Road> roadS;
	
	public static boolean ifViolate;

	private class PriorityQueueElement {
		PMNode n;
		City c;
		double distance;

		public PriorityQueueElement(PMNode n, double d, City c) {
			this.n = n;
			this.distance = d;
			this.c = c;
		}
	}

	private class PriorityQueueComparator implements Comparator<PriorityQueueElement> {

		@Override
		public int compare(PriorityQueueElement o1, PriorityQueueElement o2) {
			if (o1.distance < o2.distance) {
				return -1;
			} else if (o1.distance > o2.distance) {
				return 1;
			} else {
				if (o1.c != null && o2.c != null) {
					return o2.c.getName().compareTo(o1.c.getName());
				}
				if (o1.c == null) {
					return -1;
				} else if (o2.c == null) {
					return 1;
				}
				return 0;
			}
		}
	}



	public PMQuadTree(final Validator validator, final int spatialWidth, final int spatialHeight,
			final int order) {
		root = white;
		this.validator = validator;
		this.sw = spatialWidth;
		this.sh = spatialHeight;
		this.origin = new Point2D.Float(0.0f, 0.0f);
		this.region = new Rectangle2D.Float(origin.x, origin.y, spatialWidth, spatialHeight);
		airportS = new HashSet<>();
		roadS = new HashSet<>();
		ifViolate = false;
	}

	public Set<City> getCitySet() {
		Set<City> ret = new HashSet<>();
		for (Road r : roadS) {
			GeomPoint start = r.getStart();
			GeomPoint end = r.getEnd();
			if (start instanceof City) {
				ret.add((City)start);
			}
			if (end instanceof City) {
				ret.add((City)end);
			}
		}
		return ret;
	}

	public boolean addAirport(final GeomPoint a) {
		ifViolate = false;
		root = root.addAirport(a, origin, sw, sh, validator);
		if (ifViolate) {
			root = root.removeAirport(a, origin, sw, sh, validator);
		} else {
			airportS.add(a);
		}
		return !ifViolate;
	}

	public boolean addRoad(final Road r) {
		ifViolate = false;
		root = root.addRoad(r, origin, sw, sh, validator);
		if (ifViolate) {
			root = root.removeRoad(r, origin, sw, sh, validator, roadS);
		} else {
			roadS.add(r);
		}
		return !ifViolate;
	}
	
	public void removeRoad(final Road r) {
		roadS.remove(r);
		root = root.removeRoad(r, origin, sw, sh, validator, roadS);
	}
	
	public void removeAirport(final Airport a) {
		airportS.remove(a);
		root = root.removeAirport(a, origin, sw, sh, validator);
	}
	
	public Set<Road> removeCity(final City c) {
		Set<Road> ret = new TreeSet<>(new RoadNameComparator());
		Set<Road> copyRoadS = new HashSet<>(roadS);
		for (Road r : copyRoadS) {
			if (r.getStart().equals(c) || r.getEnd().equals(c)) {
				removeRoad(r);
				ret.add(r);
			}
		}
		return ret;
	}
	
	public boolean ifPointonExistingRoad(GeomPoint g) {
		for (Road r : roadS) {
			if (r.ptLineDist(g) == 0) {
				return true;
			}
		}
		return false;
	}

	public boolean ifGeomPointOutOfBounds(GeomPoint g) {
		return !Inclusive2DIntersectionVerifier.intersects(g, region);
	}

	public boolean ifRoadAlreadyMapped(Road r) {
		return roadS.contains(r);
	}

	public boolean ifRoadOutOfBounds(Road r) {
		return !Inclusive2DIntersectionVerifier.intersects(r, region);
	}
	
	public boolean ifGeomAlreadyMapped(GeomPoint gp) {
		for (Road r : roadS) {
			if (gp.x == r.getStart().x && gp.y == r.getStart().y) {
				return true;
			}
			if (gp.x == r.getEnd().x && gp.y == r.getEnd().y) {
				return true;
			}
		}
		return false;
	}
	
	public boolean ifRoadIntersects(Road road) {
		for (Road r : roadS) {
			if (Inclusive2DIntersectionVerifier.intersects(r, road)) {
				if (!(r.getStart().getName().equals(road.getStart().getName()) ||
						r.getEnd().getName().equals(road.getStart().getName()) || 
						r.getStart().getName().equals(road.getEnd().getName()) || 
						r.getEnd().getName().equals(road.getEnd().getName()))) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean ifEmpty() {
		return root == white;
	}

	public City nearestCity(Integer x, Integer y, boolean ifIsolated) {
		Point2D.Float pt = new Point2D.Float(x, y);
		PriorityQueue<PriorityQueueElement> pq = new PriorityQueue<>(11,
				new PriorityQueueComparator());
		pq.add(new PriorityQueueElement(root, 0.0, null));
		double distance;
		while (!pq.isEmpty()) {
			PriorityQueueElement pqe = pq.poll();
			if (pqe.n.getNodeT() == 2) {
				return pqe.c;
			} else if (pqe.n.getNodeT() == 3) {
				PMGrayNode node = (PMGrayNode) pqe.n;
				for (int i = 0; i < 4; i++) {
					if (node.children[i].getNodeT() == 2) {
						PMBlackNode blackNode = (PMBlackNode) node.children[i];
						GeomPoint gp = blackNode.getGeomPoint();
						if (gp != null && gp instanceof City) {
							City c = (City)gp;
							distance = pt.distance(c);
							pq.add(new PriorityQueueElement(blackNode, distance, c));
						}
					} else {
						distance = Shape2DDistanceCalculator.distance(pt, node.childRegions[i]);
						pq.add(new PriorityQueueElement(node.children[i], distance, null));
					}
				}
			}
		}
		return null;
	}

	public void printPMQuadTree(Element parent, Document doc) {
		root.printNode(parent, doc);
	}

	public void drawMap(CanvasPlus canvas) {
		drawMapHelper(root, canvas);
		for (Road r : roadS) {
			canvas.addLine(r.x1, r.y1, r.x2, r.y2, Color.BLACK);
			canvas.addPoint(r.getStart().getName(), r.getStart().x, r.getStart().y, Color.BLACK);
			canvas.addPoint(r.getEnd().getName(), r.getEnd().x, r.getEnd().y, Color.BLACK);
		}
		for (GeomPoint c : airportS) {
			canvas.addPoint(c.getName(), c.x, c.y, Color.BLACK);
		}
	}
	
	public void drawRange(CanvasPlus canvas, Integer x, Integer y, Integer r) {
		canvas.addCircle(x, y, r, Color.BLUE, false);
	}

	private void drawMapHelper(PMNode n, CanvasPlus canvas) {
		if (n.getNodeT() == 1 || n.getNodeT() == 2) {
			return;
		}
		if (n.getNodeT() == 3) {
			PMGrayNode grayNode = (PMGrayNode) n;
			Rectangle2D.Float[] children = grayNode.childRegions;
			canvas.addLine(children[0].getMinX(), children[0].getMinY(), children[1].getMaxX(), children[1].getMinY(), Color.GRAY);
			canvas.addLine(children[0].getMaxX(), children[0].getMaxY(), children[2].getMaxX(), children[2].getMinY(), Color.GRAY);
			for (PMNode node : grayNode.children) {
				drawMapHelper(node, canvas);
			}
		}
	}
}
