package ru.ifmo.ctddev.shirvinsky.bank;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is the basic implementation of {@link ru.ifmo.ctddev.shirvinsky.bank.Bank} interface.
 * Has HashMap which contains pairs of String {@code PersonID} and Person {@code RemotePerson}
 * and final int {@code port} which contains the number of port
 */
public class BankImpl implements Bank {
    private final int port;
    private ConcurrentHashMap<String, RemotePerson> persons = new ConcurrentHashMap<>();

    public BankImpl(final int port) {
        this.port = port;
    }

    public Person createPerson(String firstName, String lastName, String personID, PersonType type) throws RemoteException {
        if (checkPerson(personID)) {
            Person person = persons.get(personID);
            if (!person.getFirstName().equals(firstName) || !person.getLastName().equals(lastName)) {
                throw new RuntimeException("Person ID must be exclusive");
            }
            return null;
        }
        Person person = new RemotePersonImpl(firstName, lastName, personID);
        persons.put(personID, (RemotePerson) person);
        if (type == PersonType.Local) {
            person = createPerson(person, PersonType.Local);
        }
        return person;
    }

    public Person createPerson(String firstName, String lastName, String personID) throws RemoteException {
        return createPerson(firstName, lastName, personID, PersonType.Remote);
    }

    public Person getPerson(String personID, PersonType type) throws RemoteException {
        if (!checkPerson(personID)) {
            return null;
        }
        Person person = persons.get(personID);
        person = createPerson(person, type);
        return person;
    }

    public Person getPerson(String personID) throws RemoteException {
        if (!checkPerson(personID)) {
            return null;
        }
        return persons.get(personID);
    }

    public boolean personUpdate(LocalPerson person) throws RemoteException {
        if (!checkPerson(person.getID())) {
            return false;
        }
        RemotePerson prevPerson = persons.get(person.getID());
        return prevPerson.updatePerson(person);
    }

    private Person createPerson(Person person, PersonType type) throws RemoteException {
        Person personForReturn;
        if (type == PersonType.Local) {
            personForReturn = person.getLocalPerson();
        } else {
            personForReturn = person;
        }
        return personForReturn;
    }

    public boolean checkPerson(String personID) throws RemoteException {
        return persons.containsKey(personID);
    }

    public void deleteInformation() throws RemoteException {
        persons.clear();
    }

    public int countOfPerson() throws RemoteException {
        return persons.size();
    }

}