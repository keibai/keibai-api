package main.java.utils;

public class Logger {
    public static void error(String message) {
        System.out.println("-- ERROR: --");
        System.out.println(message);
        System.out.println("------------");
    }
}
