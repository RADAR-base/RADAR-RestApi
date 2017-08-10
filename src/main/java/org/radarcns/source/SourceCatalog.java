package org.radarcns.source;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.config.Properties;
import org.radarcns.config.catalog.DeviceCatalog;
import org.radarcns.dao.mongo.data.sensor.DataFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * All supported sourceCatalog specifications.
 */
public class SourceCatalog {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceCatalog.class);

    /** Variables. **/
    private final Map<SourceType, SourceDefinition> sourceCatalog;
    private final Set<SensorType> supportedSensor;
    private final Map<SensorType, DataFormat> formatMap;

    /** Singleton instance. **/
    private static final SourceCatalog INSTANCE;

    // Static initializer.
    static {
        try {
            INSTANCE = new SourceCatalog(Properties.getDeviceCatalog());
        } catch (ClassNotFoundException exec) {
            LOGGER.error(exec.getMessage(), exec);
            throw new ExceptionInInitializerError(exec);
        }
    }

    /**
     * Returns the singleton.
     * @return the singleton {@code SourceCatalog} instance
     */
    public static SourceCatalog getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the {@code SourceDefinition} associated with the given {@code SourceType}.
     * @return the singleton {@code SourceDefinition} instance
     */
    public static SourceDefinition getInstance(SourceType sourceType) {
        SourceDefinition definition = INSTANCE.sourceCatalog.get(sourceType);

        if (definition == null) {
            throw new UnsupportedOperationException(sourceType.name() + " is not supported yet.");
        }

        return definition;
    }

    private SourceCatalog(DeviceCatalog catalog) throws ClassNotFoundException {
        sourceCatalog = new HashMap<>();
        supportedSensor = new HashSet<>();
        formatMap = new HashMap<>();

        for (SourceType source : catalog.getSupportedSources()) {
            sourceCatalog.put(source, new SourceDefinition(
                    source, catalog.getDevices().get(source)));

            supportedSensor.addAll(sourceCatalog.get(source).getSensorTypes());

            SourceDefinition definition = sourceCatalog.get(source);

            for (SensorType sensor : definition.getFormats().keySet()) {
                if (formatMap.get(sensor) == null) {
                    formatMap.put(sensor, definition.getFormats().get(sensor));
                } else if (!formatMap.get(sensor).equals(definition.getFormats().get(sensor))) {
                    throw new IllegalArgumentException("The same sensor cannot have two different"
                        + " format at the same time. Find " + formatMap.get(sensor) + " while"
                        + " putting " + definition.getFormats().get(sensor));
                }
            }

            supportedSensor.addAll(sourceCatalog.get(source).getSensorTypes());
        }
    }

    /**
     * Returns source's SourceDefinition.
     * @param source sourceType involved in the interaction
     * @return the SourceDefinition related to the input
     * @see SourceDefinition
     */
    public SourceDefinition getDefinition(SourceType source) {
        SourceDefinition definition = sourceCatalog.get(source);

        if (definition != null) {
            return definition;
        }

        throw new UnsupportedOperationException(source.name() + " is not currently supported.");
    }

    /**
     * Returns the supported source type.
     * @return a set containing all supported sourceType
     */
    public Set<SourceType> getSupportedSource() {
        return sourceCatalog.keySet();
    }

    /**
     * Returns the supported sensor set.
     * @return a set containing all supported sourceType
     */
    public Set<SensorType> getSupportedSensor() {
        return supportedSensor;
    }

    /**
     * Returns the DataFormat associated with the sensor.
     */
    public DataFormat getFormat(SensorType sensor) {
        DataFormat format = formatMap.get(sensor);

        if (format != null) {
            return format;
        }

        throw new UnsupportedOperationException(sensor.name() + " is not currently supported.");
    }

}
