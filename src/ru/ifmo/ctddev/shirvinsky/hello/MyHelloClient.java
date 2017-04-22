package ru.ifmo.ctddev.shirvinsky.hello;

/**
 * Created by SuperStrongDinosaur on 14.04.17.
 */

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sends request to the server, accepts the results and prints them.
 */
public class MyHelloClient implements HelloClient {
    private static final int TIMEOUT = 100;

    /**
     * Entry point for {@link MyHelloClient}.
     */
    public static void main(String[] args) {
        MyHelloClient client = new MyHelloClient();
        client.start(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
    }

    /**
     * Starts client.
     *
     * @param host     name or ip-address computer where server is run.
     * @param port     port to send requests to.
     * @param prefix   prefix for the requests.
     * @param requests number of requests in each thread.
     * @param nThreads number of threads.
     */
    @Override
    public void start(String host, int port, String prefix, int requests, int nThreads) {
        ExecutorService service = Executors.newFixedThreadPool(nThreads);
        try {
            InetAddress address = InetAddress.getByName(host);
            List<Callable<Object>> list = new ArrayList<>();
            for (int i = 0; i < nThreads; i++) {
                final int threadNumber = i;
                list.add(() -> {
                    DatagramSocket socket = new DatagramSocket();
                    socket.setSoTimeout(TIMEOUT);

                    byte[] buffer = new byte[socket.getReceiveBufferSize()];
                    for (int queryNumber = 0; queryNumber < requests; queryNumber++) {
                        String s = prefix + threadNumber + "_" + queryNumber;
                        byte[] sending_data = s.getBytes("UTF8");
                        DatagramPacket sending = new DatagramPacket(sending_data, sending_data.length, address, port);
                        DatagramPacket received = new DatagramPacket(buffer, buffer.length);
                        String req = "Hello, " + s, res = "";
                        while (!req.equals(res)) {
                            try {
                                socket.send(sending);
                                try {
                                    socket.receive(received);
                                    res = new String(received.getData(), received.getOffset(), received.getLength());
                                } catch (IOException e) {
                                    System.err.println("Error to receive packet: " + e.getMessage());
                                }
                            } catch (IOException e) {
                                System.err.println("Error to send packet: " + e.getMessage());
                            }
                        }
                    }
                    socket.close();
                    return null;
                });
            }
            service.invokeAll(list);
        } catch (InterruptedException e) {
            System.err.println("Worker is interrupted: " + e.getMessage());
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + e.getMessage());
        } finally {
            service.shutdown();
        }
    }
}
