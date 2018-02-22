package org.radarcns.webapp;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.radarcns.webapp.SampleDataHandler.CATALOGUE_VERSION;
import static org.radarcns.webapp.SampleDataHandler.MODEL;
import static org.radarcns.webapp.SampleDataHandler.PRODUCER;

import com.fasterxml.jackson.databind.ObjectReader;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.core.Response.Status;
import okhttp3.Response;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.domain.managementportal.SourceTypeDTO;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RestApiDetails;
import org.radarcns.util.RadarConverter;
import org.radarcns.webapp.resource.BasePath;

public class SourceTypeEndPointTest {



    @Rule
    public final ApiClient apiClient = new ApiClient(
            RestApiDetails.getRestApiClientDetails().getApplicationConfig().getUrlString());

    @Test
    public void getAllSourceTypesStatusTest200() throws IOException {

        Response actual = apiClient.request(BasePath.SOURCE_TYPES, APPLICATION_JSON, Status.OK);
        assertTrue(actual.isSuccessful());
        ObjectReader reader = RadarConverter.readerForCollection(List.class, SourceTypeDTO.class);
        List<SourceTypeDTO> sourceTypes = reader.readValue(actual.body().byteStream());

        assertNotNull(sourceTypes);
        assertTrue(sourceTypes.size() > 0);
    }

    @Test
    public void getSourceTypeByIdentifierStatusTest200() throws IOException {

        Response actual = apiClient
                .request(BasePath.SOURCE_TYPES + "/" + PRODUCER + "/" + MODEL + "/"
                                + CATALOGUE_VERSION, APPLICATION_JSON,
                        Status.OK);
        assertTrue(actual.isSuccessful());
        ObjectReader reader = RadarConverter.readerFor(SourceTypeDTO.class);
        SourceTypeDTO project = reader.readValue(actual.body().byteStream());
        assertEquals(PRODUCER, project.getProducer());
        assertEquals(MODEL, project.getModel());
        assertEquals(CATALOGUE_VERSION, project.getCatalogVersion());

    }

    @Test
    public void getSourceTypeByUnavailableIdStatusTest404() throws IOException {

        Response actual = apiClient
                .request(BasePath.SOURCE_TYPES + "/" + "SOMETHING" + "/" + MODEL + "/"
                                + CATALOGUE_VERSION, APPLICATION_JSON,
                        Status.NOT_FOUND);
        assertFalse(actual.isSuccessful());
        assertEquals(actual.code(), Status.NOT_FOUND.getStatusCode());

    }

}