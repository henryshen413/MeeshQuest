package cmsc420.sortedmap;

import java.util.Comparator;
import java.util.Map;

public final class AvlNode<K, V> extends AvlGTree implements Map.Entry<K, V> {
    public K key;
    public V value;
    public AvlNode<K, V> left = null;
    public AvlNode<K, V> right = null;
    public AvlNode<K, V> parent = null;
    Comparator<? super K> comparator;

    AvlNode(K key, V value, Comparator<? super K> comp) {
        this.key = key;
        this.value = value;
        this.parent = null;
        this.comparator = comp;
    }

    public V add(AvlNode<K, V> node) {
        int cmp = compare(node.key, this.key);
        if (cmp < 0) {
            if (left == null) {
                left = node;
                left.parent = this;
                return null;
            } else {
                V ret = this.left.add(node);
                return ret;
            }
        } else if (cmp > 0) {
            if (right == null) {
                right = node;
                right.parent = this;
                return null;
            } else {
                V ret = this.right.add(node);
                return ret;
            }
        } else {
            return this.setValue(node.value);
        }
    }

    public int hashCode() {
        int keyHash = (key == null ? 0 : key.hashCode());
        int valueHash = (value == null ? 0 : value.hashCode());
        return keyHash ^ valueHash;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Map.Entry))
            return false;
        Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;

        return valEquals(key, e.getKey()) && valEquals(value, e.getValue());
    }

    public String toString() {
        return key + "=" + value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public V setValue(V value) {
        V oldValue = this.value;
        this.value = value;
        return oldValue;
    }

    @SuppressWarnings({ "unchecked" })
    private int compare(Object k1, Object k2) {
        return comparator == null ? ((Comparable<? super K>) k1)
                .compareTo((K) k2) : comparator.compare((K) k1, (K) k2);
    }
}

