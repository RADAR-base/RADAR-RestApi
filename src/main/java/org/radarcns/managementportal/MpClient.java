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

package org.radarcns.managementportal;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static org.radarcns.config.managementportal.Properties.validateMpUrl;
import static org.radarcns.webapp.util.BasePath.SUBJECTS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.radarcns.config.managementportal.Properties;
import org.radarcns.listener.managementportal.HttpClientListener;
import org.radarcns.listener.managementportal.TokenManagerListener;
import org.radarcns.producer.rest.RestClient;
import org.radarcns.producer.rest.RestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client to interact with the RADAR Management Portal.
 */
public class MpClient {

    private static final Logger logger = LoggerFactory.getLogger(MpClient.class);
    private final RestClient client;

    private ArrayList<Subject> subjects;

    /**
     * Client to interact with the RADAR Management Portal.
     * @param context {@link ServletContext} useful to retrieve shared {@link OkHttpClient} and
     *      {@code access token}.
     * @throws IllegalStateException in case the object cannot be created
     * @see HttpClientListener
     * @see TokenManagerListener
     */
    public MpClient(ServletContext context) {
        Objects.requireNonNull(context);

        try {
            this.client = HttpClientListener.getRestClient(context, validateMpUrl().toString());
        } catch (MalformedURLException ex) {
            throw new AssertionError("ManagementPortal URL is invalid");
        }

        subjects = null;
    }

    /**
     * Retrieves all {@link Subject} from the already computed list of subjects
     * using {@link ArrayList} of {@link Subject} else it calls a method for retrieving
     * the subjects from MP.
     * @return {@link ArrayList} of {@link Subject} if a subject is found
     */
    public ArrayList<Subject> getSubjects() {
        if (subjects == null) {
            subjects = getAllSubjects();
        }
        return subjects;
    }

    /**
     * Retrieves all {@link Subject} from Management Portal using {@link ServletContext} entity.
     * @return {@link ArrayList} of {@link Subject} retrieved from the Management Portal
     */
    private ArrayList<Subject> getAllSubjects() {
        ArrayList<Subject> allSubjects;

        try {
            Request request = client.requestBuilder(Properties.getSubjectPath()).build();
            String jsonData = client.requestString(request);

            allSubjects = Subject.getAllSubjectsFromJson(jsonData);
            logger.info("Retrieved Subjects from MP.");
            return allSubjects;
        } catch (MalformedURLException ex) {
            logger.error("Invalid URL {}{}",
                    client.getConfig(), Properties.getSubjectPath());
        } catch (IOException exc) {
            logger.error("Subjects could not be retrieved", exc);
        }
        return null;
    }

    /**
     * Retrieves a {@link Subject} from the already computed list of subjects
     * using {@link ArrayList} of {@link Subject} entity.
     * @param subjectLogin {@link String} that has to be searched.
     * @return {@link Subject} if a subject is found
     */
    public Subject getSubject(@Nonnull String subjectLogin) {
        if (subjects != null) {
            for (Subject currentSubject : subjects) {
                if (subjectLogin.equals(currentSubject.getLogin())) {
                    return currentSubject;
                }
            }
        } else {
            try {
                return retrieveSubject(subjectLogin);
            } catch (IOException exc) {
                logger.error("Error : ", exc.fillInStackTrace());
            }
        }
        logger.info("Subject could not be retrieved.");
        return null;
    }

    /**
     * Retrieves a {@link Subject} from the Management Portal using {@link ServletContext} entity.
     * @param subjectLogin {@link String} of the Subject that has to be retrieved
     * @return {@link Subject} retrieved from the Management Portal
     * @throws MalformedURLException,URISyntaxException in case the subjects cannot be retrieved.
     */
    private Subject retrieveSubject(@Nonnull String subjectLogin) throws IOException {
        if (subjects != null) {
            return getSubject(subjectLogin);
        }

        try (Response response = client.request(Properties.getSubjectPath() + subjectLogin)) {
            String jsonData = RestClient.responseBody(response);

            if (response.isSuccessful()) {
                Subject subject = Subject.getObject(jsonData);
                logger.info("Subject : " + subject.getJsonString());
                return subject;
            } else if (response.code() == HTTP_NOT_FOUND) {
                logger.info("Subject {} is not present", subjectLogin);
                return null;
            } else {
                throw new RestException(response.code(), jsonData == null ? "" : jsonData);
            }
        }
    }

    /**
     * Retrieves all {@link Subject} from a study (or project) in the
     * Management Portal using {@link ServletContext} entity.
     * @param studyName {@link String} the study from which subjects to be retrieved
     * @return {@link List} of {@link Subject} retrieved from the Management Portal
     * @throws MalformedURLException,URISyntaxException in case the subjects cannot be retrieved.
     */
    public List<Subject> getAllSubjectsFromStudy(@Nonnull String studyName) throws
            IOException {

        if (subjects != null) {
            return subjects.stream()
                    .filter(s -> studyName.equals(s.getProject().getProjectName()))
                    .collect(Collectors.toList());
        }

        try (Response response = client.request(
                Properties.getProjectPath() + studyName + '/' + SUBJECTS)) {

            String jsonData = RestClient.responseBody(response);
            if (response.isSuccessful()) {
                List<Subject> allSubjects = Subject.getAllSubjectsFromJson(jsonData);
                logger.info("Retrieved Subjects from MP from Project " + studyName);
                return allSubjects;
            } else if (response.code() == HTTP_NOT_FOUND) {
                logger.info("Subjects for study {} are not present", studyName);
                return null;
            } else {
                throw new RestException(response.code(), jsonData == null ? "" : jsonData);
            }

        }
    }

    /**
     * Retrieves all {@link Project} from Management Portal using {@link ServletContext} entity.
     * @return {@link ArrayList} of {@link Project} retrieved from the Management Portal
     */
    public List<Project> getAllProjects() throws
            MalformedURLException, URISyntaxException {
        try (Response response = client.request(Properties.getProjectPath())) {
            String jsonData = RestClient.responseBody(response);
            if (response.isSuccessful()) {
                List<Project> allProjects = Project.getAllObjects(jsonData);
                logger.info("Retrieved Projects from MP");
                return allProjects;
            } else if (response.code() == HTTP_NOT_FOUND) {
                logger.info("Projects are not present");
                return null;
            } else {
                throw new RestException(response.code(), jsonData == null ? "" : jsonData);
            }
        } catch (IOException exc) {
            throw new IllegalStateException("Projects could not be retrieved", exc);
        }
    }

    /**
     * Retrieves a {@link Project} from the Management Portal using {@link ServletContext} entity.
     * @param projectName {@link String} of the Project that has to be retrieved
     * @return {@link Project} retrieved from the Management Portal
     */
    public Project getProject(String projectName) throws IOException {

        try (Response response = client.request(Properties.getProjectPath() + projectName)) {
            String jsonData = RestClient.responseBody(response);
            if (response.isSuccessful()) {
                Project project = Project.getObject(jsonData);
                logger.info("Retrieved project {} from MP", projectName);
                return project;
            } else if (response.code() == HTTP_NOT_FOUND) {
                logger.info("Project {} is not present", projectName);
                return null;
            } else {
                throw new RestException(response.code(), jsonData == null ? "" : jsonData);
            }
        }
    }

    /**
     * Creates a {@link Response} entity from a provided {@link Object}.
     */
    public static javax.ws.rs.core.Response getJsonResponse(Object obj) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        JsonNode toJson = objectMapper.valueToTree(obj);

        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.OK;
        return javax.ws.rs.core.Response.status(status.getStatusCode()).entity(toJson).build();
    }

}
