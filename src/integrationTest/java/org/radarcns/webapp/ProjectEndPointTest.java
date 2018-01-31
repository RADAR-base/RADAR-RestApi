package org.radarcns.webapp;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.radarcns.webapp.util.BasePath.SUBJECTS;

import com.fasterxml.jackson.databind.ObjectReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import javax.ws.rs.core.Response.Status;
import okhttp3.Response;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RestApiDetails;
import org.radarcns.managementportal.Project;
import org.radarcns.managementportal.Subject;
import org.radarcns.util.RadarConverter;
import org.radarcns.webapp.util.BasePath;

public class ProjectEndPointTest {

    private static final String PROJECT_NAME = "radar";

    @Rule
    public final ApiClient apiClient = new ApiClient(
            RestApiDetails.getRestApiClientDetails().getApplicationConfig().getUrlString());

    @Test
    public void getAllProjectsStatusTest200()
            throws IOException, URISyntaxException {

        Response actual = apiClient.request(BasePath.PROJECTS, APPLICATION_JSON, Status.OK);
        assertTrue(actual.isSuccessful());
        ObjectReader reader = RadarConverter.readerForCollection(List.class, Project.class);
        List<Project> projects = reader.readValue(actual.body().byteStream());

        assertNotNull(projects);
        assertTrue(projects.size() > 0);
    }

    @Test
    public void getProjectByProjectNameStatusTest200()
            throws IOException, ReflectiveOperationException, URISyntaxException {

        Response actual = apiClient
                .request(BasePath.PROJECTS + "/" + PROJECT_NAME, APPLICATION_JSON,
                        Status.OK);
        assertTrue(actual.isSuccessful());
        ObjectReader reader = RadarConverter.readerFor(Project.class);
        Project project = reader.readValue(actual.body().byteStream());
        assertEquals(PROJECT_NAME, project.getProjectName());

    }

    @Test
    public void getProjectByUnavailableNameStatusTest404()
            throws IOException, ReflectiveOperationException, URISyntaxException {

        Response actual = apiClient
                .request(BasePath.PROJECTS + "/" + "SOMETHING", APPLICATION_JSON,
                        Status.NOT_FOUND);
        assertFalse(actual.isSuccessful());
        assertEquals(actual.code(), Status.NOT_FOUND.getStatusCode());

    }


    @Test
    public void getSubjectsByProjectName200()
            throws IOException, ReflectiveOperationException, URISyntaxException {

        Response actual = apiClient
                .request(BasePath.PROJECTS + "/" + PROJECT_NAME + "/" + SUBJECTS, APPLICATION_JSON,
                        Status.OK);
        assertTrue(actual.isSuccessful());

        ObjectReader reader = RadarConverter.readerForCollection(List.class, Subject.class);
        List<Subject> subjects = reader.readValue(actual.body().byteStream());

        assertNotNull(subjects);
        assertTrue(subjects.size() > 0);

    }

}
