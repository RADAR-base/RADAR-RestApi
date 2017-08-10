package org.radarcns.unit.security;

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
        int countLocal = count;
        try {
            Param.isValidInput(user, source);
        } catch (IllegalArgumentException exec) {
            countLocal++;
        }

        return countLocal;
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
        int countLocal = count;
        try {
            Param.isValidSubject(user);
        } catch (IllegalArgumentException exec) {
            countLocal++;
        }

        return countLocal;
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
        int countLocal = count;
        try {
            Param.isValidSubject(source);
        } catch (IllegalArgumentException exec) {
            countLocal++;
        }

        return countLocal;
    }

}
