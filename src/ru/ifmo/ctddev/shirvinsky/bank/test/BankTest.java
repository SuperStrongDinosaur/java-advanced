package ru.ifmo.ctddev.shirvinsky.bank.test;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ru.ifmo.ctddev.shirvinsky.bank.*;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class BankTest {

    private static final int PORT = 8888;
    private static Registry registry = null;
    private static Bank bank;

    private static String firstName = "John";
    private static String lastName = "Doe";
    private static String personID = "1_";
    private static String accountID = "1_";
    private static int amount = 100;

    /**
     * Starts bank server that's capable of accepting requests
     * and changing user data as specified in {@link ru.ifmo.ctddev.shirvinsky.bank.Bank} interface.
     */
    @BeforeClass
    public static void serverStarted() {
        BankTest.bank = new BankImpl(PORT);
        try {
            registry = LocateRegistry.createRegistry(PORT);
            System.out.println("Registry was created");
        } catch (RemoteException e) {
            System.out.println("Registry already exists");
        }
        try {
            UnicastRemoteObject.exportObject(bank, PORT);
            registry.rebind("localhost/bank", bank);
        } catch (RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Server started");
    }


    @Before
    public void runningTest() throws RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry(null, 8888);
            bank = (Bank) registry.lookup("localhost/bank");
        } catch (NotBoundException e) {
            System.err.println(e.getMessage());
            System.out.println("Bank is not bound");
            //return;
        }
        bank.deleteInformation();
    }

    /**
     * Creates single person
     *
     * @throws RemoteException
     */
    @Test
    public void test01_createSinglePerson() throws RemoteException {
        System.err.println("=== Running test01_createSinglePerson");
        bank.createPerson(firstName, lastName, personID);
        assertEquals("Wrong count of persons: expected 1, found " + bank.countOfPerson(), 1, bank.countOfPerson());
        assertTrue("Can't found this person: " + personID, bank.checkPerson(personID));
    }

    /**
     * Creates twice same person. The second invocation of {@code bank.createPerson} should return null.
     *
     * @throws RemoteException
     */
    @Test
    public void test02_createTwiceSamePerson() throws RemoteException {
        System.err.println("=== Running test02_createTwiceSamePerson");
        bank.createPerson(firstName, lastName, personID);
        bank.createPerson(firstName, lastName, personID);
        assertEquals("Wrong count of persons: expected 1, found " + bank.countOfPerson(), 1, bank.countOfPerson());
        assertTrue("Can't found this person: " + personID, bank.checkPerson(personID));
    }

    /**
     * Creates persons with same ID and different names. The second invocation of {@code bank.createPerson}
     * should throw {@code IllegalPersonException}.
     *
     * @throws RemoteException
     */
    @Test(expected = RuntimeException.class)
    public void test03_createTwiceWithFailures() throws RemoteException {
        System.err.println("=== Running test03_createTwiceWithFailures");
        bank.createPerson(firstName, lastName, personID);
        bank.createPerson(firstName + "1", lastName, personID);
        assertEquals("Wrong count of persons: expected 1, found " + bank.countOfPerson(), 1, bank.countOfPerson());
        assertTrue("Can't found this person: " + personID, bank.checkPerson(personID));
    }

    /**
     * Creates 100 different persons
     *
     * @throws RemoteException
     */
    @Test
    public void test04_createMultithreaded() throws RemoteException {
        System.err.println("=== Running test04_createMultithreaded");
        ExecutorService service = Executors.newFixedThreadPool(100);
        for (int thread = 0; thread < 100; thread++) {
            final int threadID = thread;
            service.execute(() -> {
                try {
                    bank.createPerson(firstName, lastName, personID + threadID);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
        }
        service.shutdownNow();
        assertEquals("Wrong count of persons: expected 100, found " + bank.countOfPerson(), 100, bank.countOfPerson());
        for (int i = 0; i < 100; i++) {
            assertTrue("Can't found this person: " + personID + i, bank.checkPerson(personID + i));
        }
    }

    /**
     * Creates single person with 100 accounts
     *
     * @throws RemoteException
     */
    @Test
    public void test05_createAccounts() throws RemoteException {
        System.err.println("=== Running test05_createAccounts");
        ExecutorService service = Executors.newFixedThreadPool(100);
        Person person = bank.createPerson(firstName, lastName, personID);
        assertNotNull("This person creates for the first time, but it equals null", person);
        for (int thread = 0; thread < 100; thread++) {
            final int threadID = thread;
            service.execute(() -> {
                try {
                    person.createNewAccount(accountID + threadID);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
        }
        service.shutdownNow();
        ConcurrentHashMap<String, Account> accounts = person.getAccounts();
        assertEquals("Wrong count of accounts: expected 100, found " + accounts.size(), 100, accounts.size());
        for (int i = 0; i < 100; i++) {
            assertTrue("Can't found this account: " + accountID + i, accounts.containsKey(accountID + i));
        }
    }

    /**
     * Creates single person with single account and change amount 100 times
     *
     * @throws RemoteException
     */
    @Test
    public void test06_createAccountWithChangingAmount() throws RemoteException {
        System.err.println("=== Running test06_createAccountWithChangingAmount");
        ExecutorService service = Executors.newFixedThreadPool(100);
        Person person = bank.createPerson(firstName, lastName, personID);
        assertNotNull("This person creates for the first time, but it equals null", person);
        person.createNewAccount(accountID);
        for (int thread = 0; thread < 100; thread++) {
            service.execute(() -> {
                try {
                    person.changeAmount(accountID, amount);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });
        }
        service.shutdownNow();
        assertEquals("Wrong amount: expected 10000, found " + person.getAmount(accountID), 10000, person.getAmount(accountID));
    }

    /**
     * Creates single local person and update information in bank
     *
     * @throws RemoteException
     */
    @Test
    public void test07_createSingleLocalPerson() throws RemoteException {
        System.err.println("=== Running test07_createSingleLocalPerson");
        Person person = bank.createPerson(firstName, lastName, personID, PersonType.Local);
        assertNotNull("This person creates for the first time, but it equals null", person);
        person.changeAmount(accountID, amount);
        bank.personUpdate((LocalPerson) person);

        person = bank.getPerson(personID);
        assertEquals("Wrong version of person: expected 2, found " + person.getVersionOfPerson(), 2, person.getVersionOfPerson());
    }

    /**
     * Creates single local person, changes amount twice and update information in bank.
     * The information in bank shouldn't be updated
     *
     * @throws RemoteException
     */
    @Test
    public void test08_createSingleLocalPersonWithFailures() throws RemoteException {
        System.err.println("=== Running test08_createSingleLocalPersonWithFailures");
        Person person = bank.createPerson(firstName, lastName, personID, PersonType.Local);
        assertNotNull("This person creates for the first time, but it equals null", person);
        person.changeAmount(accountID, amount);
        person.changeAmount(accountID, amount);
        assertFalse("Version should be obsolete", bank.personUpdate((LocalPerson) person));

        person = bank.getPerson(personID);
        assertEquals(person, bank.getPerson(personID, PersonType.Remote));
        assertEquals("Wrong version of person: expected 1, found " + person.getVersionOfPerson(), 1, person.getVersionOfPerson());
    }

    /**
     * Creates single local person, gets remote person with personID,
     * changes amount using serializable method, updates, then changes amount using remote method
     *
     * @throws RemoteException
     */
    @Test
    public void test09_createLocalAndRemote() throws RemoteException {
        System.err.println("=== Running test09_createLocalAndRemote");

        Person person = bank.createPerson(firstName, lastName, personID, PersonType.Local);
        assertNotNull("This person creates for the first time, but it equals null", person);
        Person remPerson = bank.getPerson(personID);
        assertEquals(person, person.getLocalPerson());
        assertEquals(firstName, person.getFirstName());
        assertEquals(lastName, person.getLastName());

        person.changeAmount(accountID, amount);
        assertTrue("This operation shouldn't fails", bank.personUpdate((LocalPerson) person));

        remPerson.changeAmount(accountID, amount);
        assertEquals("Version should be equals 3, found " + remPerson.getVersionOfPerson(), 3, remPerson.getVersionOfPerson());
        person.changeAmount(accountID, amount);
        assertFalse("Version should be obsolete", bank.personUpdate((LocalPerson) person));
    }
}
