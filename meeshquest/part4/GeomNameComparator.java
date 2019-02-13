package cmsc420.meeshquest.part4;

import java.util.Comparator;

public class GeomNameComparator implements Comparator<GeomPoint> {

	@Override
	public int compare(GeomPoint g1, GeomPoint g2) {
		return g2.getName().compareTo(g1.getName());
	}

}
