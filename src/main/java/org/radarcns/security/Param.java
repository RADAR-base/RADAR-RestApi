package org.radarcns.security;

/**
 * Checks if parameters are valid or not matching again REGEXs.
 */
public class Param {

    public static final String USER = "user";
    public static final String SOURCE = "source";

    public static boolean isNullOrEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }

    public static boolean isNumeric(String input) {
        return !isNullOrEmpty(input) && input.matches("\\d+");
    }

    public static boolean isAlpha(String input) {
        return !isNullOrEmpty(input) && input.matches("[a-zA-Z]+");
    }

    public static boolean isAlphaNumeric(String input) {
        return !isNullOrEmpty(input) && input.matches("^[a-zA-Z0-9]*$");
    }

    public static boolean isMacAddress(String input) {
        return !isNullOrEmpty(input) && input.matches("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
    }

    public static boolean isAlphaNumericAndSpecials(String input) {
        return !isNullOrEmpty(input) && input.matches("^[a-zA-Z0-9-_./:#@-]*$");
    }

    public static boolean isUser(String input) {
        return isAlphaNumericAndSpecials(input);
    }

    public static boolean isSource(String input) {
        return isAlphaNumericAndSpecials(input);
    }

    /**
     * Given a userID and a sourceID, it checks if they are valid input or not.
     **/
    public static void isValidInput(String user, String source) {
        if (!(isUser(user) && isSource(source))) {
            throw new IllegalArgumentException("Parameters do not respect REGEXs");
        }
    }

    /**
     * Given a userID, it checks if they are valid input or not.
     **/
    public static void isValidUser(String user) {
        validOrThrow(user, USER);
    }

    /**
     * Given a sourceID, it checks if they are valid input or not.
     **/
    public static void isValidSource(String source) {
        validOrThrow(source, SOURCE);
    }

    /**
     * Checks against the REGEX whether the input parameter is valid or not.
     **/
    public static void validOrThrow(String value, String param) throws IllegalArgumentException {
        boolean test;

        switch (param) {
            case USER:      test = isUser(value);
                            break;
            case SOURCE:    test = isSource(value);
                            break;
            default: throw new UnsupportedOperationException(param + " is not supported yet.");
        }

        if (!test) {
            throw new IllegalArgumentException(param + "parameter does not respect REGEXs");
        }
    }

}
