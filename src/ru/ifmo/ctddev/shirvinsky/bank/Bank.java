package ru.ifmo.ctddev.shirvinsky.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface provides basic methods to implement bank system. The bank contains persons,
 * each person can have any number of accounts. Interface allows to add persons.
 * It also implements {@link java.rmi.Remote} interface
 */
public interface Bank extends Remote {
    /**
     * Creates new {@code Remote person} if the type of person isn't important
     *
     * @param firstName First name of person
     * @param lastName  Last name of person
     * @param personID  The passport number of person
     * @return Stub of remote person,
     * @throws RemoteException
     * @see java.rmi.Remote
     * @see ru.ifmo.ctddev.shirvinsky.bank.RemotePerson
     */
    Person createPerson(String firstName, String lastName, String personID) throws RemoteException;

    /**
     * Creates new Local or Remote person according to {@code type}.
     * Pushes new RemotePerson with {@code firstName}, {@code lastName}, {@code personID} data
     *
     * @param firstName First name of person
     * @param lastName  Last name of person
     * @param personID  The passport number of person
     * @param type      Type of returning person
     * @return Local or Remote person according to {@code type}
     * @throws RemoteException
     */
    Person createPerson(String firstName, String lastName, String personID, PersonType type) throws RemoteException;

    /**
     * Returns to user a person of specified type according to {@code type}
     *
     * @param personID The passport number of person to return
     * @param type     Type of returning person
     * @return The person with specified type
     * @throws RemoteException
     * @see ru.ifmo.ctddev.shirvinsky.bank.Person
     */
    Person getPerson(String personID, PersonType type) throws RemoteException;

    /**
     * Returns to user a Remote person
     *
     * @param personID The passport number of person to return
     * @return The remote person
     * @throws RemoteException
     * @see ru.ifmo.ctddev.shirvinsky.bank.RemotePerson
     */
    Person getPerson(String personID) throws RemoteException;

    /**
     * Updates on {@code Server} the person which was provided to {@code Client} by serialization.
     * This function should be called every time the change occured
     *
     * @param person Local Person to update
     * @return True if there was no problems during the operation of update
     * or false if this user has wrong PersonID or the version of {@code Person} is obsolete
     * @throws RemoteException
     */
    boolean personUpdate(LocalPerson person) throws RemoteException;

    /**
     * Function for test.
     * Clears HashMap {@code persons}
     *
     * @throws RemoteException
     */
    void deleteInformation() throws RemoteException;

    /**
     * Function for test
     * Returns count of person contained in bank
     *
     * @throws RemoteException
     */
    int countOfPerson() throws RemoteException;

    /**
     * Checks whether the person contains or not
     *
     * @throws RemoteException
     */
    boolean checkPerson(String personID) throws RemoteException;
}