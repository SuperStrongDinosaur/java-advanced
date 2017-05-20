package ru.ifmo.ctddev.shirvinsky.bank;

import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * This class starts bank server that's capable of accepting requests
 * and changing user data as specified in {@link ru.ifmo.ctddev.shirvinsky.bank.Bank} interface.
 */
public class Server {
    private final static int PORT = 8888;
    private static Registry registry = null;

    public static void main(String[] args) {
        Bank bank = new BankImpl(PORT);
        try {
            registry = LocateRegistry.createRegistry(PORT);
            System.out.println("Registry was created");
        } catch (RemoteException e) {
            System.out.println("Registry already exists");
        }
        try {
            UnicastRemoteObject.exportObject(bank, PORT);
            registry.rebind("localhost/bank", bank);
        } catch (AccessException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
        } catch (RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Server started");
    }
}