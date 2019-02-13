package cmsc420.meeshquest.part4;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MST {
	private PriorityQueue<PriorityQueueEle> pq;
	private Map<String, TreeSet<String>> ret;
	private double total;
	private Set<String> prt;
	private Set<String> fd;
	
	public MST() {
		pq = new PriorityQueue<>(11, new PriorityQueueComparator());
		ret = new TreeMap<>(new CityNameComparator());
		total = 0;
		prt = new TreeSet<>(new CityNameComparator());
		fd = new TreeSet<>(new CityNameComparator());
	}
	
	public void solveMST(Map<String, Metropole> geomNameMap, RoadAdjacencyList roadAj, City start) {
		for (Map.Entry<String, Metropole> e : geomNameMap.entrySet()) {
			if (e.getKey().equals(start.getName())) {
				pq.add(new PriorityQueueEle(e.getKey(), 0, null));
			} else {
				pq.add(new PriorityQueueEle(e.getKey(), Double.MAX_VALUE, null));
			}
		}
		while (!pq.isEmpty()) {
			PriorityQueueEle pqe = pq.poll();
			if (!fd.contains(pqe.n)) {
				if (!(pqe.d == Double.MAX_VALUE)) {
					total += pqe.d;
					fd.add(pqe.n);
					Map<String, Double> adjMap = roadAj.getRoadSet(pqe.n);
					for (Map.Entry<String, Double> e : adjMap.entrySet()) {
						if (!fd.contains(e.getKey())) {
							pq.add(new PriorityQueueEle(e.getKey(), e.getValue(), pqe.n));
						}
					}
					if (pqe.f != null) {
						if (!ret.containsKey(pqe.f)) {
							ret.put(pqe.f, new TreeSet<String>(new CityNameComparator()));
						}
						Set<String> adj = ret.get(pqe.f);
						adj.add(pqe.n);
					}
				}
			}
		}
	}
	
	public double getTotal() {
		return total;
	}

	public void printXML(String start, Document doc, Element parent) {
		if (prt.contains(start)) {
			return;
		}
		prt.add(start);
		Element node = doc.createElement("node");
		node.setAttribute("name", start);
		parent.appendChild(node);
		if (!ret.containsKey(start)) {
			return;
		}
		for (String s : ret.get(start)) {
			printXML(s, doc, node);
		}
	}
}
