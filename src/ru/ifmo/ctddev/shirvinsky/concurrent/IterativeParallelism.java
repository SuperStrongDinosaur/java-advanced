package ru.ifmo.ctddev.shirvinsky.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class IterativeParallelism implements ListIP {

    private ParallelMapper mapper = null;

    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    public IterativeParallelism() {
    }

    private <T> List<List<? extends T>> split(List<? extends T> list, int n) {
        List<List<? extends T>> pieces = new ArrayList<>(n);
        double step = Math.max(list.size() / (double) n, 1);
        int from = 0;
        for (double to = step; from < list.size(); to += step) {
            int ito = (int) Math.floor(to);
            pieces.add(list.subList(from, Math.min(ito, list.size())));
            from = ito;
        }
        return pieces;
    }

    private <T, S, U> U runPieces(int threadsCount, List<? extends T> data, final Function<List<? extends T>, S> func, final Function<List<? extends S>, U> combiner) throws InterruptedException {
        List<List<? extends T>> lists = split(data, threadsCount);

        if (mapper != null) {
            return combiner.apply(mapper.map(func, lists));
        }

        List<S> funcRes = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < lists.size(); i++) {
            final int ind = i;
            funcRes.add(null);
            threads.add(new Thread(() -> funcRes.set(ind, func.apply(lists.get(ind)))));
            threads.get(ind).start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        return combiner.apply(funcRes);
    }

    @Override
    public String join(int i, List<?> list) throws InterruptedException {
        return runPieces(i, list, data -> data.stream().map(Object::toString).collect(Collectors.joining()), data -> String.join("", data));
    }

    @Override
    public <T> List<T> filter(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return runPieces(i, list, (Function<java.util.List<? extends T>, List<T>>) data -> data.stream().filter(predicate).collect(Collectors.toList()),
                data -> data.stream().flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T, U> List<U> map(int i, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        return runPieces(i, list, (Function<java.util.List<? extends T>, List<U>>) data -> data.stream().map(function).collect(Collectors.toList()),
                data -> data.stream().flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T> T maximum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return runPieces(i, list, data -> Collections.max(data, comparator), data -> Collections.max(data, comparator));
    }

    @Override
    public <T> T minimum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(i, list, comparator.reversed());
    }

    @Override
    public <T> boolean all(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return runPieces(i, list, data -> data.stream().allMatch(predicate), data -> data.stream().allMatch(Boolean::booleanValue));
    }

    @Override
    public <T> boolean any(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return runPieces(i, list, data -> data.stream().anyMatch(predicate), data -> data.stream().anyMatch(Boolean::booleanValue));
    }
}