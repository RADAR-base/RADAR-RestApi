package org.radarcns.unit.security;

/*
 *  Copyright 2016 King's College London and The Hyve
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.radarcns.security.Param;

public class ParamTest {

    @Test
    public void isNullOrEmptyTest() {
        assertEquals(true, Param.isNullOrEmpty(null));
        assertEquals(true, Param.isNullOrEmpty(""));
        assertEquals(false, Param.isNullOrEmpty("test"));
        assertEquals(false, Param.isNullOrEmpty("1"));
        assertEquals(false, Param.isNullOrEmpty("test1"));
    }

    @Test
    public void isNumericTest() {
        assertEquals(false, Param.isNumeric(null));
        assertEquals(false, Param.isNumeric(""));
        assertEquals(false, Param.isNumeric("test"));
        assertEquals(true, Param.isNumeric("1"));
        assertEquals(false, Param.isNumeric("test1"));
    }

    @Test
    public void isAlphaTest() {
        assertEquals(false, Param.isAlpha(null));
        assertEquals(false, Param.isAlpha(""));
        assertEquals(true, Param.isAlpha("test"));
        assertEquals(false, Param.isAlpha("1"));
        assertEquals(false, Param.isAlpha("test1"));
    }

    @Test
    public void isAlphaNumericTest() {
        assertEquals(false, Param.isAlphaNumeric(null));
        assertEquals(false, Param.isAlphaNumeric(""));
        assertEquals(true, Param.isAlphaNumeric("test"));
        assertEquals(true, Param.isAlphaNumeric("1"));
        assertEquals(true, Param.isAlphaNumeric("test1"));
    }

    @Test
    public void isMacAddressTest() {
        assertEquals(false, Param.isMacAddress(null));
        assertEquals(false, Param.isMacAddress(""));
        assertEquals(false, Param.isMacAddress("test"));
        assertEquals(false, Param.isMacAddress("1"));
        assertEquals(false, Param.isMacAddress(" 01:23:45:67:89:ab"));
        assertEquals(false, Param.isMacAddress(" 01:23:45:67:89:abb"));
        assertEquals(false, Param.isMacAddress("01:23:45:67:89:abb"));
        assertEquals(true, Param.isMacAddress("01:23:45:67:89:ab"));
    }

    @Test
    public void isAlphaNumericAndSpecialsTest() {
        assertEquals(false, Param.isAlphaNumericAndSpecials(null));
        assertEquals(false, Param.isAlphaNumericAndSpecials(""));
        assertEquals(true, Param.isAlphaNumericAndSpecials("test"));
        assertEquals(true, Param.isAlphaNumericAndSpecials("1"));
        assertEquals(true, Param.isAlphaNumericAndSpecials("01:23:45:67:89:ab"));
        assertEquals(true, Param.isAlphaNumericAndSpecials("01:23:45:67:89:abb"));
        assertEquals(true, Param.isAlphaNumericAndSpecials("SourceID_1"));
        assertEquals(true, Param.isAlphaNumericAndSpecials("SourceID#1"));
        assertEquals(true, Param.isAlphaNumericAndSpecials("SourceID@1"));
    }

    @Test
    public void isUserTest() {
        assertEquals(false, Param.isUser(null));
        assertEquals(false, Param.isUser(""));
        assertEquals(true, Param.isUser("UserID_1"));
        assertEquals(true, Param.isUser("UserID_1"));
        assertEquals(true, Param.isUser("UserID#1"));
        assertEquals(true, Param.isUser("UserID@1"));
    }

    @Test
    public void isSourceTest() {
        assertEquals(false, Param.isSource(null));
        assertEquals(false, Param.isSource(""));
        assertEquals(true, Param.isSource("UserID_1"));
        assertEquals(true, Param.isSource("UserID_1"));
        assertEquals(true, Param.isSource("UserID#1"));
        assertEquals(true, Param.isSource("UserID@1"));
    }

    @Test
    public void isValidInputTest() {
        int count = 0;
        count = isValidInputTestCount(null, null, count);
        count = isValidInputTestCount("", null, count);
        count = isValidInputTestCount("UserID_0", null, count);
        count = isValidInputTestCount(null, "SourceID_0", count);
        count = isValidInputTestCount(null, "", count);
        count = isValidInputTestCount("", "", count);
        count = isValidInputTestCount("UserID_0", "SourceID_0", count);

        assertEquals(6, count);
    }

    private int isValidInputTestCount(String user, String source, int count) {
        try {
            Param.isValidInput(user, source);
        } catch (IllegalArgumentException exec) {
            count++;
        }

        return count;
    }

    @Test
    public void isValidUserTest() {
        int count = 0;
        count = isValidUserTestCount(null, count);
        count = isValidUserTestCount("", count);
        count = isValidUserTestCount("UserID_0", count);

        assertEquals(2, count);
    }

    private int isValidUserTestCount(String user, int count) {
        try {
            Param.isValidUser(user);
        } catch (IllegalArgumentException exec) {
            count++;
        }

        return count;
    }

    @Test
    public void isValidSourceTest() {
        int count = 0;
        count = isValidSourceTestCount(null, count);
        count = isValidSourceTestCount("", count);
        count = isValidSourceTestCount("UserID_0", count);

        assertEquals(2, count);
    }

    private int isValidSourceTestCount(String source, int count) {
        try {
            Param.isValidUser(source);
        } catch (IllegalArgumentException exec) {
            count++;
        }

        return count;
    }

}
