package cmsc420.meeshquest.part4;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.drawing.CanvasPlus;

public class PRQuadTree<T extends Point2D.Float> {

	private class PriorityQueueElement {
		public double d;
		public PRNode nd;
		public String n;

		public PriorityQueueElement(double d, PRNode node, String n) {
			this.d = d;
			this.nd = node;
			this.n = n;
		}
	}

	private class PriorityQueueComparator implements Comparator<PriorityQueueElement> {

		@Override
		public int compare(PRQuadTree<T>.PriorityQueueElement pqe1,
				PRQuadTree<T>.PriorityQueueElement pqe2) {
			if (pqe1.d < pqe2.d) {
				return -1;
			} else if (pqe1.d > pqe2.d) {
				return 1;
			} else {
				if (pqe1.n == null && pqe2.n == null) {
					return 0;
				} else if (pqe1.n == null) {
					return -1;
				} else if (pqe2.n == null) {
					return 1;
				}
				return pqe2.n.compareTo(pqe1.n);
			}
		}

	}

	private PRNode root;
	private int mx, my, mX, mY;
	private Map<String, PRNode> nodeMap;

	public PRQuadTree(int mX, int mx, int mY, int my) {
		this.mX = mX;
		this.mx = mx;
		this.mY = mY;
		this.my = my;
		root = null;
		nodeMap = new HashMap<>();
	}

	public boolean insert(T e, String n) {
		PRNode ret = insertH(root, e, mX, mx, mY, my, null, 0, n);
		if (ret == null) {
			return false;
		}
		return true;
	}

	private PRNode splitAndInsert(PRQuadInternal nd, T ele, String n) {
		PRNode ret = null;
		double xCenter = (nd.xh + nd.xl) / 2;
		double yCenter = (nd.yh + nd.yl) / 2;
		int direction = getDirection(ele, nd.xh, nd.xl, nd.yh, nd.yl);
		switch (direction) {
		case 1:
			ret = insertH(nd.NW, ele, xCenter, nd.xl, nd.yh, yCenter, nd, 1, n);
			break;
		case 2:
			ret = insertH(nd.NE, ele, nd.xh, xCenter, nd.yh, yCenter, nd, 2, n);
			break;
		case 3:
			ret = insertH(nd.SW, ele, xCenter, nd.xl, yCenter, nd.yl, nd, 3, n);
			break;
		case 4:
			ret = insertH(nd.SE, ele, nd.xh, xCenter, yCenter, nd.yl, nd, 4, n);
			break;
		}
		return ret;
	}

	public boolean remove(String cn) {
		removeH(findElement(cn));
		nodeMap.remove(cn);
		return true;
	}

	public void drawMap(CanvasPlus canvas) {
		drawTreeH(root, canvas);
	}

	public void drawCircle(CanvasPlus canvas, Integer x, Integer y, Integer r) {
		canvas.addCircle(x, y, r, Color.BLUE, false);
	}

	public List<T> rangeSearch(Integer x, Integer y, Integer r) {
		List<T> result = new ArrayList<>();
		rangeSearchH(root, x, y, r, result);
		return result;
	}

	public T nearestPoint(Integer x, Integer y) {
		PriorityQueue<PriorityQueueElement> pq = new PriorityQueue<>(11,
				new PriorityQueueComparator());
		pq.add(new PriorityQueueElement(0, root, null));
		while (!pq.isEmpty()) {
			PriorityQueueElement pqe = pq.poll();
			if (pqe.nd instanceof PRQuadLeaf) {
				PRQuadLeaf node = (PRQuadLeaf) pqe.nd;
				return (T) node.e;
			} else if (pqe.nd instanceof PRQuadInternal) {
				PRQuadInternal node = (PRQuadInternal) pqe.nd;
				if (node.NW != null) {
					double distance = calculateNodeDistance(node.NW, x, y);
					if (node.NW instanceof PRQuadLeaf) {
						PRQuadLeaf nd = (PRQuadLeaf) node.NW;
						pq.add(new PriorityQueueElement(distance, node.NW, nd.name));
					} else {
						pq.add(new PriorityQueueElement(distance, node.NW, null));
					}
				}
				if (node.NE != null) {
					double distance = calculateNodeDistance(node.NE, x, y);
					if (node.NE instanceof PRQuadLeaf) {
						PRQuadLeaf n = (PRQuadLeaf) node.NE;
						pq.add(new PriorityQueueElement(distance, node.NE, n.name));
					} else {
						pq.add(new PriorityQueueElement(distance, node.NE, null));
					}
				}
				if (node.SW != null) {
					double distance = calculateNodeDistance(node.SW, x, y);
					if (node.SW instanceof PRQuadLeaf) {
						PRQuadLeaf n = (PRQuadLeaf) node.SW;
						pq.add(new PriorityQueueElement(distance, node.SW, n.name));
					} else {
						pq.add(new PriorityQueueElement(distance, node.SW, null));
					}
				}
				if (node.SE != null) {
					double distance = calculateNodeDistance(node.SE, x, y);
					if (node.SE instanceof PRQuadLeaf) {
						PRQuadLeaf n = (PRQuadLeaf) node.SE;
						pq.add(new PriorityQueueElement(distance, node.SE, n.name));
					} else {
						pq.add(new PriorityQueueElement(distance, node.SE, null));
					}
				}
			}
		}
		return null;
	}

	private double calculateNodeDistance(PRNode node, Integer x, Integer y) {
		if (node instanceof PRQuadLeaf) {
			PRQuadLeaf nd = (PRQuadLeaf) node;
			return euclideanDistance(nd.e.getX(), nd.e.getY(), x, y);
		} else if (node instanceof PRQuadInternal) {
			if (x < node.xl && y >= node.yh) {
				return euclideanDistance(node.xl, node.yh, x, y);
			} else if (x >= node.xl && x < node.xh && y > node.yh) {
				return y - node.yh;
			} else if (x >= node.xh && y >= node.yh) {
				return euclideanDistance(node.xh, node.yh, x, y);
			} else if (x < node.xl && y >= node.yl && y < node.yh) {
				return node.xl - x;
			} else if (x >= node.xl && x < node.xh && y >= node.yl && y < node.yh) {
				return 0;
			} else if (x > node.xh && y >= node.yl && y < node.yh) {
				return x - node.xh;
			} else if (x < node.xl && y < node.yl) {
				return euclideanDistance(node.xl, node.yl, x, y);
			} else if (x >= node.xl && x < node.xh && y < node.yl) {
				return node.yl - y;
			} else if (x >= node.xh && y < node.yl) {
				return euclideanDistance(node.xh, node.yl, x, y);
			}
		}
		return 0;
	}

	private double euclideanDistance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}

	public void print(Element parent, Document doc) {
		printTreeH(root, parent, doc);
	}

	private PRNode findElement(String cityName) {
		return nodeMap.get(cityName);
	}

	protected Map<String, PRNode> getNodeMap() {
		return nodeMap;
	}

	private int getDirection(T ele, double xh, double xl, double yh, double yl) {
		int ret = 0;
		
		if (ele.getX() >= (xh + xl) / 2 && ele.getY() >= (yh + yl) / 2) {
			ret = 2;
		} else if (ele.getX() < (xh + xl) / 2 && ele.getY() >= (yh + yl) / 2) {
			ret = 1;
		} else if (ele.getX() < (xh + xl) / 2 && ele.getY() < (yh + yl) / 2) {
			ret = 3;
		} else if (ele.getX() >= (xh + xl) / 2 && ele.getY() < (yh + yl) / 2) {
			ret = 4;
		}
		return ret;
	}

	private Boolean inCircleRange(double x, double y, Integer cx, Integer cy,
			Integer r) {
		double d = euclideanDistance(x, y, cx, cy);
		if (d <= r) {
			return true;
		}
		return false;
	}

	private Boolean inRectRange(double xh, double yh, double xl, double yl, Integer x,
			Integer y) {
		if (x <= xh && x >= xl && y <= yh && y >= yl) {
			return true;
		}
		return false;
	}

	private Boolean nodeInRange(PRNode node, Integer cx, Integer cy, Integer r) {
		if (inCircleRange(node.xh, node.yh, cx, cy, r)) {
			return true;
		}
		if (inCircleRange(node.xh, node.yl, cx, cy, r)) {
			return true;
		}
		if (inCircleRange(node.xl, node.yh, cx, cy, r)) {
			return true;
		}
		if (inCircleRange(node.xl, node.yl, cx, cy, r)) {
			return true;
		}
		if (inRectRange(node.xh, node.yh, node.xl, node.yl, cx - r, cy)) {
			return true;
		}
		if (inRectRange(node.xh, node.yh, node.xl, node.yl, cx + r, cy)) {
			return true;
		}
		if (inRectRange(node.xh, node.yh, node.xl, node.yl, cx, cy + r)) {
			return true;
		}
		if (inRectRange(node.xh, node.yh, node.xl, node.yl, cx, cy - r)) {
			return true;
		}
		return false;
	}

	private PRNode insertH(PRNode sRoot, T e, double xh, double xl, double yh,
			double yl, PRNode p, int dir, String n) {
		PRNode ret = null;
		if (sRoot == null) {
			ret = new PRQuadLeaf(e, xh, xl, yh, yl, p, n);
			nodeMap.put(n, ret);
			if (p == null) {
				root = ret;
				return ret;
			}
			PRQuadInternal pNode = (PRQuadInternal) p;
			switch (dir) {
			case 1:
				pNode.NW = ret;
				break;
			case 2:
				pNode.NE = ret;
				break;
			case 3:
				pNode.SW = ret;
				break;
			case 4:
				pNode.SE = ret;
				break;
			}
			pNode.numNd++;
			return ret;
		}
		if (sRoot instanceof PRQuadLeaf) {
			PRQuadLeaf node = (PRQuadLeaf) sRoot;
			PRQuadInternal newNode = new PRQuadInternal(node.xh, node.xl, node.yh, node.yl,
					node.parent);
			if (node.parent == null) {
				root = newNode;
			} else {
				PRQuadInternal pNode = (PRQuadInternal) node.parent;
				switch (dir) {
				case 1:
					pNode.NW = newNode;
					break;
				case 2:
					pNode.NE = newNode;
					break;
				case 3:
					pNode.SW = newNode;
					break;
				case 4:
					pNode.SE = newNode;
					break;
				}
			}
			splitAndInsert(newNode, (T) node.e, node.name);
			ret = splitAndInsert(newNode, e, n);
			// node.parent = null;
		} else if (sRoot instanceof PRQuadInternal) {
			PRQuadInternal node = (PRQuadInternal) sRoot;
			ret = splitAndInsert(node, e, n);
		}
		return ret;
	}

	private void removeH(PRNode nd) {
		if (nd == null) {
			return;
		}
		if (nd instanceof PRQuadInternal) {
			PRQuadInternal n = (PRQuadInternal) nd;
			if (n.numNd >= 2) {
				return;
			}
			PRNode child = null;
			if (n.numNd == 1) {
				// There should be only one non-null child
				if (n.SE != null) {
					child = n.SE;
				} else if (n.SW != null) {
					child = n.SW;
				} else if (n.NE != null) {
					child = n.NE;
				} else if (n.NW != null) {
					child = n.NW;
				}
			}
			if (child == null || child instanceof PRQuadLeaf) {
				if (nd.parent == null) {
					root = child;
					if (child != null) {
						child.parent = null;
						child.xh = mX;
						child.xl = mx;
						child.yh = mY;
						child.yl = my;
					}
				} else {
					PRQuadInternal parent = (PRQuadInternal) nd.parent;
					if (parent.SE == n) {
						parent.SE = child;
						if (child != null) {
							child.xh = parent.xh;
							child.xl = (parent.xh + parent.xl) / 2;
							child.yh = (parent.yh + parent.yl) / 2;
							child.yl = parent.yl;
						}
					} else if (parent.SW == n) {
						parent.SW = child;
						if (child != null) {
							child.xh = (parent.xh + parent.xl) / 2;
							child.xl = parent.xl;
							child.yh = (parent.yh + parent.yl) / 2;
							child.yl = parent.yl;
						}
					} else if (parent.NE == n) {
						parent.NE = child;
						if (child != null) {
							child.xh = parent.xh;
							child.xl = (parent.xh + parent.xl) / 2;
							child.yh = parent.yh;
							child.yl = (parent.yh + parent.yl) / 2;
						}
					} else if (parent.NW == n) {
						parent.NW = child;
						if (child != null) {
							child.xh = (parent.xh + parent.xl) / 2;
							child.xl = parent.xl;
							child.yh = parent.yh;
							child.yl = (parent.yh + parent.yl) / 2;
						}
					}
					if (child != null) {
						child.parent = parent;
					}
					if (child == null) {
						parent.numNd--;
					}
				}
			}
		} else if (nd instanceof PRQuadLeaf) {
			if (nd.parent == null) {
				root = null;
			} else {
				PRQuadInternal parent = (PRQuadInternal) nd.parent;
				if (parent.SE == nd) {
					parent.SE = null;
				} else if (parent.SW == nd) {
					parent.SW = null;
				} else if (parent.NE == nd) {
					parent.NE = null;
				} else if (parent.NW == nd) {
					parent.NW = null;
				}
				parent.numNd--;
			}
		}
		// node.parent = null;
		removeH(nd.parent);
	}

	private void printTreeH(PRNode sRoot, Element parent, Document doc) {
		if (sRoot == null) {
			Element white = doc.createElement("white");
			parent.appendChild(white);
			return;
		}
		if (sRoot instanceof PRQuadLeaf) {
			PRQuadLeaf node = (PRQuadLeaf) sRoot;
			Element black = doc.createElement("black");
			black.setAttribute("name", node.name);
			black.setAttribute("x", String.valueOf(Math.round(node.e.getX())));
			black.setAttribute("y", String.valueOf(Math.round(node.e.getY())));
			parent.appendChild(black);
		} else if (sRoot instanceof PRQuadInternal) {
			PRQuadInternal node = (PRQuadInternal) sRoot;
			Element gray = doc.createElement("gray");
			gray.setAttribute("x", String.valueOf(Math.round((node.xh + node.xl) / 2)));
			gray.setAttribute("y", String.valueOf(Math.round((node.yh + node.yl) / 2)));
			parent.appendChild(gray);
			printTreeH(node.NW, gray, doc);
			printTreeH(node.NE, gray, doc);
			printTreeH(node.SW, gray, doc);
			printTreeH(node.SE, gray, doc);
		}
	}

	private void drawTreeH(PRNode sRoot, CanvasPlus canvas) {
		if (sRoot == null) {
			return;
		}
		if (sRoot instanceof PRQuadLeaf) {
			PRQuadLeaf node = (PRQuadLeaf) sRoot;
			canvas.addPoint(node.name, node.e.getX(), node.e.getY(), Color.BLACK);
		} else if (sRoot instanceof PRQuadInternal) {
			PRQuadInternal node = (PRQuadInternal) sRoot;
			canvas.addLine(node.xl, (node.yh + node.yl) / 2, node.xh, (node.yh + node.yl) / 2, Color.BLACK);
			canvas.addLine((node.xh + node.xl) / 2, node.yl, (node.xh + node.xl) / 2, node.yh, Color.BLACK);
			drawTreeH(node.NW, canvas);
			drawTreeH(node.NE, canvas);
			drawTreeH(node.SW, canvas);
			drawTreeH(node.SE, canvas);
		}
	}

	private void rangeSearchH(PRNode sRoot, Integer cx, Integer cy,
			Integer r, List<T> result) {
		if (sRoot instanceof PRQuadLeaf) {
			PRQuadLeaf node = (PRQuadLeaf) sRoot;
			if (inCircleRange(node.e.getX(), node.e.getY(), cx, cy, r)) {
				result.add((T) node.e);
			}
		} else if (sRoot instanceof PRQuadInternal) {
			PRQuadInternal node = (PRQuadInternal) sRoot;
			if (node.NW != null && nodeInRange(node.NW, cx, cy, r)) {
				rangeSearchH(node.NW, cx, cy, r, result);
			}
			if (node.NE != null && nodeInRange(node.NE, cx, cy, r)) {
				rangeSearchH(node.NE, cx, cy, r, result);
			}
			if (node.SW != null && nodeInRange(node.SW, cx, cy, r)) {
				rangeSearchH(node.SW, cx, cy, r, result);
			}
			if (node.SE != null && nodeInRange(node.SE, cx, cy, r)) {
				rangeSearchH(node.SE, cx, cy, r, result);
			}
		}
	}

}
