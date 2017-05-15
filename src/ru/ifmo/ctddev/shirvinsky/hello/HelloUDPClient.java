package ru.ifmo.ctddev.shirvinsky.hello;

/**
 * Created by SuperStrongDinosaur on 14.04.17.
 */

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.concurrent.CountDownLatch;

/**
 * Sends request to the server, accepts the results and prints them.
 */
public class HelloUDPClient implements HelloClient {
    private static final int TIMEOUT = 1000;
    private CountDownLatch countDown;

    /**
     * Entry point for {@link HelloUDPClient}.
     */
    public static void main(String[] args) {
        HelloUDPClient client = new HelloUDPClient();
        client.run(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
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
    public void run(String host, int port, String prefix, int requests, int nThreads) {
        countDown = new CountDownLatch(nThreads);
        try {
            InetAddress address = InetAddress.getByName(host);
            for (int i = 0; i < nThreads; i++) {
                final int threadNumber = i;
                Thread a = new Thread(() -> {
                    DatagramSocket socket = null;
                    byte[] buffer = new byte[0];
                    try {
                        socket = new DatagramSocket();
                        socket.setSoTimeout(TIMEOUT);
                        buffer = new byte[socket.getReceiveBufferSize()];
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }
                    for (int queryNumber = 0; queryNumber < requests; queryNumber++) {
                        String s = prefix + threadNumber + "_" + queryNumber;
                        byte[] sending_data = new byte[0];
                        try {
                            sending_data = s.getBytes("UTF8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        DatagramPacket sending = new DatagramPacket(sending_data, sending_data.length, address, port);
                        DatagramPacket received = new DatagramPacket(buffer, buffer.length);
                        String res = "";
                        while (!res.contains(s)) {
                            try {
                                socket.send(sending);
                                try {
                                    socket.receive(received);
                                    res = new String(received.getData(), received.getOffset(), received.getLength());
                                } catch (IOException e) {
                                    //    System.err.println("Error to receive packet: " + e.getMessage());
                                }
                            } catch (IOException e) {
                                System.err.println("Error to send packet: " + e.getMessage());
                            }
                        }
                    }
                    socket.close();
                    countDown.countDown();
                });
                a.start();
            }
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + e.getMessage());
        }
        try {
            countDown.await();
        } catch (InterruptedException e) {
            System.out.println("Await interrupted");
        }
    }
}
