package org.radarcns.source;

/*
 *  Copyright 2016 Kings College London and The Hyve
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

import org.radarcns.avro.restapi.source.SourceType;

/**
 * All supported sources specifications.
 */
public class SourceCatalog {

    private static class EmpaticaInstanceHolder {
        private static final Empatica instance = new Empatica();
    }

    /**
     * Returns source's SourceDefinition.
     * @param source sourceType involved in the interaction
     * @return the SourceDefinition related to the input
     * @see {@link SourceDefinition}
     */
    public static SourceDefinition getInstance(SourceType source) {
        switch (source) {
            case ANDROID: break;
            case BIOVOTION: break;
            case EMPATICA: return EmpaticaInstanceHolder.instance;
            case PEBBLE: break;
            default: break;
        }

        throw new UnsupportedOperationException(source.name() + " is not"
            + " currently supported.");
    }

}
