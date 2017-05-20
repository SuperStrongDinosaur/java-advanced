package ru.ifmo.ctddev.shirvinsky.bank;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation if {@link ru.ifmo.ctddev.shirvinsky.bank.LocalPerson} interface.
 * The instance of {@code LocalPerson} can be delievered to {@code Client} via {@code Serialization}
 * Has HashMap which contains pairs of String {@code accountID} and Account,
 * String firstName, String lastName, String personID,
 * static final long serialVersionUID which contains the version of this class
 * and long versionOfPerson which contains the version of current {@code LocalPerson}
 *
 * @see java.io.Serializable
 * @see ru.ifmo.ctddev.shirvinsky.bank.Person
 * @see ru.ifmo.ctddev.shirvinsky.bank.Bank
 */
public class LocalPersonImpl implements LocalPerson {
    private static final long serialVersionUID = 1L;
    private long versionOfPerson = 1L;
    private String firstName;
    private String lastName;
    private String personID;
    private ConcurrentHashMap<String, Account> personAccounts;

    /**
     * Creates new {@code Local person} using another {@code Person}
     *
     * @param person Person all the fields of which should be copied to the new {@code Local Person}
     * @throws RemoteException if it was catched then all the fields of new class filled with empty space
     */
    LocalPersonImpl(Person person) {
        try {
            firstName = person.getFirstName();
            lastName = person.getLastName();
            personID = person.getID();
            personAccounts = person.getAccounts();
            versionOfPerson = person.getVersionOfPerson();
        } catch (RemoteException e) {
            firstName = "";
            lastName = "";
            personID = "";
            personAccounts = new ConcurrentHashMap<>();
            versionOfPerson = 1L;
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getID() {
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
        int amt = getAmount(accountID);
        amt += amount;
        Account account = getAccount(accountID);
        account.setAmount(amt);
        increaseVersion();
        return account.getAmount();
    }

    public int getAmount(String accountID) throws RemoteException {
        return getAccount(accountID).getAmount();
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
            accountsInfo.append("\nThis user has no account");
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
        return this;
    }

}