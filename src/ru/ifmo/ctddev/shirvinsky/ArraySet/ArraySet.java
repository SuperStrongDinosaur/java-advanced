package ru.ifmo.ctddev.shirvinsky.ArraySet;

import java.util.*;

/**
 * Created by SuperStrongDinosaur on 23.02.17.
 */
public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T>{
    final private List<T> list;
    final private Comparator<T> comparator;

    public ArraySet() {
        list = Collections.emptyList();
        comparator = null;
    }

    public ArraySet(Collection<T> other, Comparator<T> otherComparator) {
        comparator = otherComparator;
        TreeSet<T> temp = new TreeSet<T>(comparator);
        temp.addAll(other);
        list = new ArrayList<T>(temp);
    }

    public ArraySet(Collection<? extends T> collection) {
        this((Collection<T>) collection, null);
    }

    @Override
    public T lower(T t) {
        return null;
    }

    @Override
    public T floor(T t) {
        return null;
    }

    @Override
    public T ceiling(T t) {
        return null;
    }

    @Override
    public T higher(T t) {
        return null;
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException("ArraySet is immutable");
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException("ArraySet is immutable");
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(list).iterator();
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return null;
    }

    @Override
    public Iterator<T> descendingIterator() {
        return null;
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        return null;
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        return null;
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        return null;
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return null;
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return null;
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return null;
    }

    @Override
    public T first() {
        return list.get(0);
    }

    @Override
    public T last() {
        return list.get(list.size() - 1);
    }

    @Override
    public int size() {
        return list.size();
    }
}
