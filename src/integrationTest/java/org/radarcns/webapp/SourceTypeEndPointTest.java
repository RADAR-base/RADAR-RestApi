package org.radarcns.webapp;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import javax.ws.rs.core.Response.Status;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.domain.managementportal.SourceTypeDTO;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RestApiDetails;
import org.radarcns.webapp.resource.BasePath;

public class SourceTypeEndPointTest {

    private static final String PRODUCER = "Empatica";
    private static final String MODEL = "E4";
    private static final String CATALOGUE_VERSION = "v1";

    @Rule
    public final ApiClient apiClient = new ApiClient(
            RestApiDetails.getRestApiClientDetails().getApplicationConfig().getUrlString());

    @Test
    public void getAllSourceTypesStatusTest200() throws IOException {

        List<SourceTypeDTO> sourceTypes = apiClient.requestJsonList(
                BasePath.SOURCE_TYPES, SourceTypeDTO.class, Status.OK);
        assertNotNull(sourceTypes);
        assertTrue(sourceTypes.size() > 0);
    }

    @Test
    public void getSourceTypeByIdentifierStatusTest200() throws IOException {
        SourceTypeDTO project = apiClient.requestJson(
                BasePath.SOURCE_TYPES + '/' + PRODUCER + '/' + MODEL + '/' + CATALOGUE_VERSION,
                SourceTypeDTO.class, Status.OK);
        assertEquals(PRODUCER, project.getProducer());
        assertEquals(MODEL, project.getModel());
        assertEquals(CATALOGUE_VERSION, project.getCatalogVersion());

    }

    @Test
    public void getSourceTypeByUnavailableIdStatusTest404() throws IOException {
        apiClient.assertRequest(
                BasePath.SOURCE_TYPES + '/' + "SOMETHING" + '/' + MODEL + '/' + CATALOGUE_VERSION,
                APPLICATION_JSON, Status.NOT_FOUND);
    }
}