package ru.ifmo.ctddev.shirvinsky.bank;

import java.rmi.RemoteException;

/**
 * An implementation of {@link ru.ifmo.ctddev.shirvinsky.bank.Account} interface. Has
 * int variable {@code amount} describing amount of money and final String {@code id} that describes account id.
 */
public class AccountImpl implements Account {
    private final String id;
    private int amount;

    public AccountImpl(String id) throws RemoteException {
        this.id = id;
        amount = 0;
    }

    public String getId() {
        return id;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        //  System.out.println("\nChanging amount of money for account " + id);
        this.amount = amount;
    }
}