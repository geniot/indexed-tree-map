package io.github.geniot.indexedtreemap;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SublistTest {
    @Test
    public void testSublist() {
        Random random = new Random(System.currentTimeMillis());
        Set<Integer> hashSet = new HashSet<Integer>();
        IndexedNavigableSet<Integer> indexedSet = new IndexedTreeSet<>();

        int size = 1000;

        while (hashSet.size() < size) {
            Integer next = random.nextInt();
            if (!hashSet.contains(next)) {
                hashSet.add(next);
                indexedSet.add(next);
            }
        }

        Integer from = indexedSet.exact(0);
        Integer to = indexedSet.exact(indexedSet.size() - 1);
        SortedSet<Integer> sortedSet = indexedSet.subSet(from, to);
        int counter = 0;
        for (Integer i : sortedSet) {
            assertEquals(indexedSet.exact(counter++), i);
        }
    }
}
