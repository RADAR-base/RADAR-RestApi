package org.radarcns.webapp;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import javax.ws.rs.core.Response.Status;
import okhttp3.Response;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.domain.managementportal.SourceType;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RestApiDetails;
import org.radarcns.util.RadarConverter;
import org.radarcns.webapp.resource.BasePath;

public class SourceTypeEndPointTest {

    private static final String PRODUCER = "Empatica";
    private static final String MODEL = "E4";
    private static final String CATALOGUE_VERSION = "v1";

    @Rule
    public final ApiClient apiClient = new ApiClient(
            RestApiDetails.getRestApiClientDetails().getApplicationConfig().getUrlString());

    @Test
    public void getAllSourceTypesStatusTest200()
            throws IOException, URISyntaxException {

        Response actual = apiClient.request(BasePath.SOURCE_TYPES, APPLICATION_JSON, Status.OK);
        assertTrue(actual.isSuccessful());
        ObjectReader reader = RadarConverter.readerForCollection(List.class, SourceType.class);
        List<SourceType> sourceTypes = reader.readValue(actual.body().byteStream());

        assertNotNull(sourceTypes);
        assertTrue(sourceTypes.size() > 0);
    }

    @Test
    public void getSourceTypeByIdentifierStatusTest200()
            throws IOException, ReflectiveOperationException, URISyntaxException {

        Response actual = apiClient
                .request(BasePath.SOURCE_TYPES + "/" + PRODUCER + "/" + MODEL + "/"
                                + CATALOGUE_VERSION, APPLICATION_JSON,
                        Status.OK);
        assertTrue(actual.isSuccessful());
        ObjectReader reader = RadarConverter.readerFor(SourceType.class);
        SourceType project = reader.readValue(actual.body().byteStream());
        assertEquals(PRODUCER, project.getProducer());
        assertEquals(MODEL, project.getModel());
        assertEquals(CATALOGUE_VERSION, project.getCatalogVersion());

    }

    @Test
    public void getSourceTypeByUnavailableIdStatusTest404()
            throws IOException, ReflectiveOperationException, URISyntaxException {

        Response actual = apiClient
                .request(BasePath.SOURCE_TYPES + "/" + "SOMETHING" + "/" + MODEL + "/"
                                + CATALOGUE_VERSION, APPLICATION_JSON,
                        Status.NOT_FOUND);
        assertFalse(actual.isSuccessful());
        assertEquals(actual.code(), Status.NOT_FOUND.getStatusCode());

    }

}