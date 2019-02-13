package cmsc420.meeshquest.part4;

public class PRQuadInternal extends PRNode {
	PRNode NE, NW, SE, SW;
	int numNd;

	public PRQuadInternal(double xh, double xl, double yh, double yl, PRNode p) {
		super(xh, xl, yh, yl, p);
		this.NE = null;
		this.NW = null;
		this.SE = null;
		this.SW = null;
		this.numNd = 0;
	}
}
