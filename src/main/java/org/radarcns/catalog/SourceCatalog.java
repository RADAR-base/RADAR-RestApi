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
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.NotFoundException;
import org.radarcns.domain.managementportal.SourceTypeDTO;
import org.radarcns.domain.managementportal.SourceTypeIdentifier;
import org.radarcns.listener.managementportal.ManagementPortalClient;
import org.radarcns.management.service.dto.SourceDataDTO;
import org.radarcns.util.CachedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores all the sourceType-type definition and sourceType-data.
 */
public class SourceCatalog {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceCatalog.class);

    private CachedMap<SourceTypeIdentifier, SourceTypeDTO> sourceTypes;

    private CachedMap<String, SourceDataDTO> sourceData;

    private static final Duration CACHE_INVALIDATE_DEFAULT = Duration.ofMinutes(1);

    private static final Duration CACHE_RETRY_DEFAULT = Duration.ofHours(1);

    /**
     * Constructor to create SourceCatalog.
     *
     * @param managementPortalClient of the context
     */
    @Inject
    public SourceCatalog(ManagementPortalClient managementPortalClient) {
        LOGGER.debug("Initializing source catalogue");
        this.sourceTypes = new CachedMap<>(managementPortalClient::retrieveSourceTypes,
                SourceTypeDTO::getSourceTypeIdentifier,
                CACHE_INVALIDATE_DEFAULT, CACHE_RETRY_DEFAULT);
        this.sourceData = new CachedMap<>(managementPortalClient::retrieveSourceData,
                SourceDataDTO::getSourceDataName,
                CACHE_INVALIDATE_DEFAULT, CACHE_RETRY_DEFAULT);
    }

    /**
     * Retrieves all {@link SourceTypeDTO} from Management Portal using {@link ServletContext}
     * entity.
     *
     * @return {@link ArrayList} of {@link SourceTypeDTO} retrieved from the Management Portal
     */
    public List<SourceTypeDTO> getSourceTypes() throws IOException {
        return new ArrayList<>(sourceTypes.get().values());
    }

    /**
     * Retrieves a {@link SourceTypeDTO} from the Management Portal using {@link ServletContext}
     * entity.
     *
     * @param producer {@link String} of the Source-type that has to be retrieved
     * @param model {@link String} of the Source-type that has to be retrieved
     * @param catalogVersion {@link String} of the Source-type that has to be retrieved
     * @return {@link SourceTypeDTO} retrieved from the Management Portal
     */
    public SourceTypeDTO getSourceType(String producer, String model, String catalogVersion)
            throws NotFoundException, IOException {
        SourceTypeDTO result;
        try {
            result = sourceTypes.get(new SourceTypeIdentifier(producer, model,
                    catalogVersion));
        } catch (NoSuchElementException exe) {
            throw new NotFoundException("Cannot find sourceType-type of identifier "
                    + producer + ":" + model + ":" + catalogVersion, exe);
        }
        return result;
    }


    /**
     * Retrieves all {@link SourceDataDTO} from Management Portal using {@link ServletContext}
     * entity.
     *
     * @return {@link ArrayList} of {@link SourceDataDTO} retrieved from the Management Portal
     */
    public List<SourceDataDTO> getSourceData() throws IOException {
        return new ArrayList<>(sourceData.get().values());
    }

    /**
     * Retrieves a {@link SourceDataDTO} from the Management Portal using {@link ServletContext}
     * entity.
     *
     * @param sourceDataName {@link String} of the Source-Data that has to be retrieved
     * @return {@link SourceDataDTO} retrieved from the Management Portal
     */
    public SourceDataDTO getSourceData(String sourceDataName)
            throws NotFoundException, IOException {
        SourceDataDTO result = sourceData.get(sourceDataName);
        if (result == null) {
            result = sourceData.get(true).get(sourceDataName);
        }
        if (result == null) {
            throw new NotFoundException(
                    "Cannot find sourceType-data of identifier " + sourceDataName);
        }
        return result;
    }

    /**
     * Returns the supported sourceType type.
     *
     * @return a set containing all supported sourceType
     */
    public Set<String> getSupportedSourceTypes() throws IOException {
        return sourceTypes.get().keySet().stream().map(SourceTypeIdentifier::toString).collect(
                Collectors.toSet());
    }

    /**
     * Returns the supported sensor set.
     *
     * @return a set containing all supported sourceType
     */
    public Set<SourceDataDTO> getSupportedSensor() throws IOException {
        return new HashSet<>(sourceData.get().values());
    }


}
