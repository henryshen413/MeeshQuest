package cmsc420.meeshquest.part4;

import java.awt.geom.Point2D;

public class PRQuadLeaf<T extends Point2D.Float> extends PRNode {
	T e;
	String name;

	public PRQuadLeaf(T ele, double xh, double xl, double yh, double yl, PRNode p, String n) {
		super(xh, xl, yh, yl, p);
		this.e = ele;
		this.name = n;
	}
}
