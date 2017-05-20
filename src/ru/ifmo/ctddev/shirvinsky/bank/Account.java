package ru.ifmo.ctddev.shirvinsky.bank;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface, that describes the person account. Implement {@link java.rmi.Remote},
 * so can be used via rmi, and {@link java.io.Serializable}.
 */
public interface Account extends Remote, Serializable {
    /**
     * Returns ID of account
     *
     * @return String ID of account
     * @throws RemoteException
     */
    String getId() throws RemoteException;

    /**
     * Returns the current balance
     *
     * @return The balance
     * @throws RemoteException
     */
    int getAmount() throws RemoteException;

    /**
     * Sets amount of money to the current balance
     *
     * @param amount Amount of money that should be set
     * @throws RemoteException
     */
    void setAmount(int amount) throws RemoteException;
}