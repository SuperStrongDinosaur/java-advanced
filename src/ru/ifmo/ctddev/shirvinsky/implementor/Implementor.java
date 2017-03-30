package ru.ifmo.ctddev.shirvinsky.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * Created by SuperStrongDinosaur on 24.02.17.
 */

public class Implementor implements JarImpler {
    /**
     * main method
     * @param args class canonical name, path to jar
     */
    public static void main(String[] args) {
        if (args.length < 2) {
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

    /**
     * Makes own class, compiles that file and zips it to jar file in directory
     * @param token   type token to create implementation for.
     * @param jarFile target <tt>.jar</tt> file.
     * @throws ImplerException if inheriting impossible
     *                         javac was not found
     *                         javac exit code != 0
     *                         or resulting classfile was not found
     *                         IOException occurred
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        implement(token, Paths.get("./"));
        Path sourceFile = Paths.get(token.getPackage().getName().replace(".", File.separator) + File.separator + token.getSimpleName() + "Impl.java");

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Couldn't find java compiler");
        }
        if (compiler.run(null, null, null, "-encoding", "Cp1251", sourceFile.toString()) != 0) {
            throw new ImplerException("Error while compiling");
        }
        Path classFile = Paths.get(sourceFile.toString().replace(".java", ".class"));

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (JarOutputStream target = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            target.putNextEntry(new ZipEntry(classFile.toString()));
            Files.copy(classFile, target);
            target.close();
            Files.deleteIfExists(sourceFile);
            Files.deleteIfExists(classFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a file in specified folder that implements or extends interface or class
     *
     * @param aClass class or interface that will be implemented
     * @param root   directory where to create implementation
     * @throws ImplerException inheriting impossible,
     *                         IOException occurred
     */
    @Override
    public void implement(Class<?> aClass, Path root) throws ImplerException {
        boolean bConstr = aClass.isInterface();
        for (Constructor constructor : aClass.getDeclaredConstructors()) {
            if (!Modifier.isPrivate(constructor.getModifiers())) {
                bConstr = true;
            }
        }
        if (Modifier.isFinal(Modifier.classModifiers() & aClass.getModifiers())) {
            throw new ImplerException("Class is final");
        }
        if (aClass == Enum.class) {
            throw new ImplerException("Enum can not be extended");
        }
        if (!bConstr) {
            throw new ImplerException("No constructors");
        }
        Path path;
        try {
            path = Files.createDirectories(root.resolve(Paths.get(aClass.getPackage().getName().replace(".", File.separator) + File.separator)));
        } catch (IOException e) {
            throw new ImplerException();
        }
        try (Writer writer = new UnicodeFilter(Files.newBufferedWriter(path.resolve(aClass.getSimpleName() + "Impl.java"), Charset.defaultCharset()))) {
            ClassDescriber classDescriber = new ClassDescriber(aClass);
            writer.write(classDescriber.emptyImplemented());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Class that's have to make some implementation of classes
     */
    private class ClassDescriber {
        private static final String TAB = "    ";
        private Class aClass;
        private StringBuilder builder = new StringBuilder();

        /**
         * creates instance
         * @param aClass class from what we want to make instance
         */
        public ClassDescriber(Class aClass) {
            this.aClass = aClass;
        }

        /**
         * returns implementation
         * @return string with implementation
         * @throws ImplerException if something wrong
         */
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

            for (Method method : getAbstractMethods(aClass)) {
                printMethod(method);
            }
            write("\n}");
            return builder.toString();
        }

        /**
         * Returns list of methods that must be implemented.
         * Returns only abstract methods of superclasses
         * @param aClass class from where gets methods
         * @return list of all non abstract methods
         */
        private List<Method> getAbstractMethods(Class aClass) {
            Map<String, Method> map = new HashMap<>();
            getAllHierarchyMethods(map, aClass);
            return map.values().stream().filter(method -> Modifier.isAbstract(method.getModifiers())).collect(Collectors.toList());
        }

        /**
         * Puts methods of superclass and interfaces into the map.
         * Uses #getHashString(Method) to keep from adding already defined method.
         * @param map map with methods
         * @param aClass class from where gets methods
         */
        private void getAllHierarchyMethods(Map<String, Method> map, Class aClass) {
            if (aClass == null) {
                return;
            }
            for (Method method : aClass.getDeclaredMethods()) {
                map.putIfAbsent(method.getName() + Arrays.toString(method.getParameterTypes()), method);
            }
            getAllHierarchyMethods(map, aClass.getSuperclass());
            for (Class interf : aClass.getInterfaces()) {
                getAllHierarchyMethods(map, interf);
            }
        }

        /**
         * adds one non private constructor to implementation
         * @param constructor non private constructor
         * @param simpleName simple name of constructor
         */
        private void printConstructor(Constructor constructor, String simpleName) {
            write(TAB, "\npublic ", simpleName, "(");
            printArgs(constructor.getParameterTypes());
            write(")");
            printExceptions(constructor.getExceptionTypes());
            write("{\n", TAB, TAB, "super(");
            int n = constructor.getParameterTypes().length;
            for (int i = 0; i < n; i++) {
                write("arg", String.valueOf(i));
                if (i != n - 1) {
                    write(", ");
                }
            }
            write(TAB, TAB, ");\n", TAB, "}");
        }

        /**
         * adds one non private and non abstract method to implementation
         * @param method non private abstract method
         */
        private void printMethod(Method method) {
            write(TAB, "\n", Modifier.toString(~Modifier.ABSTRACT & method.getModifiers() & Modifier.methodModifiers()));
            write(" ", method.getReturnType().getCanonicalName(), " ", method.getName(), "(");
            printArgs(method.getParameterTypes());
            write(") {\n");
            Class retClass = method.getReturnType();
            if (!retClass.equals(void.class)) {
                write(TAB, TAB, "return ");
                String retDefault;
                if (retClass.isPrimitive()) {
                    retDefault = retClass.equals(boolean.class) ? "false" : "0";
                } else {
                    retDefault = "null";
                }
                write(retDefault, ";");
            }
            write("\n", TAB, "}");
        }

        /**
         * adds arguments of constructor or method to implementation
         * @param args arguments of constructor or method
         */
        private void printArgs(Class[] args) {
            for (int i = 0; i < args.length; i++) {
                write(args[i].getCanonicalName(), " arg", String.valueOf(i));
                if (i != args.length - 1) {
                    write(", ");
                }
            }
        }

        /**
         * adds exeptions of constructor to implementation
         * @param exceptions exceptions to append
         */
        private void printExceptions(Class[] exceptions) {
            if (exceptions.length != 0) {
                write(" throws ");
            }
            for (int i = 0; i < exceptions.length; i++) {
                write(exceptions[i].getCanonicalName());
                if (i != exceptions.length - 1) {
                    write(", ");
                }
            }
            write(" ");
        }

        /**
         * adds string to builder
         * @param strings strings to append
         */
        private void write(String... strings) {
            for (String string : strings) {
                builder.append(string);
            }
        }
    }

    /**
     * Filters non-ASCII characters in output stream and converts it to "\\uXXXX" sequences.
     * Use {@link #write(String, int, int)} to filter.
     */
    private class UnicodeFilter extends FilterWriter {
        /**
         * create instance
         * @param out writer from what we want to make instance
         */
        protected UnicodeFilter(Writer out) {
            super(out);
        }

        /**
         * Prints current sumbol in correct charset
         * @param c what to write
         * @throws IOException when something wrong
         */
        @Override
        public void write(int c) throws IOException {
            if (c >= 128) {
                super.write(String.format("\\u%04X", (int) c));
            } else {
                super.write(c);
            }
        }

        /**
         * Replaces unicode characters in string to "\\uXXXX" sequences.
         * @param string string to write
         * @param off    from what char write
         * @param len    lenght to write
         * @throws IOException when something wrong
         */
        @Override
        public void write(String string, int off, int len) throws IOException {
            for (char c : string.substring(off, off + len).toCharArray()) {
                write(c);
            }
        }
    }
}