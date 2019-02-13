package cmsc420.meeshquest.part4;

import java.awt.geom.Point2D;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Metropole extends Point2D.Float {
	public Map<String, City> cityM;
	public Set<City> cityS;
	public Set<GeomPoint> geomPtS;
	public PMQuadTree pmQuadTree;
	
	public Metropole(float x, float y, int localSpatialWidth, int localSpacialHeight, int pmOrder) {
		cityM = new TreeMap<>(new CityNameComparator());
		cityS = new TreeSet<>(new PointComparator());
		this.x = x;
		this.y = y;
		Validator v;
		
		if (pmOrder != 1) {
			v = new PM3Validator();
		} else {
			v = new PM1Validator();
		}
		
		pmQuadTree = new PMQuadTree(v, localSpatialWidth, localSpacialHeight, pmOrder);
		geomPtS = new TreeSet<>(new PointComparator());
	}
}
