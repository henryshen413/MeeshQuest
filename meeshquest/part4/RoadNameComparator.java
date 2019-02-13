package cmsc420.meeshquest.part4;

import java.util.Comparator;

public class RoadNameComparator implements Comparator<Road> {

	@Override
	public int compare(Road r1, Road r2) {
		if (r1.getStart().getName().compareTo(r2.getStart().getName()) < 0) {
			return 1;
		} else if (r1.getStart().getName().compareTo(r2.getStart().getName()) > 0) {
			return -1;
		} else {
			return r2.getEnd().getName().compareTo(r1.getEnd().getName());
		}
	}

}
