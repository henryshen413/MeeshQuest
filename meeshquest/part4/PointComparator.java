package cmsc420.meeshquest.part4;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.util.Comparator;

public class PointComparator implements Comparator<Point2D.Float> {

	@Override
	public int compare(Float p1, Float p2) {
		double x1 = p1.getX();
		double x2 = p2.getX();
		double y1 = p1.getY();
		double y2 = p2.getY();
		if (y1 != y2) {
			if (y1 < y2) return -1;
			else return 1;
		} else {
			if (x1 < x2) {
				return -1;
			} else if (x1 == x2) {
				return 0;
			} else {
				return 1;
			}
		}
	}

}
