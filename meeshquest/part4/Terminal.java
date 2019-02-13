package cmsc420.meeshquest.part4;

public class Terminal extends GeomPoint {
	public City connCity;
	public Road connRoad;
	public Airport airport;
	
	public Terminal(float x, float y, float rx, float ry, String n) {
		super(n, x, y, rx, ry);
		this.connCity = null;
		this.airport = null;
		this.connRoad = null;
	}
	
	public void setConnectedCity(City c) {
		this.connCity = c;
	}
	
	public void setConnectedRoad(Road r) {
		this.connRoad = r;
	}
	
	public void setAirport(Airport a) {
		this.airport = a;
	}
}
