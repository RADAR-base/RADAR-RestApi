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

package org.radarcns.monitor;

import com.mongodb.MongoClient;
import java.net.ConnectException;
import java.util.HashMap;
import org.radarcns.restapi.source.Source;
import org.radarcns.restapi.spec.SourceSpecification;
import org.radarcns.source.SourceCatalog;

/**
 * Generic Data Accesss Object database independent.
 */
public class Monitors {

    /** Map containing actual implementations of each source monitor. **/
    private final HashMap<String, SourceMonitor> hooks;

    /** Singleton instance. **/
    private static final Monitors INSTANCE = new Monitors();

    /** Constructor. **/
    private Monitors() {
        hooks = new HashMap<>();
    }

    /**
     * Returns the singleton.
     * @return the singleton {@code Monitors} instance
     */
    public static Monitors getInstance() {
        return INSTANCE;
    }

    private SourceMonitor getMonitor(String sourceType) {
        return hooks.computeIfAbsent(sourceType,
                (k) -> new SourceMonitor(SourceCatalog.getInstance().getDefinition(k)));
    }

    /**
     * Checks the status for the given source counting the number of received messages and
     *      checking whether it respects the data frequencies. There is a check for each data.
     *
     * @param client is the MongoDB client
     * @param subject identifier
     * @param source identifier
     * @return {@code SourceDefinition} representing a source source
     * @throws ConnectException if the connection with MongoDb is faulty
     *
     * @see Source
     */
    public Source getState(MongoClient client, String subject, String source, String sourceType)
            throws ConnectException {
        return getMonitor(sourceType).getState(subject, source, client);
    }

    /**
     * Returns the SourceDefinition Specification used by the monitor associated with the monitor.
     *
     * @return {@code SourceSpecification} containing all data names and related frequencies
     */
    public SourceSpecification getSpecification(String sourceType) {
        return getMonitor(sourceType).getSource().getSpecification();
    }


}
