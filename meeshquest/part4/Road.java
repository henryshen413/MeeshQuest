package cmsc420.meeshquest.part4;

import java.awt.geom.Line2D;

public class Road extends Line2D.Float {

	private GeomPoint start;
	private GeomPoint end;
	private double d;
	private GeomNameComparator gnc = new GeomNameComparator();
	
	public Road(GeomPoint start, GeomPoint end) {
		
		if (gnc.compare(start, end) > 0) {
			this.start = start;
			this.end = end;
		} else {
			this.end = start;
			this.start = end;
		}
		this.x1 = this.start.x;
		this.x2 = this.end.x;
		this.y1 = this.start.y;
		this.y2 = this.end.y;
		this.d = start.distance(end);
	}
	
	public GeomPoint getStart() {
		return start;
	}
	
	public GeomPoint getEnd() {
		return end;
	}
	
	public double getDistance() {
		return d;
	}
	
	public GeomPoint getOther(String pointName) {
		if (start.getName().equals(pointName)) {
			return end;
		} else if (end.getName().equals(pointName)) {
			return start;
		} else {
			return null;
		}
	}
	
	public String getPointNameString() {
		return start.getName() + "-" + end.getName();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && (obj.getClass().equals(this.getClass()))) {
			Road r = (Road) obj;
			return (start.equals(r.start) && end.equals(r.end) && d == r.d);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		final long dBits = java.lang.Double.doubleToLongBits(d);
		int hash = 31;
		hash = 37 * hash + start.hashCode();
		hash = 37 * hash + end.hashCode();
		return 37 * hash + (int) (dBits ^ (dBits >>> 32));
	}
}
