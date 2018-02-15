/*
 * Copyright 2017 King's College London
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

import com.fasterxml.jackson.databind.ObjectReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.NotFoundException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.radarcns.config.ManagementPortalConfig;
import org.radarcns.config.Properties;
import org.radarcns.domain.managementportal.SourceDTO;
import org.radarcns.domain.managementportal.SourceTypeDTO;
import org.radarcns.domain.managementportal.SubjectDTO;
import org.radarcns.exception.TokenException;
import org.radarcns.management.service.dto.ProjectDTO;
import org.radarcns.management.service.dto.SourceDataDTO;
import org.radarcns.oauth.OAuth2Client;
import org.radarcns.producer.rest.RestClient;
import org.radarcns.util.CachedMap;
import org.radarcns.util.RadarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client to interact with the RADAR Management Portal. This class is thread-safe.
 */
public class ManagementPortalClient {

    private static final Logger logger = LoggerFactory.getLogger(ManagementPortalClient.class);

    private static final ObjectReader SUBJECT_LIST_READER = RadarConverter.readerForCollection(
            List.class, SubjectDTO.class);
    private static final ObjectReader PROJECT_LIST_READER = RadarConverter.readerForCollection(
            List.class, ProjectDTO.class);
    private static final ObjectReader SOURCE_TYPE_LIST_READER = RadarConverter.readerForCollection(
            List.class, SourceTypeDTO.class);
    private static final ObjectReader SOURCE_DATA_LIST_READER = RadarConverter.readerForCollection(
            List.class, SourceDataDTO.class);

    private static final ObjectReader SOURCE_LIST_READER = RadarConverter.readerForCollection(
            List.class, SourceDTO.class);

    private static final Duration CACHE_INVALIDATE_DEFAULT = Duration.ofMinutes(1);
    private static final Duration CACHE_RETRY_DEFAULT = Duration.ofHours(1);

    private final OkHttpClient client;
    private final OAuth2Client oauthClient;

    private final CachedMap<String, SubjectDTO> subjects;
    private final CachedMap<String, ProjectDTO> projects;
    private final CachedMap<String, SourceDTO> sources;


    /**
     * Client to interact with the RADAR Management Portal.
     *
     * @param okHttpClient {@link OkHttpClient} to communicate to external web services
     * @throws IllegalStateException in case the object cannot be created
     */
    @Inject
    public ManagementPortalClient(OkHttpClient okHttpClient) {
        this.client = okHttpClient;

        Duration invalidate = CACHE_INVALIDATE_DEFAULT;
        Duration retry = CACHE_RETRY_DEFAULT;
        ManagementPortalConfig mpConfig = Properties.getApiConfig().getManagementPortalConfig();

        if (mpConfig == null) {
            throw new IllegalStateException("ManagementPortal configuration not set");
        }

        invalidate = parseDuration(mpConfig.getCacheInvalidateDuration(), invalidate);
        retry = parseDuration(mpConfig.getCacheRetryDuration(), retry);

        try {
            oauthClient = new OAuth2Client.Builder()
                    .endpoint(mpConfig.getManagementPortalUrl(), mpConfig.getTokenEndpoint())
                    .credentials(mpConfig.getOauthClientId(), mpConfig.getOauthClientSecret())
                    .httpClient(client)
                    .build();
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("Failed to construct MP URL", ex);
        }

        subjects = new CachedMap<>(this::retrieveSubjects, SubjectDTO::getLogin, invalidate,
                retry);
        projects = new CachedMap<>(this::retrieveProjects, ProjectDTO::getProjectName,
                invalidate, retry);
        sources = new CachedMap<>(this::retrieveSources, SourceDTO::getSourceIdentifier,
                invalidate, retry);
    }

    private static Duration parseDuration(String duration, Duration defaultValue) {
        if (duration != null) {
            try {
                return Duration.parse(duration);
            } catch (DateTimeParseException ex) {
                logger.warn("Management Portal cache duration {} is invalid."
                        + " Use ISO 8601 duration format, e.g.,"
                        + " PT1M for one minute or PT1H for one hour.", duration);
            }
        }
        return defaultValue;
    }

    private String getToken() throws IOException {
        try {
            return oauthClient.getValidToken(Duration.ofSeconds(30)).getAccessToken();
        } catch (TokenException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Retrieves all {@link SubjectDTO} from the already computed list of SubjectDTOs using {@link
     * ArrayList} of {@link SubjectDTO} else it calls a method for retrieving the SubjectDTOs from
     * MP.
     *
     * @return {@link ArrayList} of {@link SubjectDTO} if a SubjectDTO is found
     */
    public Map<String, SubjectDTO> getSubjects() throws IOException {
        return subjects.get();
    }

    /**
     * Retrieves all {@link SubjectDTO} from Management Portal using {@link ServletContext} entity.
     *
     * @return SubjectDTOs retrieved from the Management Portal
     */
    private List<SubjectDTO> retrieveSubjects() throws IOException {
        ManagementPortalConfig config = Properties.getApiConfig().getManagementPortalConfig();
        URL url = new URL(config.getManagementPortalUrl(), config.getSubjectEndpoint());
        Request getAllSubjectsRequest = this.buildGetRequest(url);
        try (Response response = this.client.newCall(getAllSubjectsRequest).execute()) {
            String responseBody = RestClient.responseBody(response);
            if (!response.isSuccessful()) {
                throw new IOException("Failed to retrieve all SubjectDTOs: " + responseBody);
            }
            List<SubjectDTO> allSubjects = SUBJECT_LIST_READER.readValue(responseBody);
            logger.info("Retrieved SubjectDTOs from MP.");
            return allSubjects;
        }
    }

    /**
     * Retrieves a {@link SubjectDTO} from the already computed list of SubjectDTOs using {@link
     * ArrayList} of {@link SubjectDTO} entity.
     *
     * @param subjectLogin {@link String} that has to be searched.
     * @return {@link SubjectDTO} if a SubjectDTO is found
     * @throws IOException if the SubjectDTOs cannot be refreshed
     * @throws NotFoundException if the SubjectDTO is not found
     */
    public SubjectDTO getSubject(@Nonnull String subjectLogin)
            throws IOException, NotFoundException {
        try {
            return subjects.get(subjectLogin);
        } catch (NoSuchElementException ex) {
            throw new NotFoundException("SubjectDTO " + subjectLogin + " not found.");
        }
    }

    /**
     * Checks whether given SubjectDTO is part of given project.
     *
     * @param projectName project that should contain the SubjectDTO.
     * @param subjectLogin login name that has to be searched.
     * @throws IOException if the list of SubjectDTOs cannot be refreshed.
     * @throws NotFoundException if the SubjectDTO is not found in given project.
     */
    public void checkSubjectInProject(@Nonnull String projectName, @Nonnull String subjectLogin)
            throws IOException, NotFoundException {
        SubjectDTO subject = getSubject(subjectLogin);
        if (!projectName.equals(subject.getProject().getProjectName())) {
            throw new NotFoundException(
                    "SubjectDTO " + subjectLogin + " is not part of project " + projectName
                            + ".");
        }
    }


    /**
     * Retrieves all {@link SubjectDTO} from a study (or project) in the Management Portal using
     * {@link ServletContext} entity.
     *
     * @param projectName {@link String} the study from which SubjectDTOs to be retrieved
     * @return {@link List} of {@link SubjectDTO} retrieved from the Management Portal
     */
    public List<SubjectDTO> getAllSubjectsFromProject(@Nonnull String projectName)
            throws IOException {
        // will throw not found if relevant.
        getProject(projectName);

        List<SubjectDTO> result = subjects.get().values().stream()
                .filter(s -> projectName.equals(s.getProject().getProjectName()))
                .collect(Collectors.toList());

        if (result.isEmpty() && subjects.mayRetry()) {
            result = subjects.get(true).values().stream()
                    .filter(s -> projectName.equals(s.getProject().getProjectName()))
                    .collect(Collectors.toList());
        }

        return result;
    }

    /**
     * Retrieves all {@link ProjectDTO} from Management Portal using {@link ServletContext} entity.
     *
     * @return {@link ArrayList} of {@link ProjectDTO} retrieved from the Management Portal
     * @throws IOException if the list of projects cannot be retrieved.
     */
    public Map<String, ProjectDTO> getProjects() throws IOException {
        return projects.get();
    }

    /**
     * Retrieves all {@link ProjectDTO} from Management Portal using {@link ServletContext} entity.
     *
     * @return projects retrieved from the management portal.
     */
    private List<ProjectDTO> retrieveProjects() throws IOException {
        ManagementPortalConfig config = Properties.getApiConfig().getManagementPortalConfig();
        URL getAllProjectsUrl = new URL(config.getManagementPortalUrl(),
                config.getProjectEndpoint());
        Request getAllProjects = this.buildGetRequest(getAllProjectsUrl);
        try (Response response = this.client.newCall(getAllProjects).execute()) {
            String responseBody = RestClient.responseBody(response);
            if (!response.isSuccessful()) {
                throw new IOException("Failed to retrieve all SubjectDTOs: " + responseBody);
            }
            List<ProjectDTO> allProjects = PROJECT_LIST_READER.readValue(responseBody);
            logger.info("Retrieved ProjectDTOs from MP");
            return allProjects;
        }
    }

    /**
     * Retrieves a {@link ProjectDTO} from the Management Portal using {@link ServletContext}
     * entity.
     *
     * @param projectName {@link String} of the ProjectDTO that has to be retrieved
     * @return {@link ProjectDTO} retrieved from the Management Portal
     * @throws IOException if the list of projects cannot be retrieved
     * @throws NotFoundException if given project is not found
     */
    public ProjectDTO getProject(String projectName) throws IOException, NotFoundException {
        try {
            return projects.get(projectName);
        } catch (NoSuchElementException ex) {
            throw new NotFoundException("ProjectDTO " + projectName + " not found");
        }
    }

    /**
     * Retrieves all {@link SourceTypeDTO} from Management Portal using {@link ServletContext}
     * entity.
     *
     * @return source-types retrieved from the management portal.
     */
    public List<SourceTypeDTO> retrieveSourceTypes() throws IOException {
        ManagementPortalConfig config = Properties.getApiConfig().getManagementPortalConfig();
        URL getAllSourceTypesUrl = new URL(config.getManagementPortalUrl(),
                config.getSourceTypeEndpoint());
        Request getAllSourceTypes = this.buildGetRequest(getAllSourceTypesUrl);
        try (Response response = this.client.newCall(getAllSourceTypes).execute()) {
            String responseBody = RestClient.responseBody(response);
            if (!response.isSuccessful()) {
                throw new IOException("Failed to retrieve all sourceType-types: " + responseBody);
            }
            List<SourceTypeDTO> allSourceTypes = SOURCE_TYPE_LIST_READER.readValue(responseBody);
            logger.info("Retrieved SourceDTOTypes from MP");
            return allSourceTypes;
        }
    }

    /**
     * Retrieves all {@link org.radarcns.management.service.dto.SourceDataDTO} from Management
     * Portal using {@link ServletContext} entity.
     *
     * @return sourceType-types retrieved from the management portal.
     */
    public List<SourceDataDTO> retrieveSourceData() throws IOException {
        ManagementPortalConfig config = Properties.getApiConfig().getManagementPortalConfig();
        URL getAllSourceTypesUrl = new URL(config.getManagementPortalUrl(),
                config.getSourceDataEndpoint());
        Request getAllSourceTypes = this.buildGetRequest(getAllSourceTypesUrl);
        try (Response response = this.client.newCall(getAllSourceTypes).execute()) {
            String responseBody = RestClient.responseBody(response);
            if (!response.isSuccessful()) {
                throw new IOException("Failed to retrieve all sourceType-data: " + responseBody);
            }
            List<SourceDataDTO> allSourceData = SOURCE_DATA_LIST_READER.readValue(responseBody);
            logger.info("Retrieved SourceDTOTypes from MP");
            return allSourceData;
        }
    }

    /**
     * Retrieves all {@link SourceDTO} from the already computed list of sources using {@link
     * ArrayList} of {@link SourceDTO} else it calls a method for retrieving the SourceDTO from MP.
     *
     * @return {@link ArrayList} of {@link SourceDTO} if a SubjectDTO is found
     */
    public Map<String, SourceDTO> getSources() throws IOException {
        return sources.get();
    }

    /**
     * Retrieves all {@link SourceDTO} from Management Portal using {@link ServletContext} entity.
     *
     * @return SourceDTO retrieved from the Management Portal
     */
    private List<SourceDTO> retrieveSources() throws IOException {
        ManagementPortalConfig config = Properties.getApiConfig().getManagementPortalConfig();
        URL url = new URL(config.getManagementPortalUrl(), config.getSourceEndpoint());
        Request getAllSourcesRequest = this.buildGetRequest(url);
        try (Response response = this.client.newCall(getAllSourcesRequest).execute()) {
            String responseBody = RestClient.responseBody(response);
            if (!response.isSuccessful()) {
                throw new IOException("Failed to retrieve all sources: " + responseBody);
            }
            List<SourceDTO> sources = SOURCE_LIST_READER.readValue(responseBody);
            logger.info("Retrieved SourceDTOs from MP.");
            return sources;
        }
    }

    /**
     * Retrieves a {@link SourceDTO} from the already computed list of SubjectDTOs using {@link
     * ArrayList} of {@link SourceDTO} entity.
     *
     * @param sourceId {@link String} that has to be searched.
     * @return {@link SourceDTO} if a SubjectDTO is found
     * @throws IOException if the SubjectDTOs cannot be refreshed
     * @throws NotFoundException if the SubjectDTO is not found
     */
    public SourceDTO getSource(@Nonnull String sourceId) throws IOException, NotFoundException {
        try {
            return sources.get(sourceId);
        } catch (NoSuchElementException ex) {
            throw new NotFoundException("SourceDTO " + sourceId + " not found.");
        }
    }

    private Request buildGetRequest(URL url) throws IOException {
        return new Request.Builder()
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + getToken())
                .url(url)
                .get()
                .build();
    }
}
