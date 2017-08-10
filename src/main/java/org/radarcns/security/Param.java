package org.radarcns.security;

/*
 * Copyright 2016 King's College London and The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.regex.Pattern;

/**
 * Checks if parameters are valid or not matching again REGEXs.
 */
public class Param {
    private static final Pattern IS_ALPHANUMERIC_SPECIAL = Pattern.compile(
            "^[a-zA-Z0-9_./:#@-]+$");

    public static boolean isNullOrEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }

    public static boolean isAlphaNumericAndSpecials(String input) {
        return input != null && IS_ALPHANUMERIC_SPECIAL.matcher(input).matches();
    }

    /**
     * Given a userID and a sourceID, it checks if they are valid input or not.
     **/
    public static void isValidInput(String user, String source) {
        isValidSubject(user);
        isValidSource(source);
    }

    /**
     * Given a subjectID, it checks if they are valid input or not.
     **/
    public static void isValidSubject(String user) {
        if (!isAlphaNumericAndSpecials(user)) {
            throw new IllegalArgumentException("SUBJECT " + user + " is not alphanumeric");
        }
    }

    /**
     * Given a sourceID, it checks if they are valid input or not.
     **/
    public static void isValidSource(String source) {
        if (!isAlphaNumericAndSpecials(source)) {
            throw new IllegalArgumentException("SOURCE " + source + " is not alphanumeric");
        }
    }
}
