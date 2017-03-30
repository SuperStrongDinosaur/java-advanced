package ru.ifmo.ctddev.shirvinsky.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by SuperStrongDinosaur on 26.03.17.
 */
public class ParallelMapperImpl implements ParallelMapper {
    final private Thread[] threads;
    final private Queue<Consumer<Void>> queue;

    public ParallelMapperImpl(int cnt) {
        queue = new ArrayDeque<>();
        threads = new Thread[cnt];
        for (int i = 0; i < cnt; i++) {
            threads[i] = new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        Consumer<Void> data = null;
                        synchronized (queue) {
                            if (!queue.isEmpty()) {
                                data = queue.poll();
                            }
                        }
                        if (data != null) {
                            data.accept(null);
                            synchronized (queue) {
                                queue.notifyAll();
                            }
                        } else {
                            synchronized (queue) {
                                queue.wait();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    //return;
                } finally {
                    Thread.currentThread().interrupt();
                }
            });
            threads[i].start();
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        List<R> result = new ArrayList<>();
        for (int i = 0; i < args.size(); i++) {
            result.add(null);
        }
        final int[] counter = {0};
        for (int i = 0; i < args.size(); i++) {
            final int current = i;
            synchronized (queue) {
                queue.add(data -> {
                    result.set(current, f.apply(args.get(current)));
                    synchronized (counter) {
                        counter[0]++;
                    }
                });

            }
        }
        synchronized (queue) {
            queue.notifyAll();
            while (counter[0] < args.size()) {
                queue.wait();
            }
        }
        return result;
    }

    @Override
    public void close() throws InterruptedException {
        for (Thread thread : threads) {
            thread.interrupt();
            thread.join();
        }
    }
}
