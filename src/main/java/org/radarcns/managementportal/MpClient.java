package org.radarcns.managementportal;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.radarcns.config.managementportal.config.Properties;
import org.radarcns.listener.managementportal.listener.HttpClientListener;
import org.radarcns.listener.managementportal.listener.TokenManagerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

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

/**
 * Client to interact with the RADAR Management Portal.
 */
public class MpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MpClient.class);

    private ArrayList<Subject> subjects;
    private ServletContext context;
    private boolean SUBJECTS_INITIALIZED = false;

    /**
     * @param context {@link ServletContext} useful to retrieve shared {@link OkHttpClient} and
     *      {@code access token}
     * @throws IllegalStateException in case the object cannot be created
     * @see org.radarcns.listener.managementportal.listener.HttpClientListener
     * @see org.radarcns.listener.managementportal.listener.TokenManagerListener
     */
    public MpClient(ServletContext context) {
        Objects.requireNonNull(context);

        // TODO Use try catch and initialize all

        this.context = context;

        try {
            subjects = getAllSubjects(context);
            getSubject("1",context);
        } catch (MalformedURLException exc){
            LOGGER.error(exc.getMessage());
        } catch (URISyntaxException exc){
            LOGGER.error(exc.getMessage());
        } catch (IllegalStateException exc) {
            LOGGER.error("Error : ", exc.fillInStackTrace());
        }

    }

    public ArrayList<Subject> getSubjects() {
        if(SUBJECTS_INITIALIZED) {
            return subjects;
        }else {
            try {
                return getAllSubjects(context);
            } catch (MalformedURLException exc){
                LOGGER.error(exc.getMessage());
            } catch (URISyntaxException exc){
                LOGGER.error(exc.getMessage());
            } catch (IllegalStateException exc) {
                LOGGER.error("Error : ", exc.fillInStackTrace());
            }
        }
        LOGGER.warn("Subjects cannot be retrieved");
        return null;
    }

    /**
     * Retrieves all {@link Subject} from the Management Portal using {@link ServletContext} entity.
     * @param context {@link ServletContext} that has been used to authenticate token
     * @return {@link ArrayList<Subject>} retrieved from the Management Portal
     * @throws MalformedURLException,URISyntaxException in case the subjects cannot be retrieved.
     */
    private ArrayList<Subject> getAllSubjects(ServletContext context) throws MalformedURLException,
            URISyntaxException {

        Request request = getBuilder(Properties.getSubjectEndPoint(), context).get().build();

        ArrayList<Subject> allSubjects;

        try (Response response = HttpClientListener.getClient(context).newCall(request).execute()) {
            if (response.isSuccessful()) {

                String jsonData = response.body().string();
                allSubjects = Subject.getAllSubjectsFromJson(jsonData);
                LOGGER.info("Retrieved Subjects from MP.");
                SUBJECTS_INITIALIZED = true;
                response.close();
                return allSubjects;
            }
            LOGGER.info("Subjects is not present");
            return null;
        } catch (IOException exc) {
            throw new IllegalStateException("Subjects could not be retrieved", exc);
        }
    }

    /**
     * Retrieves a {@link Subject} from the already computed list of subjects using {@link ArrayList<Subject>} entity.
     * @param subjectId {@link String} that has to be searched.
     * @return {@link Subject} if a subject is found
     */
    public Subject getSubject(String subjectId){
        if(SUBJECTS_INITIALIZED){
            Iterator<Subject> elements = subjects.iterator();

            while (elements.hasNext()) {
                Subject currentSubject = elements.next();
                if (subjectId.equals(currentSubject.getLogin())) {
                    return currentSubject;
                }
            }
        } else {
            try{
                subjects = getAllSubjects(context);
                SUBJECTS_INITIALIZED = true;
                getSubject(subjectId);
            } catch (MalformedURLException exc){
                LOGGER.error(exc.getMessage());
            } catch (URISyntaxException exc){
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
    private Subject getSubject(String subjectId, ServletContext context) throws MalformedURLException,
            URISyntaxException {

        if (SUBJECTS_INITIALIZED)
            return getSubject(subjectId);

        // TODO Use Login instead of Subject ID to get subjects from Management Portal.

        Request request = getBuilder(getUrl(Properties.getSubjectEndPoint(), subjectId), context).get().build();

        try (Response response = HttpClientListener.getClient(context).newCall(request).execute()) {
            if (response.isSuccessful()) {
                Subject subject = Subject.getObject(response.body().string());
                //Subject subject = Subject.createSubjectFromJson(response.body().string());
                LOGGER.info("Subject : " + subject.getJsonString());
                response.close();
                return subject;
            }
            LOGGER.info("Subject is not present");
            return null;
        } catch (IOException exc) {
            throw new IllegalStateException("Subject could not be retrieved", exc);
        }
    }

    /**
     * Retrieves all {@link Subject} from a study (or project) {@param studyId} in the Management Portal using {@link ServletContext} entity.
     * @param studyId {@link Integer} the study from which subjects to be retrieved
     * @return {@link ArrayList<Subject>} retrieved from the Management Portal
     * @throws MalformedURLException,URISyntaxException in case the subjects cannot be retrieved.
     */
    public ArrayList<Subject> getAllSubjectsFromStudy(Integer studyId){
        LOGGER.info(studyId + context.getContextPath());

        if(SUBJECTS_INITIALIZED) {
            return findSubjectsInProject(subjects,studyId);
        } else {
            try{
                ArrayList<Subject> allSubjects = getAllSubjects(context);
                return findSubjectsInProject(allSubjects , studyId);
            } catch (MalformedURLException exc){
                LOGGER.error(exc.getMessage());
            } catch (URISyntaxException exc){
                LOGGER.error(exc.getMessage());
            } catch (IllegalStateException exc) {
                LOGGER.error("Error : ", exc.fillInStackTrace());
            }
        }
        return null;
    }

    /**
     * Retrieves all {@link Subject} from a list of subjects having the same projectId.
     * @param projectId {@link Integer} that has to be searched
     * @return {@link ArrayList<Subject>} retrieved from the Management Portal
     * @throws MalformedURLException,URISyntaxException in case the subjects cannot be retrieved.
     */
    private ArrayList<Subject> findSubjectsInProject(ArrayList<Subject> subjects, Integer projectId) {
        ArrayList<Subject> subjectsInProject = new ArrayList<>();
        Iterator<Subject> elements = subjects.iterator();

        while (elements.hasNext()) {
            Subject currentSubject = elements.next();
            if (projectId.intValue() == currentSubject.getProject().getId().intValue()) {
                subjectsInProject.add(currentSubject);
            }
        }
        LOGGER.info("Subjects Retrieved from Study ID " + projectId);
        return subjectsInProject;
    }

    private URL getUrl(URL endPoint, String subjectId) throws MalformedURLException {
        URL subjectUrl = new URL(endPoint + subjectId);
        return subjectUrl;
    }

    private static Request.Builder getBuilder(URL url, ServletContext context) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer ".concat(
                        TokenManagerListener.getToken(context)));
    }

    /**
     * Retrieves all {@link Project} from the Management Portal using {@link ServletContext} entity.
     * @param context {@link ServletContext} that has been used to authenticate token
     * @return {@link ArrayList<Project>} retrieved from the Management Portal
     * @throws MalformedURLException,URISyntaxException in case the subjects cannot be retrieved.
     */
    public ArrayList<Project> getAllProjects(ServletContext context) throws MalformedURLException, URISyntaxException {
        Request request = getBuilder(Properties.getProjectEndPoint(), context).get().build();

        ArrayList<Project> allProjects;

        try (Response response = HttpClientListener.getClient(context).newCall(request).execute()) {
            if (response.isSuccessful()) {
                LOGGER.info("Response : " + response.body().string());
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
     * @param projectId {@link String} of the Project that has to be retrieved
     * @return {@link Subject} retrieved from the Management Portal
     * @throws MalformedURLException,URISyntaxException in case the subjects cannot be retrieved.
     */
    public Project getProject(String projectId, ServletContext context) throws MalformedURLException,
            URISyntaxException {

        Request request = getBuilder(getUrl(Properties.getProjectEndPoint(), projectId), context).get().build();

        try (Response response = HttpClientListener.getClient(context).newCall(request).execute()) {
            if (response.isSuccessful()) {
                Project project = Project.getObject(response.body().string());
                LOGGER.info("Project : " + project.toString());
                response.close();
                return project;
            }
            LOGGER.info("Subject is not present");
            return null;
        } catch (IOException exc) {
            throw new IllegalStateException("Subject could not be retrieved", exc);
        }
    }



    public static javax.ws.rs.core.Response getJsonResponse(Object obj) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        JsonNode toJson = objectMapper.valueToTree(obj);

        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.OK;
        return javax.ws.rs.core.Response.status(status.getStatusCode()).entity(toJson).build();
    }

}
