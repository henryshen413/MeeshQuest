package cmsc420.meeshquest.part4;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.geom.Inclusive2DIntersectionVerifier;

public class PMGrayNode extends PMNode {

	public PMNode[] children;
	public Rectangle2D.Float[] childRegions;
	public Point2D.Float origin;
	public Point2D.Float[] childOrigin;
	public int hw;
	public int hh;

	public PMGrayNode(final Point2D.Float origin, final int w, final int h) {
		this.origin = origin;

		children = new PMNode[4];
		children[0] = new PMWhiteNode();;
		children[1] = new PMWhiteNode();;
		children[2] = new PMWhiteNode();;
		children[3] = new PMWhiteNode();;

		hw = w >> 1;
		hh = h >> 1;

		childOrigin = new Point2D.Float[4];
		childOrigin[0] = new Point2D.Float(origin.x, origin.y + hh);
		childOrigin[1] = new Point2D.Float(origin.x + hw, origin.y + hh);
		childOrigin[2] = new Point2D.Float(origin.x, origin.y);
		childOrigin[3] = new Point2D.Float(origin.x + hw, origin.y);

		childRegions = new Rectangle2D.Float[4];
		for (int i = 0; i < 4; i++) {
			childRegions[i] = new Rectangle2D.Float(childOrigin[i].x, childOrigin[i].y,
					hw, hh);
		}
	}
	
	public int getNodeT() {
		return 3;
	}

	public Rectangle2D.Float[] getChildRegions() {
		return childRegions;
	}

	@Override
	public PMNode addRoad(final Road r, final Point2D.Float origin, final int w,
			final int h, Validator validator) {
		for (int i = 0; i < 4; i++) {
			if (Inclusive2DIntersectionVerifier.intersects(r, childRegions[i])) {
				children[i] = children[i].addRoad(r, childOrigin[i], hw, hh, validator);
			}
		}
		return this;
	}

	@Override
	public PMNode addAirport(final GeomPoint a, final Point2D.Float origin, final int w,
			final int h, Validator validator) {
		for (int i = 0; i < 4; i++) {
			if (Inclusive2DIntersectionVerifier.intersects(a, childRegions[i])) {
				children[i] = children[i].addAirport(a, childOrigin[i], hw, hh, validator);
			}
		}
		return this;
	}
	
	private PMBlackNode addAllGeometry(PMBlackNode b, PMNode n) {
		if (n.getNodeT() == 2) {
			PMBlackNode bNode = (PMBlackNode) n;
			for (Road r : bNode.roadL) {
				if (!b.roadL.contains(r)) {
					b.roadL.add(r);
				}
			}
			for (GeomPoint gp : bNode.geomL) {
				if (!b.geomL.contains(gp)) {
					b.geomL.add(gp);
				}
			}
			for (GeomPoint gp : bNode.airportL) {
				if (!b.airportL.contains(gp)) {
					b.airportL.add(gp);
				}
			}
		} else if (n.getNodeT() == 3) {
			PMGrayNode gNode = (PMGrayNode)n;
			for (PMNode node : gNode.children) {
				b = addAllGeometry(b, node);
			}
		}
		return b;
	}

	@Override
	public PMNode removeRoad(final Road r, final Point2D.Float origin, final int w,
			final int h, Validator validator, Set<Road> roadS) {
		int wc = 0, gc = 0, bc = 0;
		for (int i = 0; i < 4; i ++) {
			if (Inclusive2DIntersectionVerifier.intersects(r, childRegions[i])) {
				children[i] = children[i].removeRoad(r, childOrigin[i], hw, hh, validator, roadS);
			}
			if (children[i].getNodeT() == 1) {
				wc ++;
			} else if (children[i].getNodeT() == 2) {
				bc ++;
			} else {
				gc ++;
			}
		}
		if (wc == 4) {
			return new PMWhiteNode();
		}
		if (wc == 3 && bc == 1) {
			for (int i = 0; i < 4; i ++) {
				if (children[i].getNodeT() == 2) {
					return children[i];
				}
			}
		}
		if (gc != 4) {
			PMBlackNode b = new PMBlackNode();
			b = addAllGeometry(b, this);
			if (validator.ifValid(b)) {
				return b;
			}
		}
		return this;
	}
	
	@Override
	public PMNode removeAirport(final GeomPoint a, final Point2D.Float origin, final int w,
			final int h, Validator validator) {
		int wc = 0, gc = 0, bc = 0;
		for (int i = 0; i < 4; i ++) {
			if (Inclusive2DIntersectionVerifier.intersects(a, childRegions[i])) {
				children[i] = children[i].removeAirport(a, childOrigin[i], hw, hh, validator);
			}
			if (children[i].getNodeT() == 1) {
				wc ++;
			} else if (children[i].getNodeT() == 2) {
				bc ++;
			} else {
				gc ++;
			}
		}
		if (wc == 4) {
			return new PMWhiteNode();
		}
		if (wc == 3 && bc == 1) {
			for (int i = 0; i < 4; i ++) {
				if (children[i].getNodeT() == 2) {
					return children[i];
				}
			}
		}
		if (gc != 4) {
			PMBlackNode b = new PMBlackNode();
			b = addAllGeometry(b, this);
			if (validator.ifValid(b)) {
				return b;
			}
		}
		return this;
	}
	
	@Override
	public void printNode(Element parent, Document doc) {
		Element gray = doc.createElement("gray");
		gray.setAttribute("x", String.valueOf(Math.round(origin.getX()+hw)));
		gray.setAttribute("y", String.valueOf(Math.round(origin.getY()+hh)));
		parent.appendChild(gray);
		for (PMNode n : children) {
			n.printNode(gray, doc);
		}
	}
}
