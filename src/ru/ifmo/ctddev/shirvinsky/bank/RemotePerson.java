package ru.ifmo.ctddev.shirvinsky.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * An interface which extends {@link ru.ifmo.ctddev.shirvinsky.bank.Person} and {@link java.rmi.Remote}
 * All the functions of this interface is equal to the functions of {@link ru.ifmo.ctddev.shirvinsky.bank.Person}
 *
 * @see java.rmi.Remote
 * @see ru.ifmo.ctddev.shirvinsky.bank.RemotePersonImpl
 */
public interface RemotePerson extends Person, Remote {
    /**
     * Update information in bank
     *
     * @param person Person which contains info for update
     * @return True if update was successfully completed, false if verion of person is obsolete
     * @throws RemoteException
     */
    boolean updatePerson(LocalPerson person) throws RemoteException;
}