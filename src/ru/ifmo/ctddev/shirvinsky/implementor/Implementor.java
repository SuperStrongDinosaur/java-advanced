package ru.ifmo.ctddev.shirvinsky.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.Impler;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by SuperStrongDinosaur on 24.02.17.
 */

public class Implementor implements Impler {
    private class ClassDescriber {
        private Class aClass;
        private StringBuilder builder = new StringBuilder();

        public ClassDescriber(Class aClass) {
            this.aClass = aClass;
        }

        public String emptyImplemented() throws ImplerException {
            if (!aClass.getPackage().getName().equals("")) {
                write("package ", aClass.getPackage().getName(), ";\n\n");
            }
            String simpleName = aClass.getSimpleName() + "Impl";
            write("public class ", simpleName, " ", aClass.isInterface() ? "implements " : "extends ", aClass.getCanonicalName(), " {");

            for (Constructor constructor : aClass.getDeclaredConstructors()) {
                if (!Modifier.isPrivate(constructor.getModifiers())) {
                    printConstructor(constructor, simpleName);
                }
            }

            for (Method method : aClass.getMethods()) {
                if(Modifier.isAbstract(method.getModifiers()))
                printMethod(method);
            }
            write("\n}");
            return builder.toString();
        }

        private void printConstructor(Constructor constructor, String simpleName) {
            write("\npublic ", simpleName, "(");
            printArgs(constructor.getParameterTypes());
            write("){\nsuper(");
            int n = constructor.getParameterTypes().length;
            for (int i = 0; i < n; i++) {
                write("arg", String.valueOf(i));
                if (i != n - 1) {
                    write(", ");
                }
            }
            write(");\n}");
        }

        private void printMethod(Method method) {
            write("\n", Modifier.toString(~Modifier.ABSTRACT & method.getModifiers() & Modifier.methodModifiers()));
            write(" ", method.getReturnType().getCanonicalName(), " ", method.getName(), "(");
            printArgs(method.getParameterTypes());
            write(") {\n");
            Class retClass = method.getReturnType();
            if (!retClass.equals(void.class)) {
                write("return ");
                String retDefault;
                if (retClass.isPrimitive()) {
                    retDefault = retClass.equals(boolean.class) ? "false" : "0";
                } else {
                    retDefault = "null";
                }
                write(retDefault, ";");
            }
            write("\n}");
        }

        private void printArgs(Class[] args) {
            for (int i = 0; i < args.length; i++) {
                write(args[i].getCanonicalName(), " arg", String.valueOf(i));
                if (i != args.length - 1) {
                    write(", ");
                }
            }
        }

        private void write(String... strings) {
            for (String string : strings) {
                builder.append(string);
            }
        }
    }

    @Override
    public void implement(Class<?> aClass, Path root) throws ImplerException {
        boolean bConstr = aClass.isInterface();
        for (Constructor constructor : aClass.getDeclaredConstructors()) {
            if (!Modifier.isPrivate(constructor.getModifiers())) {
                bConstr = true;
            }
        }
        if (!bConstr) {
            throw new ImplerException("No constructors");
        }
        Path path;
        try {
            path = Files.createDirectories(root.resolve(Paths.get(aClass.getPackage().getName().replaceAll("\\.", File.separator) + File.separator)));
        } catch (IOException e) {
            throw new ImplerException();
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path.resolve(aClass.getSimpleName() + "Impl.java"), Charset.defaultCharset())) {
            ClassDescriber classDescriber = new ClassDescriber(aClass);
            writer.write(classDescriber.emptyImplemented());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 2){
            System.out.println("Wrong args");
            return;
        }
        try {
            Class aClass = Class.forName(args[0]);
            Path path = Paths.get(args[1]);
            new Implementor().implement(aClass, path);
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found");
        } catch (ImplerException e) {
            System.out.println(e.getMessage());
        }
    }
}