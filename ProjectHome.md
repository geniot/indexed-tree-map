I needed to find an element in a sorted set/map by its index. After some research I found out that most of the time people recommend using some alternatives (e.g. a tree map backed up by a list, or copying elements to an array every time you need to access them by index). There was a hint on stackoverflow about adding an additional field to the internal Entry object that would contain the number of nodes beneath it. But I could not find an implementation of this enhancement. So I decided to write it myself.

I added a field called weight to the internal TreeMap's Entry class and it is updated properly every time there is a change in the red-black tree. The weight field contains the number of nodes beneath the current node (plus one - self). Search for an entry by index goes from the root narrowing down to the searched entry.

The class is called IndexedTreeMap and it implements IndexedNavigableMap:

```
public interface IndexedNavigableMap<K, V> extends NavigableMap<K, V> {
    K exactKey(int index);
    Map.Entry<K, V> exactEntry(int index);
    int keyIndex(K k);
}
```

Similarly there is IndexedNavigableSet:

```
public interface IndexedNavigableSet<E> extends NavigableSet<E> {
    E exact(int index);
    int entryIndex(E e);
}
```

Methods that look up the key by index:

```
    public K exactKey(int index) {
        if (index < 0 || index > size() - 1) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return getExactKey(root, index);
    }

    private K getExactKey(Entry<K, V> e, int index) {
        if (e.left==null && index==0){
            return e.key;
        }
        if (e.left==null && e.right==null){
            return e.key;
        }
        if (e.left != null && e.left.weight > index){
            return getExactKey(e.left, index);
        }
        if (e.left != null && e.left.weight == index){
            return e.key;
        }
        return getExactKey(e.right, index - (e.left == null ? 0 : e.left.weight) - 1);
    }
```

Method that finds the index of a key:

```
    public int keyIndex(K key) {
        if (key == null) {
            throw new NullPointerException();
        }
        Entry<K, V> e = getEntry(key);
        if (e == null) {
            throw new NullPointerException();
        }
        if (e == root) {
            return getWeight(e) - getWeight(e.right) - 1;//index to return
        }
        int index = 0;
        int cmp;
        index += getWeight(e.left);
       
        Entry<K, V> p = e.parent;
        // split comparator and comparable paths
        Comparator<? super K> cpr = comparator;
        if (cpr != null) {
            while (p != null) {
                cmp = cpr.compare(key, p.key);
                if (cmp > 0) {
                    index += getWeight(p.left) + 1;
                }
                p = p.parent;
            }
        } else {
            Comparable<? super K> k = (Comparable<? super K>) key;
            while (p != null) {
                if (k.compareTo(p.key) > 0) {
                    index += getWeight(p.left) + 1;
                }
                p = p.parent;
            }
        }
        return index;
    }
```

Updating weight every time the tree is changed (insert/remove operation). This method goes up to the root updating all parents with delta weight:

```
        void updateWeight(int delta) {
            weight += delta;
            Entry<K, V> p = parent;
            while (p != null) {
                p.weight += delta;
                p = p.parent;
            }
        }
```

Enhanced rotateLeft that updates weight as well if necessary:

```
    private void rotateLeft(Entry<K, V> p) {
        if (p != null) {
            Entry<K, V> r = p.right;

            int delta = getWeight(r.left) - getWeight(p.right);
            p.right = r.left;
            p.updateWeight(delta);

            if (r.left != null) {
                r.left.parent = p;
            }

            r.parent = p.parent;


            if (p.parent == null) {
                root = r;
            } else if (p.parent.left == p) {
                delta = getWeight(r) - getWeight(p.parent.left);
                p.parent.left = r;
                p.parent.updateWeight(delta);
            } else {
                delta = getWeight(r) - getWeight(p.parent.right);
                p.parent.right = r;
                p.parent.updateWeight(delta);
            }

            delta = getWeight(p) - getWeight(r.left);
            r.left = p;
            r.updateWeight(delta);

            p.parent = r;
        }
    }
```

This is pretty much all the enhancement I did to the java.util.TreeMap
You can download the package or check out the source code. I will focus on IndexedTreeSet and IndexedTreeMap in this project.
One other idea for a future release: persistence through a layer of collections. E.g. PersistedIndexedTreeMap - where all data would be stored in a key-value database and the class would be used as a wrapper interface around a back end.


Please note that I had to copy Java code from JDK's java.util.TreeMap which has its own license. I'm not sure if Lesser GPL is the right choice that doesn't violate Oracle's license. Use this project at your own discretion.