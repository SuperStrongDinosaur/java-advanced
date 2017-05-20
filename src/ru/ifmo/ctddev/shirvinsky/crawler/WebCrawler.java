package ru.ifmo.ctddev.shirvinsky.crawler;
/**
 * Created by SuperStrongDinosaur on 30.03.17.
 */

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class that downloads websites recursively in parallel with the specified depth and returns all the files and links
 * that are downloaded.
 * <p/>
 * User needs to provide {@link Downloader}
 *
 * @see info.kgeorgiy.java.advanced.crawler.Crawler
 */
//public class WebCrawler implements Crawler {
//    private final int perHostLimit;
//    private final Map<String, HostManager> hostManagerMap;
//    Phaser phaser;
//    private Downloader downloader;
//    private ExecutorService downloadService, extractService;
//    private Map<String, Object> result;
//    private Map<String, IOException> errors;
//
//    /**
//     * Creates instance of class with specified parameters.
//     *
//     * @param d           instance of class that implement {@link Downloader}
//     * @param downloaders maximum number of simultaneous downloading
//     * @param extractors  maximum number of simultaneous extracting links
//     * @param perHost     maximum number of downloaders for host
//     */
//    public WebCrawler(Downloader d, int downloaders, int extractors, int perHost) {
//        perHostLimit = perHost;
//        downloader = d;
//        downloadService = Executors.newFixedThreadPool(downloaders);
//        extractService = Executors.newFixedThreadPool(extractors);
//        hostManagerMap = new ConcurrentHashMap<>();
//    }
//
//    /**
//     * Main method to execute, walks websites according depth
//     *
//     * @param args {url, depth, downloaders max, exctractors max, perhosts max}
//     */
//    public static void main(String[] args) {
//        if (args == null || args.length == 0 || args.length > 4) {
//            System.err.println("invalid input");
//            return;
//        }
//        String url = args[0];
//        int downloaders = (args.length > 1) ? Integer.parseInt(args[1]) : 10;
//        int extractors = (args.length > 2) ? Integer.parseInt(args[2]) : 10;
//        int perHost = (args.length > 3) ? Integer.parseInt(args[3]) : Integer.MAX_VALUE;
//        Path path = Paths.get("Downloads" + File.separator);
//        try (WebCrawler crawler = new WebCrawler(new CachingDownloader(path), downloaders, extractors, perHost)) {
//            crawler.download(url, 4);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void downloadBooks(String page) {
//        downloadService.submit(() -> {
//            try {
//                final Document document = downloader.download(page);
//                extractService.submit(() -> {
//                    try {
//                        document.extractLinks().forEach(link -> {
//                            System.out.println(link);
//                            if (link.contains("#book_name")) {
//                                downloadService.submit(() -> {
//                                try {
//                                    final Document bookPage = downloader.download(link);
//                                    bookPage.extractLinks().forEach(link1 -> {
//                                        if (link1.contains("")) { //TODO add book ident
//                                            try {
//                                                final Document book = downloader.download(link1);
//                                            } catch (IOException e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//                                    });
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            });
//
//                            }
//                        });
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                });
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//    }
//
//    private void extractProduce(String url, int depth) {
//        try {
//            final Document document = downloader.download(url);
//            if (depth == 1) {
//                return;
//            }
//            phaser.register();
//            extractService.submit(() -> {
//                try {
//                    document.extractLinks().forEach(link -> downloadProduce(link, depth - 1));
//                } catch (IOException e) {
//                    errors.put(url, e);
//                } finally {
//                    phaser.arrive();
//                }
//            });
//        } catch (IOException e) {
//            errors.put(url, e);
//        }
//    }
//
//    private void downloadProduce(String url, int depth) {
//        if (depth > 0 && result.putIfAbsent(url, new Object()) == null) {
//            try {
//                String host = URLUtils.getHost(url);
//                phaser.register();
//                hostManagerMap.putIfAbsent(host, new HostManager(perHostLimit));
//                hostManagerMap.get(host).add(() -> {
//                    try {
//                        extractProduce(url, depth);
//                    } finally {
//                        hostManagerMap.get(host).remove();
//                        phaser.arrive();
//                    }
//                });
//            } catch (MalformedURLException e) {
//                errors.put(url, e);
//            }
//        }
//    }
//
//    /**
//     * Download pages starting with the specified url.
//     *
//     * @param url   url to start
//     * @param depth maximum depth to go to
//     * @return list of visited websites or errors
//     */
//    @Override
//    public Result download(String url, int depth) {
//        result = new ConcurrentHashMap<>();
//        errors = new ConcurrentHashMap<>();
//        phaser = new Phaser(1);
//
//        downloadProduce(url, depth);
//        phaser.arriveAndAwaitAdvance();
//
//        result.keySet().removeAll(errors.keySet());
//        return new Result(result.keySet().stream().collect(Collectors.toList()), errors);
//    }
//
//    /**
//     * Closes class, stops all threads
//     */
//    @Override
//    public void close() {
//        downloadService.shutdown();
//        extractService.shutdown();
//    }
//
//
//
//    private class HostManager {
//        int count;
//        int hostLimit;
//        Queue<Runnable> tasks;
//
//        HostManager(int Limit) {
//            tasks = new ArrayDeque<>();
//            count = 0;
//            hostLimit = Limit;
//        }
//
//        synchronized void add(Runnable task) {
//            if (count < hostLimit) {
//                downloadService.submit(task);
//                count++;
//            } else {
//                tasks.add(task);
//            }
//        }
//
//        synchronized void remove() {
//            if (!tasks.isEmpty()) {
//                downloadService.submit(tasks.poll());
//            } else {
//                count--;
//            }
//        }
//    }
//}

public class WebCrawler implements Crawler {

    private final Downloader downloader;
    private final ExecutorService downloaders, extractors;
    private final Map<String, HostManager> hostManagerMap;

    private int hostLimit;

    /**
     * Creates new instance of web crawler.
     *
     * @param downloader  {@link Downloader} to use.
     * @param downloaders Amount of threads for downloading.
     * @param extractors  Amount of threads to extracting.
     * @param perHost     Maximum amount of parallel downloads for one host.
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {

        this.downloader = downloader;
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
        hostManagerMap = new ConcurrentHashMap<>();

        hostLimit = perHost;
    }

    /**
     * Entering point of program during launching crawler from the console.
     * <p>
     * Using {@link CachingDownloader} as downloader.
     * <p>
     * Default depth is 2.
     * <p>
     * Default values for amount of threads is {@link Runtime#availableProcessors()}
     * </p>
     *
     * @param args start url, amount of downloads, amount of extractors, maximum amount per host or default value.
     */
    public static void main(String[] args) {
        try {
            String s = args[0];
            int p = Runtime.getRuntime().availableProcessors();
            List<Integer> intArgs = new ArrayList<>(Collections.nCopies(3,
                    p));
            for (int i = 2; i < args.length; i++) {
                intArgs.set(i - 2, Integer.parseInt(args[i]));
            }
            try (WebCrawler webCrawler = new WebCrawler(
                    new CachingDownloader(), intArgs.get(0), intArgs.get(1), intArgs.get(2))) {
                Result r = webCrawler.download(s, 2);
                System.out.println("downloaded" + r.getDownloaded().size() + " errors" + r.getErrors().size());
            } catch (IOException e) {
                System.out.println("Error during creating an instance of CachingDownloader");
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Missing starting link");
        } catch (NullPointerException e) {
            System.out.println("Invalid arguments, not expected null");
        }
    }

    /**
     * Crawls the given site parallel.
     *
     * @param url   Start Url
     * @param depth Depth of crawl.
     *              If 1 - download only given url.
     *              If more than one - recursive downloads urls of the downloaded page.
     * @return {@link Result} consisted of list of loaded urls and errors.
     */
    @Override
    public Result download(String url, int depth) {

        Set<String> result = ConcurrentHashMap.newKeySet(); /*new ConcurrentSkipListSet()*/
        Map<String, IOException> errors = new ConcurrentHashMap<>();

        Phaser p = new Phaser(1);
        download(url, depth, result, errors, p);
        p.awaitAdvance(p.arrive());

        result.removeAll(errors.keySet());
        return new Result(new ArrayList<>(result), errors);
    }


    private void download(String url, int depth, final Set<String> result, final Map<String, IOException> errors, Phaser p) {
        try {
            if (depth > 0 && result.add(url)) {
                String host = URLUtils.getHost(url);
                p.register();
                checkAndSubmit(host, () -> {
                    try {
                        downloadHelper(url, depth, result, errors, p);
                    } catch (IOException e) {
                        errors.put(url, e);
                    } finally {
                        resolveHost(host);
                        p.arrive();
                    }
                });
            }

        } catch (MalformedURLException e) {
            errors.put(url, e);
        }
    }

    private void checkAndSubmit(String host, Runnable task) {
        hostManagerMap.putIfAbsent(host, new HostManager());
        HostManager hostL = hostManagerMap.get(host);
        hostL.lock();
        try {
            if (hostL.count < hostLimit) {
                downloaders.submit(task);
                hostL.count++;
            } else {
                hostL.tasks.add(task);
            }
        } finally {
            hostL.unlock();
        }
    }

    private void resolveHost(String host) {
        HostManager hostL = hostManagerMap.get(host);
        hostL.lock();
        try {
            if (!hostL.tasks.isEmpty()) {
                downloaders.submit(hostL.tasks.poll());

            } else {
                hostL.count--;
            }
        } finally {
            hostL.unlock();
        }
    }

    private void downloadHelper(String s, int i, final Set<String> result, final Map<String, IOException> errors, Phaser p)
            throws IOException {

        final Document doc = downloader.download(s);
        if (i == 1) {
            return;
        }
        p.register();
        extractors.submit(() -> {
            try {
                doc.extractLinks().forEach(x -> download(x, i - 1, result, errors, p));
            } catch (IOException e) {
                errors.put(s, e);
            } finally {
                p.arrive();
            }
        });
    }


    /**
     * Shutdowns all helper threads.
     */
    @Override
    public void close() {
        extractors.shutdown();
        downloaders.shutdown();
    }

    private class HostManager {
        int count;
        Queue<Runnable> tasks;
        ReentrantLock lock;


        HostManager() {
            this.tasks = new ArrayDeque<>();
            this.count = 0;
            lock = new ReentrantLock();
        }

        void lock() {
            lock.lock();
        }

        void unlock() {
            lock.unlock();
        }

    }
}