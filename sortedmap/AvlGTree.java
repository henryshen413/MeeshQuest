package cmsc420.sortedmap;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AvlGTree<K, V> extends AbstractMap<K, V> implements
        SortedMap<K, V> {
    public final int g;

    private Comparator<? super K> comparator = null;
    private AvlNode<K, V> root = null;
    private int size = 0;
    private int modCount = 0;
    private EntrySet entrySet = null;

    public AvlGTree() {
        this.g = 1;
    }

    public AvlGTree(final Comparator<? super K> comp) {
        this.comparator = comp;
        this.g = 2;
    }

    public AvlGTree(final int g) {
        this.g = g;
    }

    public AvlGTree(final Comparator<? super K> comp, final int g) {
        this.comparator = comp;
        this.g = g;
    }

    @Override
    public Comparator<? super K> comparator() {
        return comparator;
    }

    @Override
    public void clear() {
        modCount++;
        size = 0;
        root = null;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    public void printXml(Element node, Document doc) {
		Element avlGTree = doc.createElement("AvlGTree");
		avlGTree.setAttribute("cardinality", String.valueOf(size));
		avlGTree.setAttribute("height", String.valueOf(getHeight(root)+1));
		avlGTree.setAttribute("maxImbalance", String.valueOf(g));
		printXmlHelper(root, avlGTree, doc);
		node.appendChild(avlGTree);
	}
	
	private void printXmlHelper(AvlNode<K, V> nd, Element e, Document doc) {
		if (nd == null) {
			Element emptyChild = doc.createElement("emptyChild");
			e.appendChild(emptyChild);
		} else {
			Element node = doc.createElement("node");
			node.setAttribute("key", nd.getKey().toString());
			node.setAttribute("value", nd.getValue().toString());
			e.appendChild(node);
			printXmlHelper(nd.left, node, doc);
			printXmlHelper(nd.right, node, doc);
		}
	}
	
	@Override
    public boolean containsKey(Object key) {
        if (key == null)
            throw new NullPointerException();
        return getNode(key) != null;
    }

	@Override
    public boolean containsValue(Object value) {
        if (value == null)
            throw new NullPointerException();
        return nodeContainsValue(root, value);
    }

	@Override
    public V get(Object key) {
        if (key == null)
            throw new NullPointerException();

        AvlNode<K, V> p = getNode(key);
        return (p == null ? null : p.value);
    }

	@Override
    public V put(K key, V value) {
        if (key == null || value == null)
            throw new NullPointerException();

        AvlNode<K, V> t = root;
        if (t == null) {
            root = new AvlNode<K, V>(key, value, comparator);
            size = 1;
            modCount++;
            return null;
        }
        AvlNode<K, V> e = new AvlNode<K, V>(key, value, comparator);
        V oldValue = root.add(e);

        modCount++;
        if (oldValue == null) {
            fixAfterModification(e);
            size++;
            return null;
        } else {
            return oldValue;
        }
    }

	@Override
    public V remove(Object key) {
    	AvlNode<K, V> p = getNode(key);
    	if (p == null) {
    		return null;
    	}
    	V oldValue = p.value;
    	deleteNode(p);
    	return oldValue;
    }
	
	private void deleteNode(AvlNode<K, V> p) {
		modCount ++;
		size --;
		
		if (p.left != null && p.right != null) {
			AvlNode<K, V> s = successor(p);
			p.key = s.key;
			p.value = s.value;
			p = s;
		}
		
		AvlNode<K, V> replacement = (p.left != null ? p.left : p.right);
		if (replacement != null) {
			replacement.parent = p.parent;
			if (p.parent == null) {
				root = replacement;
			} else if (p == p.parent.left) {
				p.parent.left = replacement;
			} else {
				p.parent.right = replacement;
			}
			
			p.left = p.right = p.parent = null;
			if (replacement.parent != null) {
				fixAfterModification(replacement.parent);
			}
		} else if (p.parent == null) {
			root = null;
		} else {
			if (p == p.parent.left) {
				p.parent.left = null;
			} else if (p == p.parent.right) {
				p.parent.right = null;
			}
			AvlNode<K, V> parentNode = p.parent;
			p.parent = null;
			fixAfterModification(parentNode);
		}
	}

	@Override
    public K firstKey() {
        return key(getFirstNode());
    }

	@Override
    public K lastKey() {
        return key(getLastNode());
    }

	@Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        EntrySet es = entrySet;
        return (es != null) ? es : (entrySet = new EntrySet());
    }

	@Override
    public Set<K> keySet() {
    	throw new UnsupportedOperationException();
    }

	@Override
    public Collection<V> values() {
    	throw new UnsupportedOperationException();
    }

	@Override
    public SortedMap<K, V> headMap(K toKey) {
    	throw new UnsupportedOperationException();
    }

	@Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        return new SubMap<K, V>(this, fromKey, toKey);
    }

	@Override
    public SortedMap<K, V> tailMap(K fromKey) {
    	throw new UnsupportedOperationException();
    }
    
    private int getHeight(AvlNode<K, V> node) {
    	if (node == null) {
    		return -1;
    	}
    	return Math.max(getHeight(node.left), getHeight(node.right)) + 1;
    }
    
    private int getBalance(AvlNode<K, V> node) {
    	if (node == null) {
    		return 0;
    	}
    	return getHeight(node.left) - getHeight(node.right);
    }

    private final AvlNode<K, V> getNode(Object key) {
        AvlNode<K, V> p = root;
        while (p != null) {
            int cmp = compare(key, p.key);
            if (cmp < 0)
                p = p.left;
            else if (cmp > 0)
                p = p.right;
            else
                return p;
        }
        return null;
    }

    private final boolean nodeContainsValue(AvlNode<K, V> node, Object value) {
        if (node == null)
            return false;

        if (node.value.equals(value))
            return true;
        else
            return nodeContainsValue(node.left, value)
                    || nodeContainsValue(node.right, value);
    }

    private final AvlNode<K, V> getFirstNode() {
        AvlNode<K, V> p = root;
        if (p != null)
            while (p.left != null)
                p = p.left;
        return p;
    }

    private final AvlNode<K, V> getLastNode() {
        AvlNode<K, V> p = root;
        if (p != null)
            while (p.right != null)
                p = p.right;
        return p;
    }

    private final NodeIterator getNodeIterator() {
        return new NodeIterator(getFirstNode());
    }

    private final ReverseNodeIterator getReverseNodeIterator() {
        return new ReverseNodeIterator(getLastNode());
    }

    private static <K, V> AvlNode<K, V> successor(AvlNode<K, V> t) {
        if (t == null)
            return null;
        else if (t.right != null) {
            AvlNode<K, V> p = t.right;
            while (p.left != null)
                p = p.left;
            return p;
        } else {
            AvlNode<K, V> p = t.parent;
            AvlNode<K, V> ch = t;
            while (p != null && ch == p.right) {
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }

    private static <K, V> AvlNode<K, V> predecessor(AvlNode<K, V> t) {
        if (t == null)
            return null;
        else if (t.left != null) {
            AvlNode<K, V> p = t.left;
            while (p.right != null)
                p = p.right;
            return p;
        } else {
            AvlNode<K, V> p = t.parent;
            AvlNode<K, V> ch = t;
            while (p != null && ch == p.left) {
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }

    private void fixAfterModification(AvlNode<K, V> e) {
        if (getBalance(e) > g) {
            if (getBalance(e.left) >= 0)
                e = rotateRight(e);
            else
                e = rotateLeftRight(e);
        } else if (getBalance(e) < -g) {
            if (getBalance(e.right) <= 0)
                e = rotateLeft(e);
            else
                e = rotateRightLeft(e);
        }

        if (e.parent != null)
            fixAfterModification(e.parent);
        else
            this.root = e;
    }

    private AvlNode<K, V> rotateRight(AvlNode<K, V> p) {
        if (p == null)
            return null;

        AvlNode<K, V> l = p.left;
        p.left = l.right;
        if (l.right != null)
            l.right.parent = p;
        l.parent = p.parent;
        if (p.parent != null) {
            if (p.parent.right == p)
                p.parent.right = l;
            else
                p.parent.left = l;
        }
        l.right = p;
        p.parent = l;
        return l;
    }

    private AvlNode<K, V> rotateLeft(AvlNode<K, V> p) {
        if (p == null)
            return null;

        AvlNode<K, V> r = p.right;
        p.right = r.left;
        if (r.left != null)
            r.left.parent = p;
        r.parent = p.parent;
        if (p.parent != null) {
            if (p.parent.left == p)
                p.parent.left = r;
            else
                p.parent.right = r;
        }
        r.left = p;
        p.parent = r;
        return r;

    }

    private AvlNode<K, V> rotateRightLeft(AvlNode<K, V> p) {
        p.right = rotateRight(p.right);
        return rotateLeft(p);
    }

    private AvlNode<K, V> rotateLeftRight(AvlNode<K, V> p) {
        p.left = rotateLeft(p.left);
        return rotateRight(p);
    }

    private static <K> K key(Map.Entry<K, ?> e) {
        if (e == null)
            throw new NoSuchElementException();
        return e.getKey();
    }

    @SuppressWarnings("unchecked")
    private final int compare(Object k1, Object k2) {
        return comparator == null ? ((Comparable<? super K>) k1)
                .compareTo((K) k2) : comparator.compare((K) k1, (K) k2);
    }

    class EntrySet extends AbstractSet<Map.Entry<K, V>> {
    	
    	@Override
        public Iterator<java.util.Map.Entry<K, V>> iterator() {
            return new EntryIterator(getFirstNode());
        }
    	
        public void clear() {
            AvlGTree.this.clear();
        }

        @Override
        public int size() {
            return AvlGTree.this.size();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            @SuppressWarnings("unchecked")
            Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
            V value = entry.getValue();
            AvlNode<K, V> p = getNode(entry.getKey());
            return p != null && valEquals(p.getValue(), value);
        }

        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry))
            	return false;
            @SuppressWarnings("unchecked")
            Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
            V value = entry.getValue();
            AvlNode<K, V> p = getNode(entry.getKey());
            if (p != null && valEquals(p.getValue(), value)) {
            	deleteNode(p);
            	return true;
            }
            return false;
        }
    }

    abstract class PrivateNodeIterator<T> implements Iterator<T> {
        AvlNode<K, V> next;
        AvlNode<K, V> lastReturned;
        int expectedModCount;

        public PrivateNodeIterator(AvlNode<K, V> first) {
            expectedModCount = modCount;
            lastReturned = null;
            next = first;
        }

        public final boolean hasNext() {
            return next != null;
        }

        final AvlNode<K, V> nextNode() {
            AvlNode<K, V> e = next;
            if (e == null)
                throw new NoSuchElementException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();

            next = successor(e);
            lastReturned = e;
            return e;
        }

        final AvlNode<K, V> prevNode() {
            AvlNode<K, V> e = next;
            if (e == null)
                throw new NoSuchElementException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            next = predecessor(e);
            lastReturned = e;
            return e;
        }

        public void remove() {
            return;
        }
    }

    final class NodeIterator extends PrivateNodeIterator<AvlNode<K, V>> {
        NodeIterator(AvlNode<K, V> first) {
            super(first);
        }

        public AvlNode<K, V> next() {
            return nextNode();
        }
    }

    final class ReverseNodeIterator extends PrivateNodeIterator<AvlNode<K, V>> {
        ReverseNodeIterator(AvlNode<K, V> last) {
            super(last);
        }

        public AvlNode<K, V> next() {
            return prevNode();
        }
    }

    final class EntryIterator extends PrivateNodeIterator<Map.Entry<K, V>> {
        EntryIterator(AvlNode<K, V> first) {
            super(first);
        }

        public Map.Entry<K, V> next() {
            return nextNode();
        }
    }

    public final static boolean valEquals(Object o1, Object o2) {
        return (o1 == null ? o2 == null : o1.equals(o2));
    }

    @SuppressWarnings("hiding")
    final class SubMap<K, V> extends AbstractMap<K, V> implements
            SortedMap<K, V> {
        final AvlGTree<K, V> m;
        final K low;
        final K high;
        EntrySetView entrySetView = null;

        SubMap(AvlGTree<K, V> m, K low, K high) {
            if (low == null && high == null)
                throw new IllegalArgumentException();

            if (low != null && high != null)
                if (m.compare(low, high) > 0)
                    throw new IllegalArgumentException();

            this.m = m;
            this.low = low;
            this.high = high;
        }

        public Comparator<? super K> comparator() {
            return m.comparator();
        }

        public final V put(K key, V value) {
            if (!inRange(key))
                throw new IllegalArgumentException("key out of range");
            return m.put(key, value);
        }

        public final V remove(Object key) {
            if (!inRange(key))
            	throw new IllegalArgumentException("key out of range");
            return m.remove(key);
        }

        public K firstKey() {
            return key(getFirstNode());
        }

        AvlNode<K, V> getFirstNode() {
            if (low == null) {
                AvlNode<K, V> first = m.getFirstNode();
                if (compare(first.getKey(), high) < 0)
                    return first;
                else
                    return null;
            } else {
                Iterator<AvlNode<K, V>> i = m.getNodeIterator();
                AvlNode<K, V> e;
                while (i.hasNext()) {
                    e = i.next();
                    int cmp = m.compare(e.getKey(), low);
                    if (cmp >= 0)
                        return e;
                }
                return null;
            }
        }

        public K lastKey() {
            return key(getLastNode());
        }

        final Entry<K, V> getLastNode() {
            if (high == null) {
                AvlNode<K, V> last = m.getLastNode();
                if (compare(last.getKey(), low) >= 0)
                    return last;
                else
                    return null;
            } else {
                Iterator<AvlNode<K, V>> i = m.getReverseNodeIterator();
                Entry<K, V> e;
                while (i.hasNext()) {
                    e = i.next();
                    int cmp = m.compare(e.getKey(), high);
                    if (cmp < 0)
                        return e;
                }
                return null;
            }
        }

        public Set<Map.Entry<K, V>> entrySet() {
            EntrySetView esv = entrySetView;
            return (esv != null) ? esv : (entrySetView = new EntrySetView());
        }

        public SortedMap<K, V> headMap(K toKey) {
        	throw new UnsupportedOperationException();
        }

        public SortedMap<K, V> subMap(K fromKey, K toKey) {
            if (!inRange(fromKey) || !inRange(toKey))
                throw new IllegalArgumentException();

            return new SubMap<K, V>(m, fromKey, toKey);
        }

        public SortedMap<K, V> tailMap(K fromKey) {
        	throw new UnsupportedOperationException();
        }

        final boolean tooLow(Object key) {
            if (low != null) {
                int c = m.compare(key, low);
                if (c < 0)
                    return true;
            }
            return false;
        }

        final boolean tooHigh(Object key) {
            if (high != null) {
                int c = m.compare(key, high);
                if (c >= 0)
                    return true;
            }
            return false;
        }

        final boolean inRange(Object key) {
            return !tooLow(key) && !tooHigh(key);
        }

        class EntrySetView extends AbstractSet<Map.Entry<K, V>> {
            public Iterator<Map.Entry<K, V>> iterator() {
                return new Iterator<Map.Entry<K, V>>() {
                    int expmc = m.modCount;
                    AvlNode<K, V> next = getFirstNode();
                    AvlNode<K, V> lastReturned = null;

                    public boolean hasNext() {
                        if (next != null)
                            return inRange(next.key);
                        else
                            return false;
                    }

                    public java.util.Map.Entry<K, V> next() {
                        AvlNode<K, V> e = next;
                        if (e == null)
                            throw new NoSuchElementException();
                        if (m.modCount != expmc)
                            throw new ConcurrentModificationException();

                        next = successor(e);
                        if (next != null && !inRange(next.key))
                            next = null;

                        lastReturned = e;
                        return e;
                    }

                    public void remove() {
                        return;
                    }
                };
            }

            public int size() {
                int size = 0;
                Iterator<Entry<K, V>> i = iterator();
                while (i.hasNext()) {
                    size++;
                    i.next();
                }
                return size;
            }

            public boolean remove(Object o) {
            	if (!(o instanceof Map.Entry))
                	return false;
                @SuppressWarnings("unchecked")
                Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
                K key = entry.getKey();
                if (!inRange(key))
                	return false;
                AvlNode<K, V> p = m.getNode(key);
                if (p != null && valEquals(p.getValue(), entry.getValue())) {
                	m.deleteNode(p);
                	return true;
                }
                return false;
            }
        }
    }

}
