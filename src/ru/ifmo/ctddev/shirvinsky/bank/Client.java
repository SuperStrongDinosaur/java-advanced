package ru.ifmo.ctddev.shirvinsky.bank;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * This class represents a client able to connect to the bank server, check user's ID,
 * change user's balance and more.
 *
 * @see ru.ifmo.ctddev.shirvinsky.bank.BankImpl
 */
public class Client {
    private static final String usage = "Usage: java Client <name> <surname> <passport> <accountID> <amount>";

    public static void main(String[] args) throws RemoteException {
        if (args.length != 5 || args[0] == null || args[1] == null ||
                args[2] == null || args[3] == null || args[4] == null) {
            System.out.println(usage);
            return;
        }

        String firstName = args[0];
        String lastName = args[1];
        String personID = args[2];
        String accountID = args[3];
        int changeAmount = Integer.parseInt(args[4]);

        Bank bank;

        try {
            Registry registry = LocateRegistry.getRegistry(null, 8888);
            bank = (Bank) registry.lookup("localhost/bank");
        } catch (NotBoundException e) {
            System.err.println(e.getMessage());
            System.out.println("Bank is not bound");
            return;
        }

        try {
            Person person = bank.createPerson(firstName, lastName, personID);
            if (person == null) {
                person = bank.getPerson(personID);

                if (!person.getFirstName().equals(firstName) || !person.getLastName().equals(lastName)) {
                    System.out.println("Person ID must be exclusive");
                    return;
                }

                System.out.println("This person already exists");

            } else {
                System.out.println("Person was created");
            }

            System.out.println(person.showAccountsInfo());

            System.out.println("\nTrying to create account with ID " + accountID);
            person.createNewAccount(accountID);
            System.out.println(person.showAccountInfo(accountID));
            System.out.println("\nChanging amount of money on account with ID " + accountID);
            person.changeAmount(accountID, changeAmount);
            System.out.println(person.showAccountInfo(accountID));
            System.out.println("\n" + person.getVersionOfPerson());

            System.out.println();

            person = bank.createPerson(firstName, lastName, personID, PersonType.Local);
            if (person == null) {
                person = bank.getPerson(personID, PersonType.Local);
            }
            System.out.println(person.showAccountsInfo());
            person.changeAmount(accountID, changeAmount);
            System.out.println(person.showAccountInfo(accountID));
            System.out.println(person.getVersionOfPerson());

            if (!bank.personUpdate((LocalPerson) person)) {
                System.out.println("Smth went wrong: probably you have made a new person and tried to put it in the bank " +
                        "or you have tried to put in the bank an obsolete version of person");
            }

            System.out.println();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}