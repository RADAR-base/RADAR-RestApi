package org.radarcns.webapp;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.radarcns.webapp.resource.BasePath.SUBJECTS;

import com.fasterxml.jackson.databind.ObjectReader;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.Response.Status;
import okhttp3.Response;
import org.bson.Document;
import org.junit.Rule;
import org.junit.Test;
import org.radarcns.domain.restapi.Subject;
import org.radarcns.integration.util.ApiClient;
import org.radarcns.integration.util.RestApiDetails;
import org.radarcns.domain.managementportal.Project;
import org.radarcns.integration.util.Utility;
import org.radarcns.mongo.util.MongoHelper;
import org.radarcns.util.RadarConverter;
import org.radarcns.webapp.resource.BasePath;

public class ProjectEndPointTest {

    private static final String PROJECT_NAME = "radar";
    private static final String SUBJECT_ID = "sub-1";
    private static final String SOURCE_ID = "03d28e5c-e005-46d4-a9b3-279c27fbbc83";
    private static final String MONITOR_STATISTICS_TOPIC = "source_statistics_empatica_e4";

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
        MongoClient mongoClient = Utility.getMongoClient();
        int WINDOWS = 2;
        long start = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
        long end = start + TimeUnit.SECONDS.toMillis(60 / (WINDOWS + 1));
        long later = end + TimeUnit.SECONDS.toMillis(60 / (WINDOWS + 1));
        Document doc = getDocumentsForStatistics(start, end);
        Document second = getDocumentsForStatistics(start, later);
        MongoCollection collection = MongoHelper.getCollection(mongoClient, MONITOR_STATISTICS_TOPIC);
        collection.insertMany(Arrays.asList(doc, second));
        Response actual = apiClient
                .request(BasePath.PROJECTS + "/" + PROJECT_NAME + "/" + SUBJECTS, APPLICATION_JSON,
                        Status.OK);
        assertTrue(actual.isSuccessful());

        ObjectReader reader = RadarConverter.readerForCollection(List.class, Subject.class);
        List<Subject> subjects = reader.readValue(actual.body().byteStream());

        assertNotNull(subjects);
        assertTrue(subjects.size() > 0);

        Utility.dropCollection(mongoClient, MONITOR_STATISTICS_TOPIC);

    }


    private static Document getDocumentsForStatistics(long start, long end) {
        return new Document(MongoHelper.ID, SUBJECT_ID + "-" + SOURCE_ID + "-" + start + "-" + end)
                .append(MongoHelper.USER_ID, SUBJECT_ID)
                .append(MongoHelper.SOURCE_ID, SOURCE_ID)
                .append(MongoHelper.PROJECT_ID, PROJECT_NAME)
                .append(MongoHelper.START, new Date(start))
                .append(MongoHelper.END, new Date(end));
    }

}
