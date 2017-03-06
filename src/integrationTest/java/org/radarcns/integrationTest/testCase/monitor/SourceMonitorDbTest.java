package org.radarcns.integrationTest.testCase.monitor;

import static org.junit.Assert.assertEquals;
import static org.radarcns.avro.restapi.source.SourceType.EMPATICA;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.junit.After;
import org.junit.Test;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.source.Source;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.avro.restapi.source.State;
import org.radarcns.config.Properties;
import org.radarcns.dao.SensorDataAccessObject;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.integrationTest.util.Utility;
import org.radarcns.monitor.SourceMonitor;
import org.radarcns.source.Empatica;
import org.radarcns.source.SourceCatalog;
import org.radarcns.source.SourceDefinition;

/**
 * Created by francesco on 05/03/2017.
 */
public class SourceMonitorDbTest {

    private static final String USER = "UserID_0";
    private static final String SOURCE = "SourceID_0";
    private static final SourceType SOURCE_TYPE = EMPATICA;

    private static int WINDOWS = 2;

    @Test
    public void testGetStateFine() throws IOException {
        MongoClient client = getClient();

        Source source = getSource(WINDOWS,0, client);

        assertEquals(State.FINE, source.getSummary().getState());

        dropAndClose(client);
    }

    @Test
    public void testGetStateOk() throws ConnectException {
        MongoClient client = getClient();

        Source source = getSource(WINDOWS, 0.05, client);

        assertEquals(State.OK, source.getSummary().getState());

        dropAndClose(client);
    }

    @Test
    public void testGetStateWarining() throws ConnectException {
        MongoClient client = getClient();

        Source source = getSource(WINDOWS, 0.50, client);

        assertEquals(State.WARNING, source.getSummary().getState());

        dropAndClose(client);
    }

    @Test
    public void testGetStateDisconnected() throws ConnectException {
        MongoClient client = getClient();

        Source source = getSource(WINDOWS, 1, client);

        assertEquals(State.DISCONNECTED, source.getSummary().getState());

        dropAndClose(client);
    }

    @After
    public void dropAndClose() {
        dropAndClose(Utility.getMongoClient());
    }

    public void dropAndClose(MongoClient client) {
        SourceDefinition definition = SourceCatalog.getInstance(SOURCE_TYPE);
        for (SensorType sensorType : definition.getSensorTypes()) {
            Utility.dropCollection(client,
                SensorDataAccessObject.getInstance().getCollectionName(SOURCE_TYPE, sensorType));
        }

        client.close();
    }

    private Source getSource (int window, double percentage, MongoClient client) throws ConnectException {
        long timestamp = System.currentTimeMillis();

        Map<SensorType, Integer> count = new HashMap<>();
        long start = timestamp + TimeUnit.SECONDS.toMillis(10);
        long end = start + TimeUnit.SECONDS.toMillis(60 / (window + 1));
        int messages;
        MongoCollection<Document> collection;
        SourceDefinition definition = SourceCatalog.getInstance(SOURCE_TYPE);
        for (int i = 0; i < window; i++) {
            for (SensorType sensorType : definition.getSensorTypes()) {
                collection = MongoHelper.getCollection(client,
                    SensorDataAccessObject.getInstance()
                        .getCollectionName(SOURCE_TYPE, sensorType));

                messages = reducedMessage(
                    definition.getFrequency(sensorType).intValue(), percentage) / window;

                Document doc;
                if (sensorType.name().equals(sensorType.ACC)) {
                    doc = getDocumentsByArray(messages, start, end);
                    collection.insertOne(doc);
                } else {
                    doc = getDocumentsBySingle(messages, start, end);
                    collection.insertOne(doc);
                }

                if (count.containsKey(sensorType)) {
                    count.put(sensorType, count.get(sensorType) + messages);
                } else {
                    count.put(sensorType, messages);
                }
            }

            start = end;
            end = start + TimeUnit.SECONDS.toMillis(60 / (window + 1));
        }

        end = start + TimeUnit.SECONDS.toMillis(1);
        for (SensorType sensorType : count.keySet()) {
            int sendMessages = count.containsKey(sensorType) ? count.get(sensorType) : 0;
            messages = reducedMessage(
                definition.getFrequency(sensorType).intValue(), percentage) - sendMessages;

            if (messages > 0) {
                collection = MongoHelper.getCollection(client,
                    SensorDataAccessObject.getInstance()
                        .getCollectionName(SOURCE_TYPE, sensorType));
                Document doc;
                if (sensorType.name().equals(sensorType.ACC)) {
                    doc = getDocumentsByArray(messages, start, end);
                    collection.insertOne(doc);
                } else {
                    doc = getDocumentsBySingle(messages, start, end);
                    collection.insertOne(doc);
                }
            }
        }

        return new SourceMonitor(new Empatica()).getState(USER, SOURCE, timestamp, end, client);
    }


    private Document getDocumentsBySingle(int samples, long start, long end) {
        return new Document("_id", USER + "-" + SOURCE + "-" + start + "-"+ end)
            .append("user", USER)
            .append("source", SOURCE)
            .append("min", new Double(0))
            .append("max", new Double(0))
            .append("sum", new Double(0))
            .append("count", new Double(samples))
            .append("avg", new Double(0))
            .append("quartile", getQuartile())
            .append("iqr", new Double(0))
            .append("start", new Date(start))
            .append("end", new Date(end));
    }

    private Document getDocumentsByArray(int samples, long start, long end) {
        return new Document("_id", USER + "-" + SOURCE + "-" + start + "-"+ end)
            .append("user", USER)
            .append("source", SOURCE)
            .append("min", getValue(0))
            .append("max", getValue(0))
            .append("sum", getValue(0))
            .append("count", getValue(samples))
            .append("avg", getValue(0))
            .append("quartile", Arrays.asList(new Document[]{
                new Document("25", getValue(0)),
                new Document("50", getValue(0)),
                new Document("75", getValue(0))
            }))
            .append("iqr", getValue(0))
            .append("start", new Date(start))
            .append("end", new Date(end));
    }

    private static List<Document> getQuartile() {
        return Arrays.asList(new Document[]{
            new Document("25", new Double(0)),
            new Document("50", new Double(0)),
            new Document("75", new Double(0))
        });
    }

    private static List<Document> getValue(int value) {
        return Arrays.asList(new Document[]{
            new Document("x", new Double(value)),
            new Document("y", new Double(value)),
            new Document("z", new Double(value))
        });
    }

    private int reducedMessage(double frequency, double reduction) {

        if (frequency == 1.0 && reduction == 1.0) {
            return 0;
        } else if (frequency == 1.0) {
            return Double.valueOf(frequency).intValue();
        }

        return Double.valueOf(frequency * (1 - reduction)).intValue();
    }

    private MongoClient getClient() {
        Properties.getInstanceTest(this.getClass().getClassLoader().getResource(
            Properties.NAME_FILE).getPath());
        return Utility.getMongoClient();
    }
}
