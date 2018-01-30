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

package org.radarcns.source;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.inject.Inject;
import org.radarcns.catalog.SourceDefinition;
import org.radarcns.config.Properties;
import org.radarcns.config.catalog.DeviceCatalog;
import org.radarcns.dao.mongo.data.sensor.DataFormat;

/**
 * All supported sourceCatalog specifications.
 */
public class SourceCatalog {
    /** Variables. **/
    private final Map<String, SourceDefinition> sourceCatalog;
    private final Set<String> supportedSensor;
    private final Map<String, DataFormat> formatMap;

    private static final SourceCatalog INSTANCE = new SourceCatalog();

    public static SourceCatalog getInstance() {
        return INSTANCE;
    }

    public SourceCatalog() {
        this(Properties.getDeviceCatalog());
    }

    public SourceCatalog(DeviceCatalog catalog) {
        sourceCatalog = new HashMap<>();
        supportedSensor = new HashSet<>();
        formatMap = new HashMap<>();

        for (String source : catalog.getSupportedSources()) {
            sourceCatalog.put(source, new SourceDefinition(
                    source, catalog.getDevices().get(source)));

            supportedSensor.addAll(sourceCatalog.get(source).getSensorTypes());

            SourceDefinition definition = sourceCatalog.get(source);

            for (String sensor : definition.getFormats().keySet()) {
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
    public SourceDefinition getDefinition(String source) {
        SourceDefinition definition = sourceCatalog.get(source);
        if (definition == null) {
            throw new NoSuchElementException("Source " + source + " is not found.");
        }

        return definition;
    }

    /**
     * Returns the supported source type.
     * @return a set containing all supported sourceType
     */
    public Set<String> getSupportedSource() {
        return sourceCatalog.keySet();
    }

    /**
     * Returns the supported sensor set.
     * @return a set containing all supported sourceType
     */
    public Set<String> getSupportedSensor() {
        return supportedSensor;
    }

    /**
     * Returns the DataFormat associated with the sensor.
     */
    public DataFormat getFormat(String sensor) {
        DataFormat format = formatMap.get(sensor);

        if (format != null) {
            return format;
        }

        throw new UnsupportedOperationException(sensor + " is not currently supported.");
    }
}
