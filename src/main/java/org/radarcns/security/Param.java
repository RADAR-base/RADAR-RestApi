package org.radarcns.security;

/**
 * Created by Francesco Nobilia on 10/01/2017.
 */
public class Param {

    public static boolean isNullOrEmpty(String input){
        return (input == null) || input.trim().isEmpty();
    }

    public static boolean isNumeric(String input){
        return !isNullOrEmpty(input) && input.matches("\\d+");
    }

    public static boolean isAlpha(String input){
        return !isNullOrEmpty(input) && input.matches("[a-zA-Z]+");
    }

    public static boolean isAlphaNumeric(String input){
        return !isNullOrEmpty(input) && input.matches("^[a-zA-Z0-9]*$");
    }

    public static boolean isMacAddress(String input){
        return !isNullOrEmpty(input) && input.matches("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
    }

    public static boolean isUser(String input){
        return isAlphaNumeric(input);
    }

    public static boolean isSource(String input){
        return isMacAddress(input);
    }

    public static void isValidInput(String user, String source){
        if( !(isUser(user) && isSource(source)) ){
            throw new IllegalArgumentException("Parameters do not respect REGEXs");
        }
    }
}
