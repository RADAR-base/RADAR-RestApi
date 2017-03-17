package org.radarcns.monitor;

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

import com.mongodb.MongoClient;
import java.net.ConnectException;
import java.util.HashMap;
import org.radarcns.avro.restapi.sensor.SensorSpecification;
import org.radarcns.avro.restapi.source.Source;
import org.radarcns.avro.restapi.source.SourceSpecification;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.source.SourceCatalog;

/**
 * Generic Data Accesss Object database independent.
 */
public class Monitors {

    /** Map containing actual implementations of each source monitor. **/
    private final HashMap<SourceType, SourceMonitor> hooks;

    /** Singleton instance. **/
    private static Monitors instance;

    /** Constructo.r **/
    private Monitors() {
        hooks = new HashMap<>();

        hooks.put(SourceType.EMPATICA,
                new SourceMonitor(SourceCatalog.getInstance(SourceType.EMPATICA)));
    }

    /**
     * Static initializer.
     */
    static {
        instance = new Monitors();
    }

    /**
     * Returns the singleton.
     * @return the singleton {@code Monitors} instance
     */
    public static Monitors getInstance() {
        return instance;
    }

    /**
     * Checks the status for the given source counting the number of received messages and
     *      checking whether it respects the sensor frequencies. There is a check for each sensor.
     *
     * @param user identifier
     * @param source identifier
     * @param client is the MongoDB client
     * @return {@code SourceDefinition} representing a source source
     * @throws ConnectException if the connection with MongoDb is faulty
     *
     * @see {@link Source}
     */
    public Source getState(String user, String source, SourceType sourceType, MongoClient client)
            throws ConnectException {
        SourceMonitor monitor = hooks.get(sourceType);

        if (monitor == null) {
            throw new UnsupportedOperationException(sourceType.name()
                    + "is not currently supported");
        }

        return monitor.getState(user, source, client);
    }

    /**
     * Returns the SourceDefinition Specification used by the monitor associated with the monitor.
     *
     * @return {@code SourceSpecification} containing all sensor names and related frequencies
     *
     * @see {@link SensorSpecification}
     * @see {@link SourceSpecification}
     */
    public SourceSpecification getSpecification(SourceType sourceType)
        throws ConnectException {
        SourceMonitor monitor = hooks.get(sourceType);

        if (monitor == null) {
            throw new UnsupportedOperationException(sourceType.name()
                + "is not currently supported");
        }

        return monitor.getSource().getSpecification();
    }


}
