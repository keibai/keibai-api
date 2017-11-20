package main.java.utils;

public class Logger {
    public static void error(String... messages) {
        System.out.println("-- ERROR: --");
        for (String message : messages) {
            System.out.println(message);
        }
        System.out.println("------------");
    }
}
