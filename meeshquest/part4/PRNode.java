package cmsc420.meeshquest.part4;

public class PRNode {
	public double xh;
	public double xl;
	public double yh;
	public double yl;
	public PRNode parent;

	public PRNode(double xh, double xl, double yh, double yl, PRNode p) {
		this.xh = xh;
		this.xl = xl;
		this.yh = yh;
		this.yl = yl;
		this.parent = p;
	}
}
