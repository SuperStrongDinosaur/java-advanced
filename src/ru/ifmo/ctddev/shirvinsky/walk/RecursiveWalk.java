package ru.ifmo.ctddev.shirvinsky.walk;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class RecursiveWalk {
    public static class MyFileVisitor extends SimpleFileVisitor<Path> {
        private BufferedWriter out;

        MyFileVisitor(BufferedWriter out) {
            this.out = out;
        }

        public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
            final long FNV_MOD = (1L << 32);
            final long FNV_PRIME = 16777619;
            final int NEEDED_BITS = (1 << 8) - 1;
            final int BUFFER_LENGTH = 4096;
            long hash = 2166136261L;
            InputStream input = new FileInputStream(path.toFile());
            byte[] b = new byte[BUFFER_LENGTH];
            int sz = 0;
            try {
                while ((sz = input.read(b)) >= 0) {
                    for (int i = 0; i < sz; i++) {
                        hash = (hash * FNV_PRIME) % FNV_MOD ^ ((long) b[i] & NEEDED_BITS);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error while calculating hash:" + e.getMessage());
                return FileVisitResult.CONTINUE;
            }
            out.write(String.format("%08x", hash) + " " + path.toString() + System.getProperty("line.separator"));
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException{
            System.err.println("Can not read file " + path.toString());
            out.write(String.format("%08x", 0) + " " + path.toString() + System.getProperty("line.separator"));
            return FileVisitResult.CONTINUE;
        }
    }

    public static void main(String[] args) {
        if (args.length < 2 || args[0] == null || args[1] == null) {
            System.err.println("Not enough input parameters");
            return;
        }
        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);
        try (BufferedReader in = Files.newBufferedReader(input, Charset.forName("UTF-8"));
             BufferedWriter out = Files.newBufferedWriter(output, Charset.forName("UTF-8"))) {
            String name;
            MyFileVisitor visitor = new MyFileVisitor(out);
            while ((name = in.readLine()) != null) {
                Path path = Paths.get(name);
                Files.walkFileTree(path, visitor);
            }
        }
        catch (NoSuchFileException e) {
           System.err.println("Input or output file not found: " + input.toString());
        }
        catch (UnsupportedEncodingException e) {
            System.err.println("Input file charset is not UTF-8.");
        }
        catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
        }
        catch (NullPointerException e) {
            System.err.println("Null pointer exception: " + e.getMessage());
        }
    }
}
