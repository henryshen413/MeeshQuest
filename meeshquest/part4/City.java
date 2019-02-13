package cmsc420.meeshquest.part4;

public class City extends GeomPoint {

	private final Integer r;
	private final String c;

	public City(String name, float x, float y, float rx, float ry, Integer r, String c) {
		super(name, x, y, rx, ry);
		this.r = r;
		this.c = c;
	}

	public Integer getRadius() {
		return r;
	}

	public String getColor() {
		return c;
	}
	
	@Override
	public String toString() {
		return String.format("(%d,%d)", Math.round(this.x), Math.round(this.y));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && (obj.getClass().equals(this.getClass()))) {
			City c = (City) obj;
			return (n.equals(c.getName()) && x == c.x && y == c.y);
		}
		return false;
	}
}
