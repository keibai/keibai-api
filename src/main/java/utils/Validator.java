package main.java.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean isEmail(String email) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(email);
        return matcher.find();
    }

    public static boolean isLength(String text, int minimum) {
        return text.length() >= minimum;
    }

    public static boolean isLength(String text, int minimum, int maximum) {
        return text.length() >= minimum && text.length() <= maximum;
    }
}
