package ru.ifmo.ctddev.shirvinsky.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Implementation if {@link ru.ifmo.ctddev.shirvinsky.bank.RemotePerson} interface that
 * is operated via rmi as it extends {@link java.rmi.server.UnicastRemoteObject}.
 * Has HashMap which contains pairs of String {@code accountID} and Account,
 * String firstName, String lastName, String personID,
 * static final long serialVersionUID which contains the version of this class
 * and long versionOfPerson which containt the version of current {@code RemotePerson}
 *
 * @see java.rmi.server.UnicastRemoteObject
 * @see ru.ifmo.ctddev.shirvinsky.bank.Person
 * @see ru.ifmo.ctddev.shirvinsky.bank.Bank
 */
public class RemotePersonImpl extends UnicastRemoteObject implements RemotePerson {
    private static final long serialVersionUID = 1L;
    private long versionOfPerson = 1L;
    private String firstName;
    private String lastName;
    private String personID;
    private ConcurrentHashMap<String, Account> personAccounts;

    /**
     * Creates new {@code Remote person}
     *
     * @param firstName First name of person
     * @param lastName  Last name of person
     * @param personID  The passport number of person
     * @throws RemoteException
     */
    RemotePersonImpl(String firstName, String lastName, String personID) throws RemoteException {
        this.firstName = firstName;
        this.lastName = lastName;
        this.personID = personID;
        personAccounts = new ConcurrentHashMap<>();
    }

    public String getFirstName() throws RemoteException {
        return firstName;
    }

    public String getLastName() throws RemoteException {
        return lastName;
    }

    public String getID() throws RemoteException {
        return personID;
    }

    public ConcurrentHashMap<String, Account> getAccounts() throws RemoteException {
        return personAccounts;
    }

    public long getVersionOfPerson() {
        return versionOfPerson;
    }

    private void increaseVersion() {
        versionOfPerson++;
    }

    public int changeAmount(String accountID, int amount) throws RemoteException {
        synchronized (this) {
            int amt = getAmount(accountID) + amount;
            Account account = getAccount(accountID);
            account.setAmount(amt);
            increaseVersion();
            return account.getAmount();
        }
    }

    public boolean updatePerson(LocalPerson person) throws RemoteException {
        synchronized (this) {
            if (versionOfPerson + 1 != person.getVersionOfPerson()) {
                return false;
            }
            versionOfPerson++;
            personAccounts = person.getAccounts();
            return true;
        }
    }

    public int getAmount(String accountID) throws RemoteException {
        Account account = getAccount(accountID);
        return account.getAmount();
    }

    public String showAccountInfo(String accountID) throws RemoteException {
        StringBuilder accountInfo = new StringBuilder();
        accountInfo.append("\nFirst name: ").append(firstName).append("\nLast name: ").append(lastName).append("\nPerson ID: ").append(personID);
        accountInfo.append("\nAmount of money: ").append(getAmount(accountID));
        return accountInfo.toString();
    }

    public String showAccountsInfo() {
        StringBuilder accountsInfo = new StringBuilder();
        accountsInfo.append("\nFirst name: ").append(firstName).append("\nLast name: ").append(lastName).append("\nPerson ID: ").append(personID);
        if (personAccounts.isEmpty()) {
            return accountsInfo.append("\nThis user has no account").toString();
        }
        accountsInfo.append("\nThis user has accounts with ID:");
        for (HashMap.Entry<String, Account> entry : personAccounts.entrySet()) {
            accountsInfo.append("\n").append(entry.getKey());
        }
        return accountsInfo.toString();
    }

    private Account getAccount(String accountID) throws RemoteException {
        createNewAccount(accountID);
        return personAccounts.get(accountID);
    }

    public void createNewAccount(String accountID) throws RemoteException {
        personAccounts.putIfAbsent(accountID, new AccountImpl(accountID));
    }

    public LocalPerson getLocalPerson() throws RemoteException {
        synchronized (this) {
            return new LocalPersonImpl(this);
        }
    }
}