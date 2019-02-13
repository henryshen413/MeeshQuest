package cmsc420.meeshquest.part4;

import java.util.Comparator;

public class PriorityQueueComparator implements Comparator<PriorityQueueEle> {

	@Override
	public int compare(PriorityQueueEle pqe1, PriorityQueueEle pqe2) {
		if (pqe1.d < pqe2.d) {
			return -1;
		} else if (pqe1.d > pqe2.d) {
			return 1;
		} else {
			if (pqe1.n.equals(pqe2.n)) {
				return pqe2.f.compareTo(pqe1.f);
			} else {
				return pqe2.n.compareTo(pqe1.n);
			}
		}
	}
}
