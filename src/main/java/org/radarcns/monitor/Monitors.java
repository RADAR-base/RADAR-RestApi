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
import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import javax.servlet.ServletContext;
import org.radarcns.domain.managementportal.SourceType;
import org.radarcns.domain.restapi.Source;
import org.radarcns.mongo.util.MongoHelper;
import org.radarcns.catalog.SourceCatalog;

/**
 * Generic Data Accesss Object database independent.
 */
public class Monitors {

    /** Map containing actual implementations of each sourceType monitor. **/
    private final HashMap<String, SourceMonitor> hooks;

    /** Singleton instance. **/
    private static final Monitors INSTANCE;

    /** Constructor. **/
    private Monitors(SourceCatalog catalog) {
        hooks = new HashMap<>();

        try {
            for (String sourceType : catalog.getSupportedSource()) {
                hooks.put(sourceType, new SourceMonitor(null));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Static initializer.
     */
    static {
        INSTANCE = new Monitors(null);
    }

    /**
     * Returns the singleton.
     * @return the singleton {@code Monitors} instance
     */
    public static Monitors getInstance() {
        return INSTANCE;
    }

    /**
     * Checks the status for the given sourceType counting the number of received messages and
     *      checking whether it respects the data frequencies. There is a check for each data.
     *
     * @param subject identifier
     * @param source identifier
     * @param context {@link ServletContext} used to retrieve the client for accessing the
     *      results cache
     * @return {@code SourceDefinition} representing a sourceType sourceType
     * @throws ConnectException if the connection with MongoDb is faulty
     *
     * @see Source
     */
    public Source getState(String subject, String source, String sourceType,
            ServletContext context) throws ConnectException {
        return getState(subject, source, sourceType, MongoHelper.getClient(context));
    }

    /**
     * Checks the status for the given sourceType counting the number of received messages and
     *      checking whether it respects the data frequencies. There is a check for each data.
     *
     * @param subject identifier
     * @param source identifier
     * @param client is the MongoDB client
     * @return {@code SourceDefinition} representing a sourceType sourceType
     * @throws ConnectException if the connection with MongoDb is faulty
     *
     * @see Source
     */
    public Source getState(String subject, String source, String sourceType, MongoClient client)
            throws ConnectException {
        SourceMonitor monitor = hooks.get(sourceType);

        if (monitor == null) {
            throw new UnsupportedOperationException(sourceType + "is not currently supported");
        }
        // TODO find the number of records available and get the state
//        return monitor.getState(subject, sourceType, client , 0);
        return null;
    }

    /**
     * Returns the SourceDefinition Specification used by the monitor associated with the monitor.
     *
     * @return {@code SourceSpecification} containing all data names and related frequencies
     */
    public SourceType getSpecification(String sourceType)
            throws ConnectException {
        SourceMonitor monitor = hooks.get(sourceType);

        if (monitor == null) {
            throw new UnsupportedOperationException(sourceType + " is not currently supported");
        }

//        return monitor.getSourceType().getSpecification();
        return null;
    }


}
