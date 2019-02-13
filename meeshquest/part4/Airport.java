package cmsc420.meeshquest.part4;

import java.util.ArrayList;
import java.util.List;

public class Airport extends GeomPoint {

	public List<Terminal> terminalL;
	
	public Airport(float x, float y, float rx, float ry, String name) {
		super(name, x, y, rx, ry);
		this.terminalL = new ArrayList<>();
	}
	
	public void addTerminal(Terminal t) {
		this.terminalL.add(t);
	}
	
	public void removeTerminal(Terminal t) {
		this.terminalL.remove(t);
	}
}
