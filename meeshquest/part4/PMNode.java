package cmsc420.meeshquest.part4;

import java.awt.geom.Point2D;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class PMNode {

	public abstract int getNodeT();

	public abstract PMNode addRoad(final Road r, final Point2D.Float origin, final int w,
			final int h, Validator validator);

	public abstract PMNode addAirport(final GeomPoint a, final Point2D.Float origin, final int w,
			final int h, Validator validator);

	public abstract PMNode removeRoad(final Road r, final Point2D.Float origin, final int w,
			final int h, Validator validator, Set<Road> roadSet);
	
	public abstract PMNode removeAirport(final GeomPoint a, final Point2D.Float origin, final int w,
			final int h, Validator validator);

	public abstract void printNode(Element parent, Document doc);
	
}
