package cmsc420.meeshquest.part4;

import java.awt.geom.Point2D;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PMWhiteNode extends PMNode {

	public int getNodeT() {
		return 1;
	}

	@Override
	public PMNode addRoad(final Road r, final Point2D.Float origin, final int w,
			final int h, Validator validator) {
		final PMBlackNode blackNode = new PMBlackNode();
		return blackNode.addRoad(r, origin, w, h, validator);
	}

	@Override
	public PMNode addAirport(final GeomPoint a, final Point2D.Float origin, final int w,
			final int h, Validator validator) {
		final PMBlackNode blackNode = new PMBlackNode();
		return blackNode.addAirport(a, origin, w, h, validator);
	}

	@Override
	public PMNode removeRoad(final Road r, final Point2D.Float origin, final int w,
			final int h, Validator validator, Set<Road> roadSet) {
		return this;
	}
	
	@Override
	public PMNode removeAirport(final GeomPoint a, final Point2D.Float origin, final int w,
			final int h, Validator validator) {
		return this;
	}

	@Override
	public void printNode(Element parent, Document doc) {
		Element white = doc.createElement("white");
		parent.appendChild(white);
	}
}
