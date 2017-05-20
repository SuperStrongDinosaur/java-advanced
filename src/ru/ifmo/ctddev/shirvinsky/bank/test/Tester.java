package ru.ifmo.ctddev.shirvinsky.bank.test;

import info.kgeorgiy.java.advanced.base.BaseTester;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class Tester extends BaseTester {
    public static void main(final String... args) throws NoSuchAlgorithmException, IOException {
        new Tester()
                .add("test", BankTest.class)
                .run(args);
    }
}
