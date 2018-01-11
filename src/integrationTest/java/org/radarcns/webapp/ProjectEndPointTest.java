package org.radarcns.webapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.radarcns.webapp.util.BasePath.SUBJECTS;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import javax.ws.rs.core.Response.Status;
import okhttp3.Response;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.config.Properties;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.managementportal.Project;
import org.radarcns.managementportal.Subject;
import org.radarcns.webapp.util.BasePath;

public class ProjectEndPointTest {
    private static final String PROJECT_NAME = "radar";

    @Rule
    public final ApiClient apiClient = new ApiClient(
            Properties.getApiConfig().getApplicationConfig().getUrlString());

    private static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void getAllProjectsStatusTest200()
            throws IOException, ReflectiveOperationException, URISyntaxException {

        Response actual = apiClient.request(BasePath.PROJECT, "application/json",
                Status.OK);
        assertTrue(actual.isSuccessful());
        List<Project> projects = objectMapper.readValue(actual.body().string(), objectMapper
                    .getTypeFactory().constructCollectionType(List.class, Project.class));

        assertNotNull(projects);
        assertTrue(projects.size()>0);
    }

    @Test
    public void getProjectByProjectNameStatusTest200()
            throws IOException, ReflectiveOperationException, URISyntaxException {

        Response actual = apiClient
                .request(BasePath.PROJECT + "/" + PROJECT_NAME, "application/json",
                        Status.OK);
        assertTrue(actual.isSuccessful());
        Project project = objectMapper.readValue(actual.body().string(), Project.class);
        assertEquals(PROJECT_NAME, project.getProjectName());

    }

    @Test
    public void getProjectByUnavailableNameStatusTest404()
            throws IOException, ReflectiveOperationException, URISyntaxException {

        Response actual = apiClient
                .request(BasePath.PROJECT + "/" + "SOMETHING", "application/json",
                        Status.NOT_FOUND);
        assertFalse(actual.isSuccessful());
        assertEquals(actual.code(), Status.NOT_FOUND.getStatusCode());

    }


    @Test
    public void getSubjectsByProjectName200()
            throws IOException, ReflectiveOperationException, URISyntaxException {

        Response actual = apiClient
                .request(BasePath.PROJECT + "/" + PROJECT_NAME +"/" + SUBJECTS, "application/json",
                        Status.OK);
        assertTrue(actual.isSuccessful());
        List<Subject> subjects = objectMapper.readValue(actual.body().string(), objectMapper
                .getTypeFactory().constructCollectionType(List.class, Subject.class));

        assertNotNull(subjects);
        assertTrue(subjects.size()>0);

    }

}
