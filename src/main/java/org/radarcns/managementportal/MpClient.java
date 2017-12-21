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

import static org.radarcns.webapp.util.BasePath.SUBJECTS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import javax.servlet.ServletContext;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.radarcns.config.managementportal.config.Properties;
import org.radarcns.listener.managementportal.listener.HttpClientListener;
import org.radarcns.listener.managementportal.listener.TokenManagerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client to interact with the RADAR Management Portal.
 */
public class MpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MpClient.class);

    private ArrayList<Subject> subjects;
    private ServletContext context;
    private boolean isSubjectsInitialised = false;

    /**
     * @param context {@link ServletContext} useful to retrieve shared {@link OkHttpClient} and
     *      {@code access token}.
     * @throws IllegalStateException in case the object cannot be created
     * @see org.radarcns.listener.managementportal.listener.HttpClientListener
     * @see org.radarcns.listener.managementportal.listener.TokenManagerListener
     */
    public MpClient(ServletContext context) {
        Objects.requireNonNull(context);

        // TODO Use try catch and initialize all

        this.context = context;

    }


    /**
     * Retrieves all {@link Subject} from the already computed list of subjects
     * using {@link ArrayList} of {@link Subject} else it calls a method for retrieving
     * the subjects from MP.
     * @return {@link ArrayList} of {@link Subject} if a subject is found
     */
    public ArrayList<Subject> getSubjects() {
        if ( isSubjectsInitialised ) {
            return subjects;
        } else {
            try {
                return getAllSubjects(context);
            } catch (MalformedURLException | URISyntaxException exc) {
                LOGGER.error(exc.getMessage());
            } catch (IllegalStateException exc) {
                LOGGER.error("Error : ", exc.fillInStackTrace());
            }
        }
        LOGGER.warn("Subjects cannot be retrieved");
        return null;
    }

    /**
     * Retrieves all {@link Subject} from Management Portal using {@link ServletContext} entity.
     * @param context {@link ServletContext} that has been used to authenticate token
     * @return {@link ArrayList} of {@link Subject} retrieved from the Management Portal
     * @throws MalformedURLException,URISyntaxException in case the subjects cannot be retrieved
     */
    private ArrayList<Subject> getAllSubjects(ServletContext context) throws
            MalformedURLException, URISyntaxException {

        Request request = getBuilder(Properties.getSubjectEndPoint(), context).get().build();

        ArrayList<Subject> allSubjects;

        try (Response response = HttpClientListener.getClient(context)
                .newCall(request).execute()) {
            if (response.isSuccessful()) {

                String jsonData = response.body().string();
                allSubjects = Subject.getAllSubjectsFromJson(jsonData);
                LOGGER.info("Retrieved Subjects from MP.");
                isSubjectsInitialised = true;
                return allSubjects;
            }
            LOGGER.info("Subjects is not present");
            return null;
        } catch (IOException exc) {
            throw new IllegalStateException("Subjects could not be retrieved", exc);
        }
    }

    /**
     * Retrieves a {@link Subject} from the already computed list of subjects
     * using {@link ArrayList} of {@link Subject} entity.
     * @param subjectId {@link String} that has to be searched.
     * @return {@link Subject} if a subject is found
     */
    public Subject getSubject(String subjectId) {
        if (isSubjectsInitialised) {
            Iterator<Subject> elements = subjects.iterator();

            while (elements.hasNext()) {
                Subject currentSubject = elements.next();
                if (subjectId.equals(currentSubject.getLogin())) {
                    return currentSubject;
                }
            }
        } else {
            try {
                return getSubject(subjectId, context);
            } catch (MalformedURLException | URISyntaxException exc) {
                LOGGER.error(exc.getMessage());
            } catch (IllegalStateException exc) {
                LOGGER.error("Error : ", exc.fillInStackTrace());
            }
        }
        LOGGER.info("Subject is not present");
        return null;
    }

    /**
     * Retrieves a {@link Subject} from the Management Portal using {@link ServletContext} entity.
     * @param context {@link ServletContext} that has been used to authenticate token
     * @param subjectId {@link String} of the Subject that has to be retrieved
     * @return {@link Subject} retrieved from the Management Portal
     * @throws MalformedURLException,URISyntaxException in case the subjects cannot be retrieved.
     */
    private Subject getSubject(String subjectId, ServletContext context) throws
            MalformedURLException, URISyntaxException {

        if (isSubjectsInitialised) {
            return getSubject(subjectId);
        }
        // TODO Use Login instead of Subject ID to get subjects from Management Portal.

        Request request = getBuilder(getUrl(Properties.getSubjectEndPoint(),
                subjectId), context).get().build();

        try (Response response = HttpClientListener.getClient(context)
                .newCall(request).execute()) {
            if (response.isSuccessful()) {
                Subject subject = Subject.getObject(response.body().string());
                LOGGER.info("Subject : " + subject.getJsonString());
                return subject;
            }
            LOGGER.info("Subject is not present");
            return null;
        } catch (IOException exc) {
            throw new IllegalStateException("Subject could not be retrieved", exc);
        }
    }

    /**
     * Retrieves all {@link Subject} from a study (or project) in the
     * Management Portal using {@link ServletContext} entity.
     * @param studyName {@link String} the study from which subjects to be retrieved
     * @return {@link ArrayList} of {@link Subject} retrieved from the Management Portal
     * @throws MalformedURLException,URISyntaxException in case the subjects cannot be retrieved.
     */
    public ArrayList<Subject> getAllSubjectsFromStudy(String studyName) throws
            MalformedURLException, URISyntaxException {

        LOGGER.info(studyName + context.getContextPath());
        Request request = getBuilder(getUrl(Properties.getProjectEndPoint(),
                studyName + "/" + SUBJECTS), context).get().build();

        ArrayList<Subject> allSubjects;

        try (Response response = HttpClientListener.getClient(context)
                .newCall(request).execute()) {
            if (response.isSuccessful()) {

                String jsonData = response.body().string();
                allSubjects = Subject.getAllSubjectsFromJson(jsonData);
                LOGGER.info("Retrieved Subjects from MP from Project " + studyName);
                return allSubjects;
            }
            LOGGER.info("Subjects is not present");
            return null;
        } catch (IOException exc) {
            throw new IllegalStateException("Subjects could not be retrieved from Project "
                    + studyName, exc);
        }
    }

    private URL getUrl(URL endPoint, String path) throws MalformedURLException {
        URL newUrl = new URL(endPoint + path);
        return newUrl;
    }

    private static Request.Builder getBuilder(URL url, ServletContext context) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer ".concat(
                        TokenManagerListener.getToken(context)));
    }

    /**
     * Retrieves all {@link Project} from Management Portal using {@link ServletContext} entity.
     * @param context {@link ServletContext} that has been used to authenticate token
     * @return {@link ArrayList} of {@link Project} retrieved from the Management Portal
     */
    public ArrayList<Project> getAllProjects(ServletContext context) throws
            MalformedURLException, URISyntaxException {
        Request request = getBuilder(Properties.getProjectEndPoint(), context).get().build();

        ArrayList<Project> allProjects;

        try (Response response = HttpClientListener.getClient(context)
                .newCall(request).execute()) {
            if (response.isSuccessful()) {
                allProjects = Project.getAllObjects(response);
                LOGGER.info("Retrieved Projects from MP.");
                LOGGER.info(allProjects.toString());
                return allProjects;
            }
            LOGGER.info("Projects not present");
            return null;
        } catch (IOException exc) {
            throw new IllegalStateException("Projects could not be retrieved", exc);
        }
    }

    /**
     * Retrieves a {@link Project} from the Management Portal using {@link ServletContext} entity.
     * @param context {@link ServletContext} that has been used to authenticate token
     * @param projectName {@link String} of the Project that has to be retrieved
     * @return {@link Project} retrieved from the Management Portal
     */
    public Project getProject(String projectName, ServletContext context) throws
            MalformedURLException, URISyntaxException {

        Request request = getBuilder(getUrl(Properties.getProjectEndPoint(), projectName),
                context).get().build();

        try (Response response = HttpClientListener.getClient(context)
                .newCall(request).execute()) {
            if (response.isSuccessful()) {
                Project project = Project.getObject(response.body().string());
                LOGGER.info("Project : " + project.toString());
                return project;
            }
            LOGGER.info("Subject is not present");
            return null;
        } catch (IOException exc) {
            throw new IllegalStateException("Subject could not be retrieved", exc);
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
