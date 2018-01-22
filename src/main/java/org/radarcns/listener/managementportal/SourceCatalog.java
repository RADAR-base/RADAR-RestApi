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

package org.radarcns.listener.managementportal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.ServletContext;
import org.radarcns.dao.mongo.data.sensor.DataFormat;
import org.radarcns.managementportal.SourceData;
import org.radarcns.managementportal.SourceType;
import org.radarcns.managementportal.SourceTypeIdentifier;
import org.radarcns.webapp.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * All supported sourceCatalog specifications.
 */
public class SourceCatalog {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceCatalog.class);


    private final Map<SourceTypeIdentifier, SourceType> sourceTypes;

    private final Map<String, SourceData> sourceData;

    public SourceCatalog(List<SourceType> sourceTypes) {
        this.sourceTypes = sourceTypes.stream().collect(Collectors.toMap
                (SourceType::getSourceTypeIdentifier , p -> p));
        this.sourceData = sourceTypes.stream().map(SourceType::getSourceData).flatMap
                (Collection::stream)
                .collect(Collectors.toMap(SourceData::getSourceDataName , p -> p));
    }




    /**
     * Retrieves all {@link SourceType} from Management Portal using {@link ServletContext} entity.
     *
     * @return {@link ArrayList} of {@link SourceType} retrieved from the Management Portal
     */
    public List<SourceType> getSourceTypes() throws IOException {
        return new ArrayList<>(sourceTypes.values());
    }

    /**
     * Retrieves a {@link SourceType} from the Management Portal using {@link ServletContext}
     * entity.
     *
     * @param producer {@link String} of the Source-type that has to be retrieved
     * @param model {@link String} of the Source-type that has to be retrieved
     * @param catalogVersion {@link String} of the Source-type that has to be retrieved
     * @return {@link SourceType} retrieved from the Management Portal
     */
    public SourceType getSourceType(String producer, String model, String catalogVersion) throws
            IOException,
            NotFoundException {
        try {
            return sourceTypes.get(new SourceTypeIdentifier(producer, model, catalogVersion));
        } catch (NoSuchElementException ex) {
            throw new NotFoundException("Source-type " + producer + " : " + model + " : "
                    + catalogVersion + " not found");
        }
    }

    /**
     * Returns the {@code SourceDefinition} associated with the given {@code SourceType}.
     * @return the singleton {@code SourceDefinition} instance
     */
    public SourceType getSourceTypeDefinition(SourceTypeIdentifier sourceType) {
        SourceType definition = sourceTypes.get(sourceType);

        if (definition == null) {
            throw new UnsupportedOperationException(sourceType + " is not supported yet.");
        }

        return definition;
    }



//    /**
//     * Returns source's SourceDefinition.
//     * @param source sourceType involved in the interaction
//     * @return the SourceDefinition related to the input
//     * @see SourceDefinition
//     */
//    public SourceDefinition getDefinition(String source) {
//        SourceDefinition definition = sourceCatalog.get(source);
//
//        if (definition != null) {
//            return definition;
//        }
//
//        throw new UnsupportedOperationException(source + " is not currently supported.");
//    }

    /**
     * Returns the supported source type.
     * @return a set containing all supported sourceType
     */
    public Set<String> getSupportedSource() {
        return sourceTypes.keySet().stream().map(SourceTypeIdentifier::toString).collect(
                Collectors.toSet());
    }

    /**
     * Returns the supported sensor set.
     * @return a set containing all supported sourceType
     */
    public Set<SourceData> getSupportedSensor() {
        return sourceData.values().stream().collect(Collectors.toSet());
    }



    /**
     * Returns the DataFormat associated with the sensor.
     */
    public DataFormat getFormat(String sensor) {
//        DataFormat format = formatMap.get(sensor);
//
//        if (format != null) {
//            return format;
//        }

        return null;

//        throw new UnsupportedOperationException(sensor + " is not currently supported.");
    }

}
