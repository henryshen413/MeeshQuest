package cmsc420.meeshquest.part4;

import java.awt.Color;
import java.awt.geom.Arc2D;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.LinkedList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.drawing.CanvasPlus;

public class Path {
	public LinkedList<City> p;
	protected double d;

	public Path(final double d) {
		p = new LinkedList<City>();
		setDistance(d);
	}

	public void addEdge(final City city) {
		p.addFirst(city);
	}

	public int getHops() {
		return p.size() - 1;
	}

	public double getDistance() {
		return d;
	}

	public LinkedList<City> getCityList() {
		return p;
	}

	public void setDistance(double distance) {
		DecimalFormat df = new DecimalFormat("#.###");
		df.setRoundingMode(RoundingMode.HALF_UP);
		this.d = Double.valueOf(df.format(Double.valueOf(distance)));
	}
	
	public void printPath(Element parent, Document doc) {
		if (p.size() == 0) {
			return;
		}
		City s, e, t;
		s = p.getFirst();
		for (int i = 1; i < p.size(); i ++) {
			e = p.get(i);
			Element road = doc.createElement("road");
			road.setAttribute("start", s.getName());
			road.setAttribute("end", e.getName());
			parent.appendChild(road);
			if (i != p.size()-1) {
				t = p.get(i+1);
				Arc2D.Float arc = new Arc2D.Float();
				arc.setArcByTangent(s, e, t, 1);
				double a = arc.getAngleExtent();
				if (a > 45 && a < 180) {
					Element right = doc.createElement("right");
					parent.appendChild(right);
				} else if (a < -45 && a > -180) {
					Element left = doc.createElement("left");
					parent.appendChild(left);
				} else {
					Element straight = doc.createElement("straight");
					parent.appendChild(straight);
				}
			}
			s = e;
		}
	}
	
	public void drawPath(CanvasPlus canvas) {
		City s = p.getFirst();
		City e = p.getLast();
		canvas.addPoint(s.getName(), s.x, s.y, Color.GREEN);
		canvas.addPoint(e.getName(), e.x, e.y, Color.RED);
		for (int i = 0; i < p.size()-1; i ++) {
			City c = p.get(i);
			if (i != 0) {
				canvas.addPoint(c.getName(), c.x, c.y, Color.BLUE);
			}
			City next = p.get(i+1);
			canvas.addLine(c.x, c.y, next.x, next.y, Color.BLUE);
		}
	}
}