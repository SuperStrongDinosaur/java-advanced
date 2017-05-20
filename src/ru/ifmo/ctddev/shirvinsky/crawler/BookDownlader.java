package ru.ifmo.ctddev.shirvinsky.crawler;

import info.kgeorgiy.java.advanced.crawler.CachingDownloader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by SuperStrongDinosaur on 20.05.17.
 */
public class BookDownlader {

    private static int cntPages = 2;

    static boolean isBook(File entry) {
        return false;
    }

    private static void downloadCategory(String category) {
        String prefix = "https://e.lanbook.com/books/" + category + "?page=";
        String infix = "&category_pk=" + category;
        String Folder = "Books" + File.separator + category + File.separator;
        for (int i = 1; i < cntPages; i++) {
            String page = prefix + Integer.toString(i) + infix;
            try {
                Path path = Paths.get(Folder);
                WebCrawler crawler = new WebCrawler(new CachingDownloader(path), 10, 10, 100);
                //   crawler.downloadBooks(page);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File[] folderEntries = Paths.get(Folder).toFile().listFiles();
        for (File entry : folderEntries) {
            if (isBook(entry)) {
                entry.delete();
            }
        }
    }

    public static void main(String[] args) {
        String math = "917";
        String physics = "918";
        String informatics = "1537";
        downloadCategory(physics);
        //     downloadCategory(math);
        //     downloadCategory(informatics);
    }
}
