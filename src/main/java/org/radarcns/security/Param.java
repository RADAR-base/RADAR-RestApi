package org.radarcns.security;

/**
 * Checks if parameters are valid or not matching again REGEXs.
 */
public class Param {

    public static boolean isNullOrEmpty(String input) {
        return (input == null) || input.trim().isEmpty();
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
}
