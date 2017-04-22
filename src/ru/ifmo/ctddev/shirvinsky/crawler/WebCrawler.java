package ru.ifmo.ctddev.shirvinsky.crawler;
/**
 * Created by SuperStrongDinosaur on 30.03.17.
 */

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.stream.Collectors;

/**
 * Class that downloads websites recursively in parallel with the specified depth and returns all the files and links
 * that are downloaded.
 * <p/>
 * User needs to provide {@link Downloader}
 *
 * @see info.kgeorgiy.java.advanced.crawler.Crawler
 */
public class WebCrawler implements Crawler {
    private final int perHostLimit;
    private final Map<String, HostManager> hostManagerMap;
    Phaser phaser;
    private Downloader downloader;
    private ExecutorService downloadService, extractService;
    private Map<String, Object> result;
    private Map<String, IOException> errors;

    /**
     * Creates instance of class with specified parameters.
     *
     * @param d           instance of class that implement {@link Downloader}
     * @param downloaders maximum number of simultaneous downloading
     * @param extractors  maximum number of simultaneous extracting links
     * @param perHost     maximum number of downloaders for host
     */
    public WebCrawler(Downloader d, int downloaders, int extractors, int perHost) {
        perHostLimit = perHost;
        downloader = d;
        downloadService = Executors.newFixedThreadPool(downloaders);
        extractService = Executors.newFixedThreadPool(extractors);
        hostManagerMap = new ConcurrentHashMap<>();
    }

    /**
     * Main method to execute, walks websites according depth
     *
     * @param args {url, depth, downloaders max, exctractors max, perhosts max}
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0 || args.length > 4) {
            System.err.println("invalid input");
            return;
        }
        String url = args[0];
        int downloaders = (args.length > 1) ? Integer.parseInt(args[1]) : 10;
        int extractors = (args.length > 2) ? Integer.parseInt(args[2]) : 10;
        int perHost = (args.length > 3) ? Integer.parseInt(args[3]) : Integer.MAX_VALUE;
        Path path = Paths.get("Downloads" + File.separator);
        try (WebCrawler crawler = new WebCrawler(new CachingDownloader(path), downloaders, extractors, perHost)) {
            crawler.download(url, 4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void extractProduce(String url, int depth) {
        try {
            final Document document = downloader.download(url);
            if (depth == 1) {
                return;
            }
            phaser.register();
            extractService.submit(() -> {
                try {
                    document.extractLinks().forEach(link -> downloadProduce(link, depth - 1));
                } catch (IOException e) {
                    errors.put(url, e);
                } finally {
                    phaser.arrive();
                }
            });
        } catch (IOException e) {
            errors.put(url, e);
        }
    }

    private void downloadProduce(String url, int depth) {
        if (depth > 0 && result.putIfAbsent(url, new Object()) == null) {
            try {
                String host = URLUtils.getHost(url);
                phaser.register();
                hostManagerMap.putIfAbsent(host, new HostManager(perHostLimit));
                hostManagerMap.get(host).add(() -> {
                    try {
                        extractProduce(url, depth);
                    } finally {
                        hostManagerMap.get(host).remove();
                        phaser.arrive();
                    }
                });
            } catch (MalformedURLException e) {
                errors.put(url, e);
            }
        }
    }

    /**
     * Download pages starting with the specified url.
     *
     * @param url   url to start
     * @param depth maximum depth to go to
     * @return list of visited websites or errors
     */
    @Override
    public Result download(String url, int depth) {
        result = new ConcurrentHashMap<>();
        errors = new ConcurrentHashMap<>();
        phaser = new Phaser(1);

        downloadProduce(url, depth);
        phaser.arriveAndAwaitAdvance();

        result.keySet().removeAll(errors.keySet());
        return new Result(result.keySet().stream().collect(Collectors.toList()), errors);
    }

    /**
     * Closes class, stops all threads
     */
    @Override
    public void close() {
        downloadService.shutdown();
        extractService.shutdown();
    }

    private class HostManager {
        int count;
        int hostLimit;
        Queue<Runnable> tasks;

        HostManager(int Limit) {
            tasks = new ArrayDeque<>();
            count = 0;
            hostLimit = Limit;
        }

        synchronized void add(Runnable task) {
            if (count < hostLimit) {
                downloadService.submit(task);
                count++;
            } else {
                tasks.add(task);
            }
        }

        synchronized void remove() {
            if (!tasks.isEmpty()) {
                downloadService.submit(tasks.poll());
            } else {
                count--;
            }
        }
    }
}