package cmsc420.meeshquest.part4;

import java.util.Comparator;

public class CityNameComparator implements Comparator<String> {

	@Override
	public int compare(String c1, String c2) {
		int i = c1.compareTo(c2);
		if (i > 0) {
			return -1;
		} else if (i < 0) {
			return 1;
		} else {
			return 0;
		}
	}

}
