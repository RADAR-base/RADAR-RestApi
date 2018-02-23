package org.radarcns.webapp;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.radarcns.webapp.SampleDataHandler.PROJECT;

import com.fasterxml.jackson.databind.ObjectReader;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.core.Response.Status;
import okhttp3.Response;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.domain.managementportal.ProjectDTO;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RestApiDetails;
import org.radarcns.util.RadarConverter;
import org.radarcns.webapp.resource.BasePath;

public class ProjectEndPointTest {

    @Rule
    public final ApiClient apiClient = new ApiClient(
            RestApiDetails.getRestApiClientDetails().getApplicationConfig().getUrlString());

    @Test
    public void getAllProjectsStatusTest200() throws IOException {

        Response actual = apiClient.request(BasePath.PROJECTS, APPLICATION_JSON, Status.OK);
        assertTrue(actual.isSuccessful());
        ObjectReader reader = RadarConverter.readerForCollection(List.class, ProjectDTO.class);
        List<ProjectDTO> projects = reader.readValue(actual.body().byteStream());

        assertNotNull(projects);
        assertTrue(projects.size() > 0);
    }

    @Test
    public void getProjectByProjectNameStatusTest200() throws IOException {

        Response actual = apiClient
                .request(BasePath.PROJECTS + "/" + PROJECT, APPLICATION_JSON,
                        Status.OK);
        assertTrue(actual.isSuccessful());
        ObjectReader reader = RadarConverter.readerFor(ProjectDTO.class);
        ProjectDTO project = reader.readValue(actual.body().byteStream());
        assertEquals(PROJECT, project.getProjectName());

    }

    @Test
    public void getProjectByUnavailableNameStatusTest404() throws IOException {

        Response actual = apiClient
                .request(BasePath.PROJECTS + "/" + "SOMETHING", APPLICATION_JSON,
                        Status.NOT_FOUND);
        assertFalse(actual.isSuccessful());
        assertEquals(actual.code(), Status.NOT_FOUND.getStatusCode());

    }


}
