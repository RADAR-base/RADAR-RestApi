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

package org.radarcns.catalog;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.ServletContext;
import org.radarcns.mongo.data.sensor.DataFormat;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.domain.managementportal.SourceData;
import org.radarcns.domain.managementportal.SourceType;
import org.radarcns.domain.managementportal.SourceTypeIdentifier;
import org.radarcns.util.CachedMap;
import org.radarcns.webapp.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores all the source-type definition and source-data.
 */
public class SourceCatalog {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceCatalog.class);


    private CachedMap<SourceTypeIdentifier, SourceType> sourceTypes;

    private CachedMap<String, SourceData> sourceData;

    private final ManagementPortalClient managementPortalClient ;

    private static final Duration CACHE_INVALIDATE_DEFAULT = Duration.ofMinutes(1);

    private static final Duration CACHE_RETRY_DEFAULT = Duration.ofHours(1);

    /**
     * Constructor to create SourceCatalog.
     * @param managementPortalClient of the context
     */
    public SourceCatalog(ManagementPortalClient managementPortalClient) {
        this.managementPortalClient = managementPortalClient;
            this.sourceTypes = new CachedMap<>(this.managementPortalClient::retrieveSourceTypes,
                    SourceType::getSourceTypeIdentifier,
                    CACHE_INVALIDATE_DEFAULT, CACHE_RETRY_DEFAULT);
            this.sourceData = new CachedMap<>(this.managementPortalClient::retrieveSourceData,
                    SourceData::getSourceDataName,
                    CACHE_INVALIDATE_DEFAULT, CACHE_RETRY_DEFAULT);
    }

    /**
     * Retrieves all {@link SourceType} from Management Portal using {@link ServletContext} entity.
     *
     * @return {@link ArrayList} of {@link SourceType} retrieved from the Management Portal
     */
    public List<SourceType> getSourceTypes() throws IOException {
        return new ArrayList<>(sourceTypes.get().values());
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
    public SourceType getSourceType(String producer, String model, String catalogVersion)
            throws NotFoundException, IOException {
        SourceType result;
        try {
            result = sourceTypes.get(new SourceTypeIdentifier(producer, model,
                    catalogVersion));
        } catch (NoSuchElementException exe) {
            throw new NotFoundException("Cannot find source-type of identifier "
                    + producer + ":" + model + ":" + catalogVersion , exe);
        }
        return result;
    }


    /**
     * Retrieves all {@link SourceData} from Management Portal using {@link ServletContext} entity.
     *
     * @return {@link ArrayList} of {@link SourceData} retrieved from the Management Portal
     */
    public List<SourceData> getSourceData() throws IOException {
        return new ArrayList<>(sourceData.get().values());
    }

    /**
     * Retrieves a {@link SourceData} from the Management Portal using {@link ServletContext}
     * entity.
     *
     * @param sourceDataName {@link String} of the Source-Data that has to be retrieved
     * @return {@link SourceType} retrieved from the Management Portal
     */
    public SourceData getSourceData(String sourceDataName)
            throws NotFoundException, IOException {
        SourceData result = sourceData.get(sourceDataName);
        if (Objects.isNull(result)) {
            result = sourceData.get(true).get(sourceDataName);
        }
        if (Objects.isNull(result)) {
            throw new NotFoundException("Cannot find source-data of identifier " + sourceDataName);
        }
        return result;
    }

//    /**
//     * Returns the {@code SourceDefinition} associated with the given {@code SourceType}.
//     * @return the singleton {@code SourceDefinition} instance
//     */
//    public SourceType getSourceTypeDefinition(SourceTypeIdentifier sourceType) {
//        SourceType definition = sourceTypes.get(true).get(sourceType);
//
//        if (definition == null) {
//            throw new UnsupportedOperationException(sourceType + " is not supported yet.");
//        }
//
//        return definition;
//    }



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
    public Set<String> getSupportedSource() throws IOException {
        return sourceTypes.get().keySet().stream().map(SourceTypeIdentifier::toString).collect(
                Collectors.toSet());
    }

    /**
     * Returns the supported sensor set.
     * @return a set containing all supported sourceType
     */
    public Set<SourceData> getSupportedSensor() throws IOException {
        return sourceData.get().values().stream().collect(Collectors.toSet());
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
