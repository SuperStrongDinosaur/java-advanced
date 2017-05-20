package ru.ifmo.ctddev.shirvinsky.bank;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interface for operating with person as with bank user.
 */
public interface Person {
    /**
     * Returns person's  first name
     *
     * @return first name of the person
     * @throws RemoteException
     */
    String getFirstName() throws RemoteException;

    /**
     * Returns last person's name (surname)
     *
     * @return last name of the person
     * @throws RemoteException
     */
    String getLastName() throws RemoteException;

    /**
     * Returns person's identity (passport number)
     *
     * @return ID of the person
     * @throws RemoteException
     */
    String getID() throws RemoteException;

    /**
     * Returns HashMap of person'a accounts
     *
     * @return ConcurrentHashMap which contains pairs of String {@code accountID} and Account
     * @throws RemoteException
     */
    ConcurrentHashMap<String, Account> getAccounts() throws RemoteException;

    /**
     * Changes amount of money of specified account according to {@code accountID} and {@code amount}
     *
     * @param accountID The ID of account
     * @param amount    Amount of money which should be added to account
     * @return The final amount of money
     * @throws RemoteException
     */
    int changeAmount(String accountID, int amount) throws RemoteException;

    /**
     * Returns amount of money of specified account according to {@code accountID}
     *
     * @param accountID The ID of account
     * @return The final amount of money
     * @throws RemoteException
     */
    int getAmount(String accountID) throws RemoteException;

    /**
     * Returns the string which contains an account information
     *
     * @param accountID The ID of account
     * @return String which contains the account information
     * @throws RemoteException
     */
    String showAccountInfo(String accountID) throws RemoteException;

    /**
     * Returns the current version of person which is used in {@code changeAmount} operation
     *
     * @return Long number which contains the version
     * @throws RemoteException
     */
    long getVersionOfPerson() throws RemoteException;

    /**
     * Returns the string which contains an accounts information of this person
     *
     * @return String which contains the accounts information
     * @throws RemoteException
     */
    String showAccountsInfo() throws RemoteException;

    /**
     * Creates new account if this accountID wasn't used
     *
     * @param accountID The ID of account to create
     * @throws RemoteException
     */
    void createNewAccount(String accountID) throws RemoteException;

    /**
     * Returns {@code LocalPerson} made from current {@code Person}
     *
     * @return LocalPerson
     * @throws RemoteException
     */
    LocalPerson getLocalPerson() throws RemoteException;
}