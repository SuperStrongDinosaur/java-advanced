package ru.ifmo.ctddev.shirvinsky.arrayset;

import java.util.*;

/**
 * Created by SuperStrongDinosaur on 23.02.17.
 */
public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    final private List<T> list;
    final private Comparator<T> comparator;

    public ArraySet() {
        this(Collections.emptyList(), null, false);
    }

    public ArraySet(Collection<T> collection, Comparator<T> comparator) {
        this(collection, comparator, false);
    }

    public ArraySet(Collection<? extends T> collection) {
        this((Collection<T>) collection, null, false);
    }

    private ArraySet(Collection<T> collection, Comparator<T> comparator, boolean isSorted) {
        this.comparator = comparator;
        if (!isSorted) {
            TreeSet<T> treeSet = new TreeSet<>(comparator);
            treeSet.addAll(collection);
            list = new ArrayList<>(treeSet);
        } else {
            this.list = (List<T>) collection;
        }
    }

    private T correctResForBinarySearch(int ifEqual, int ifNeededLower, T e) {
        int index = Collections.binarySearch(list, e, comparator);
        if (index >= 0) {
            index += ifEqual;
        } else {
            index = -index - 1 - ifNeededLower;
        }
        if (index >= 0 && index < list.size()) {
            return list.get(index);
        }
        return null;
    }

    @Override
    public T lower(T t) {
        return correctResForBinarySearch(-1, 1, t);
    }

    @Override
    public T floor(T t) {
        return correctResForBinarySearch(0, 1, t);
    }

    @Override
    public T ceiling(T t) {
        return correctResForBinarySearch(0, 0, t);
    }

    @Override
    public T higher(T t) {
        return correctResForBinarySearch(1, 0, t);
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException("arrayset is immutable");
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException("arrayset is immutable");
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(list).iterator();
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArraySet<>(new DescendingList<>(list), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<T> descendingIterator() {
        return new DescendingList<>(list).iterator();
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        int fromIndex = Collections.binarySearch(list, fromElement, comparator);
        if (fromIndex < 0) {
            fromIndex = -fromIndex - 1;
        } else if (!fromInclusive) {
            fromIndex++;
        }

        int toIndex = Collections.binarySearch(list, toElement, comparator);
        if (toIndex < 0) {
            toIndex = -toIndex - 1;
        } else if (toInclusive) {
            toIndex++;
        }

        if (fromIndex > toIndex) {
            fromIndex = toIndex;
        }
        return new ArraySet<>(list.subList(fromIndex, toIndex), comparator, true);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        return subSet(size() == 0 ? null : first(), true, toElement, inclusive);
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        return subSet(fromElement, inclusive, size() == 0 ? null : last(), true);
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public T first() {
        if (list.size() == 0) {
            throw new NoSuchElementException();
        }
        return list.get(0);
    }

    @Override
    public T last() {
        if (list.size() == 0) {
            throw new NoSuchElementException();
        }
        return list.get(list.size() - 1);
    }

    @Override
    public int size() {
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(list, o, (Comparator<Object>) comparator) >= 0;
    }

    private class DescendingList<E> extends AbstractList<E> implements RandomAccess {
        private final List<E> list;
        private final boolean isReversed;

        DescendingList(List<E> list) {
            if (!(list instanceof DescendingList)) {
                this.list = list;
                isReversed = true;
            } else {
                this.list = ((DescendingList<E>) list).list;
                isReversed = !((DescendingList<E>) list).isReversed;
            }
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public E get(int index) {
            if (isReversed) {
                return list.get(size() - index - 1);
            }
            return list.get(index);
        }
    }
}
