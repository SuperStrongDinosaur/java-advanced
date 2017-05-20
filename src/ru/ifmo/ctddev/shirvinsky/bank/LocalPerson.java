package ru.ifmo.ctddev.shirvinsky.bank;

import java.io.Serializable;

/**
 * An interface which extends {@link ru.ifmo.ctddev.shirvinsky.bank.Person} and {@link java.io.Serializable}
 * All the functions of this interface is equal to the functions of {@link ru.ifmo.ctddev.shirvinsky.bank.Person}
 *
 * @see java.io.Serializable
 * @see ru.ifmo.ctddev.shirvinsky.bank.LocalPersonImpl
 */
public interface LocalPerson extends Person, Serializable {
}