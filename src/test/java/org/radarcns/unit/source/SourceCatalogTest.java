package org.radarcns.unit.source;

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
import static org.radarcns.avro.restapi.source.SourceType.EMPATICA;
import static org.radarcns.avro.restapi.source.SourceType.PEBBLE;

import org.junit.Test;
import org.radarcns.source.SourceCatalog;

public class SourceCatalogTest {

    @Test(expected = UnsupportedOperationException.class)
    public void sourceCatalogTest() {
        assertEquals(EMPATICA, SourceCatalog.getInstance(EMPATICA).getType());

        SourceCatalog.getInstance(PEBBLE).getType();
    }

}
