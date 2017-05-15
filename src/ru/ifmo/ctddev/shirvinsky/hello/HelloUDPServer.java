package ru.ifmo.ctddev.shirvinsky.hello;

/**
 * Created by SuperStrongDinosaur on 14.04.17.
 */

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sends tasks, sent by {@link HelloUDPClient} and answers them.
 */
public class HelloUDPServer implements HelloServer {
    private static final int TIMEOUT = 1000;
    private ExecutorService service;
    private DatagramSocket socket;

    /**
     * Entry point for {@link HelloUDPServer}.
     */
    public static void main(String[] args) {
        HelloUDPServer server = new HelloUDPServer();
        server.start(8004, 3);
    }

    /**
     * Starts server.
     *
     * @param port    port where from sends requests
     * @param threads number of threads.
     */
    @Override
    public void start(int port, int threads) {
        service = Executors.newFixedThreadPool(threads);
        try {
            socket = new DatagramSocket(port);
            int bufferSize = socket.getReceiveBufferSize();
            socket.setSoTimeout(TIMEOUT);
            for (int i = 0; i < threads; i++) {
                service.submit(() -> {
                    while (!Thread.interrupted()) {
                        DatagramPacket received = new DatagramPacket(new byte[bufferSize], bufferSize);
                        try {
                            socket.receive(received);
                            String str = new String(received.getData(), 0, received.getLength(), Charset.forName("UTF8"));
                            str = "Hello, " + str;
                            try {
                                socket.send(new DatagramPacket(str.getBytes(), str.getBytes().length, received.getAddress(), received.getPort()));
                            } catch (IOException e) {
                                System.err.println("Unable to send package: " + e.getMessage());
                            }
                        } catch (IOException e) {
                            System.err.println("Unable to receive packet: " + e.getMessage());
                        }
                    }
                });
            }
        } catch (SocketException e) {
            System.err.println("Unable to create socket: " + e.getMessage());
        } finally {
            service.shutdown();
        }
    }

    /**
     * Terminate server.
     */
    @Override
    public void close() {
        socket.close();
        service.shutdownNow();
    }
}

