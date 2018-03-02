package org.radarcns.webapp;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.radarcns.webapp.SampleDataHandler.PROJECT;

import java.io.IOException;
import java.util.List;
import javax.ws.rs.core.Response.Status;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.domain.managementportal.ProjectDTO;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RestApiDetails;
import org.radarcns.webapp.resource.BasePath;

public class ProjectEndPointTest {

    @Rule
    public final ApiClient apiClient = new ApiClient(
            RestApiDetails.getRestApiClientDetails().getApplicationConfig().getUrlString());

    @Test
    public void getAllProjectsStatusTest200() throws IOException {
        List<ProjectDTO> projects = apiClient.getJsonList(
                BasePath.PROJECTS, ProjectDTO.class, Status.OK);
        assertNotNull(projects);
        assertTrue(projects.size() > 0);
    }

    @Test
    public void getProjectByProjectNameStatusTest200() throws IOException {
        ProjectDTO project = apiClient.getJson(
                BasePath.PROJECTS + '/' + PROJECT, ProjectDTO.class, Status.OK);
        assertEquals(PROJECT, project.getProjectName());
    }

    @Test
    public void getProjectByUnavailableNameStatusTest404() throws IOException {
        assertNotNull(apiClient.get(BasePath.PROJECTS + "/SOMETHING", APPLICATION_JSON,
                Status.NOT_FOUND));
    }
}
