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
import javax.servlet.ServletContext;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.radarcns.config.ManagementPortalConfig;
import org.radarcns.config.Properties;
import org.radarcns.managementportal.Project;
import org.radarcns.managementportal.Subject;
import org.radarcns.oauth.OAuth2AccessTokenDetails;
import org.radarcns.producer.rest.RestClient;
import org.radarcns.util.CachedMap;
import org.radarcns.util.RadarConverter;
import org.radarcns.webapp.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client to interact with the RADAR Management Portal.
 */
public class ManagementPortalClient {
    private static final Logger logger = LoggerFactory.getLogger(ManagementPortalClient.class);

    private static final ObjectReader SUBJECT_LIST_READER = RadarConverter.readerForCollection(
            List.class, Subject.class);
    private static final ObjectReader PROJECT_LIST_READER = RadarConverter.readerForCollection(
            List.class, Project.class);
    private static final Duration CACHE_INVALIDATE_DEFAULT = Duration.ofMinutes(1);
    private static final Duration CACHE_RETRY_DEFAULT = Duration.ofHours(1);

    private final OkHttpClient client;

    private final CachedMap<String, Subject> subjects;
    private final CachedMap<String, Project> projects;

    private OAuth2AccessTokenDetails token;

    /**
     * Client to interact with the RADAR Management Portal.
     *
     * @param okHttpClient {@link OkHttpClient} to communicate to external web services
     * @throws IllegalStateException in case the object cannot be created
     * @see HttpClientListener
     * @see ManagementPortalClientManager
     */
    public ManagementPortalClient(@Nonnull OkHttpClient okHttpClient) {
        this.client = okHttpClient;

        Duration invalidate = CACHE_INVALIDATE_DEFAULT;
        Duration retry = CACHE_RETRY_DEFAULT;
        ManagementPortalConfig mpConfig = Properties.getApiConfig().getManagementPortalConfig();
        if (mpConfig != null) {
            invalidate = parseDuration(mpConfig.getCacheInvalidateDuration(), invalidate);
            retry = parseDuration(mpConfig.getCacheRetryDuration(), retry);
        }

        subjects = new CachedMap<>(this::retrieveSubjects, Subject::getId, invalidate, retry);
        projects = new CachedMap<>(this::retrieveProjects, Project::getProjectName,
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

    protected void updateToken(OAuth2AccessTokenDetails tokenDetails) {
        this.token = tokenDetails;
    }

    /**
     * Retrieves all {@link Subject} from the already computed list of subjects using {@link
     * ArrayList} of {@link Subject} else it calls a method for retrieving the subjects from MP.
     *
     * @return {@link ArrayList} of {@link Subject} if a subject is found
     */
    public Map<String, Subject> getSubjects() throws IOException {
        return subjects.get();
    }

    /**
     * Retrieves all {@link Subject} from Management Portal using {@link ServletContext} entity.
     *
     * @return subjects retrieved from the Management Portal
     */
    private List<Subject> retrieveSubjects() throws IOException {
        ManagementPortalConfig config = Properties.getApiConfig().getManagementPortalConfig();
        URL url = new URL(config.getManagementPortalUrl(), config.getSubjectEndpoint());
        Request getAllSubjectsRequest = this.buildGetRequest(url);
        try (Response response = this.client.newCall(getAllSubjectsRequest).execute()) {
            String responseBody = RestClient.responseBody(response);
            if (!response.isSuccessful()) {
                throw new IOException("Failed to retrieve all subjects: " + responseBody);
            }
            List<Subject> allSubjects = SUBJECT_LIST_READER.readValue(responseBody);
            logger.info("Retrieved Subjects from MP.");
            return allSubjects;
        }
    }

    /**
     * Retrieves a {@link Subject} from the already computed list of subjects using {@link
     * ArrayList} of {@link Subject} entity.
     *
     * @param subjectLogin {@link String} that has to be searched.
     * @return {@link Subject} if a subject is found
     */
    public Subject getSubject(@Nonnull String subjectLogin) throws IOException, NotFoundException {
        try {
            return subjects.get(subjectLogin);
        } catch (NoSuchElementException ex) {
            throw new NotFoundException("Subject " + subjectLogin + " not found.");
        }
    }

    /**
     * Retrieves all {@link Subject} from a study (or project) in the Management Portal using {@link
     * ServletContext} entity.
     *
     * @param projectName {@link String} the study from which subjects to be retrieved
     * @return {@link List} of {@link Subject} retrieved from the Management Portal
     * @throws MalformedURLException in case the subjects cannot be retrieved.
     */
    public List<Subject> getAllSubjectsFromProject(@Nonnull String projectName) throws
            IOException, NotFoundException {
        // will throw not found if relevant.
        getProject(projectName);

        List<Subject> result = subjects.get().values().stream()
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
     * Retrieves all {@link Project} from Management Portal using {@link ServletContext} entity.
     *
     * @return {@link ArrayList} of {@link Project} retrieved from the Management Portal
     */
    public Map<String, Project> getProjects() throws IOException {
        return projects.get();
    }

    /**
     * Retrieves all {@link Project} from Management Portal using {@link ServletContext} entity.
     *
     * @return projects retrieved from the management portal.
     */
    private List<Project> retrieveProjects() throws IOException {
        ManagementPortalConfig config = Properties.getApiConfig().getManagementPortalConfig();
        URL getAllProjectsUrl = new URL(config.getManagementPortalUrl(),
                config.getProjectEndpoint());
        Request getAllProjects = this.buildGetRequest(getAllProjectsUrl);
        try (Response response = this.client.newCall(getAllProjects).execute()) {
            String responseBody = RestClient.responseBody(response);
            if (!response.isSuccessful()) {
                throw new IOException("Failed to retrieve all subjects: " + responseBody);
            }
            List<Project> allProjects = PROJECT_LIST_READER.readValue(responseBody);
            logger.info("Retrieved Projects from MP");
            return allProjects;
        }
    }

    /**
     * Retrieves a {@link Project} from the Management Portal using {@link ServletContext} entity.
     *
     * @param projectName {@link String} of the Project that has to be retrieved
     * @return {@link Project} retrieved from the Management Portal
     */
    public Project getProject(String projectName) throws IOException, NotFoundException {
        try {
            return projects.get(projectName);
        } catch (NoSuchElementException ex) {
            throw new NotFoundException("Project " + projectName + " not found");
        }
    }

    private Request buildGetRequest(URL url) {
        return new Request.Builder()
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + this.token.getAccessToken())
                .url(url)
                .get()
                .build();
    }
}
