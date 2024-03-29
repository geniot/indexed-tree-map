# indexed-tree-map

JDK's TreeMap / TreeSet implementation is based on the [red-black tree](https://en.wikipedia.org/wiki/Red%E2%80%93black_tree) algorithm.

**indexed-tree-map** is an enhanced version of this algorithm.

# API
    public interface IndexedNavigableSet<E> extends NavigableSet<E> {
        E exact(int index);
        int entryIndex(E e);
    }

    public interface IndexedNavigableMap<K, V> extends NavigableMap<K, V> {
        K exactKey(int index);
        Entry<K, V> exactEntry(int index);
        int keyIndex(K k);
    }
    
Or simply use [IndexedTreeSet.java](https://github.com/geniot/indexed-tree-map/blob/master/src/main/java/io/github/geniot/indexedtreemap/IndexedTreeSet.java) and [IndexedTreeMap.java](https://github.com/geniot/indexed-tree-map/blob/master/src/main/java/io/github/geniot/indexedtreemap/IndexedTreeMap.java) to avoid the interface complexity.

# How to use / install

        <dependency>
            <groupId>io.github.geniot</groupId>
            <artifactId>indexedtreemap</artifactId>
            <version>1.1</version>
        </dependency>

# Implementation

I copied SDK's code and added __*int weight*__ field to each node of the tree. 
It contains the number of elements below the node plus 1 (self).

Every time there is a change in the tree, weight is updated for changed nodes like this:

        void updateWeight(int delta) {
            weight += delta;
            Entry<K, V> p = parent;
            while (p != null) {
                p.weight += delta;
                p = p.parent;
            }
        }

We go from updated node to the root.

Let's take a look at the following tree. I added weight values to each node.

![Red-black tree with weights](screenshots/Red-black_tree_example.svg.png)

See how the weight of our root node is also the size of the tree (total number of elements).

So how do you get the index of an element in the tree using weights?

E.g. what is the index value of 11 in the tree above?

Let's take a look at the following method:

        // e is our element - 11
        int index = 0;
        int cmp;
        index += getWeight(e.left); // 0
        Entry<K, V> p = e.parent;
        Comparator<? super K> cpr = comparator;

        while (p != null) {
                cmp = cpr.compare(key, p.key);
                if (cmp > 0) {
                    index += getWeight(p.left) + 1;
                }
                p = p.parent;
        }

e.left + parent.left + 1 + (parent.parent.left + 1...) = 3 ! 

parent:8, parent.left:1, parent.left.weight=2

Index starts from 0. 11 is 4th element, so index = 3


Indeed. If we draw vertical lines through centers of each node 
we can see that lines reveal the sorted order of our tree elements.

**So to get the index of an element we need to sum up all weights to the left of our node.**

If you are not sure about this check out the test:

https://github.com/geniot/indexed-tree-map/blob/master/src/test/java/io/github/geniot/indexedtreemap/IndexedTreeSetTest.java

# License
The same as of JDK. I copied the implementation and enhanced it. 
I don't think I have the right to change the license.

https://www.oracle.com/downloads/licenses/javase-license1.html

# Links
Popular questions on Stackoverflow:

How to return the k-th element in TreeSet in Java?
https://stackoverflow.com/questions/8856815/how-to-return-the-k-th-element-in-treeset-in-java

Find element position in a Java TreeMap
https://stackoverflow.com/questions/8502542/find-element-position-in-a-java-treemap

Why is there no SortedList in Java?
https://stackoverflow.com/questions/8725387/why-is-there-no-sortedlist-in-java

Any implementation of Ordered Set in Java?
https://stackoverflow.com/questions/8712469/any-implementation-of-ordered-set-in-java

Why doesn't java.util.Set have get(int index)?
https://stackoverflow.com/questions/769731/why-doesnt-java-util-set-have-getint-index

How to find the index of an element in a TreeSet?
https://stackoverflow.com/questions/7911621/how-to-find-the-index-of-an-element-in-a-treeset

What is the most efficient way to access particular elements in a SortedSet?
https://stackoverflow.com/questions/5334020/what-is-the-most-efficient-way-to-access-particular-elements-in-a-sortedset
