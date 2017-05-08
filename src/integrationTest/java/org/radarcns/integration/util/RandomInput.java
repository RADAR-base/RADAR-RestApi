package org.radarcns.integration.util;

/*
 * Copyright 2016 King's College London and The Hyve
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

import static org.radarcns.dao.mongo.data.android.AndroidAppStatus.UPTIME_COLLECTION;
import static org.radarcns.dao.mongo.data.android.AndroidRecordCounter.RECORD_COLLECTION;
import static org.radarcns.dao.mongo.data.android.AndroidServerStatus.STATUS_COLLECTION;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.radarcns.avro.restapi.app.ServerStatus;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.header.DescriptiveStatistic;
import org.radarcns.avro.restapi.sensor.SensorType;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.integration.model.ExpectedArrayValue;
import org.radarcns.integration.model.ExpectedDoubleValue;

/**
 * All supported sources specifications.
 */
public class RandomInput {

    //private static final Logger LOGGER = LoggerFactory.getLogger(RandomInput.class);

    public static final String DATASET = "dataset";
    public static final String DOCUMENTS = "documents";

    private static Dataset dataset = null;
    private static List<Document> documents = null;

    private static ExpectedDocumentFactory expectedDocumentFactory = new ExpectedDocumentFactory();
    private static ExpectedDataSetFactory expectedDataSetFactory = new ExpectedDataSetFactory();

    private static void randomDoubleValue(String user, String source, SourceType sourceType,
            SensorType sensorType, DescriptiveStatistic stat, int samples, boolean singleWindow)
            throws InstantiationException, IllegalAccessException {
        ExpectedDoubleValue instance = new ExpectedDoubleValue(user, source);

        Long start = new Date().getTime();

        for (int i = 0; i < samples; i++) {
            instance.add(Utility.getStartTimeWindow(start), start,
                    ThreadLocalRandom.current().nextDouble());

            if (singleWindow) {
                start += 1;
            } else {
                start += TimeUnit.SECONDS.toMillis(
                    ThreadLocalRandom.current().nextLong(1, 15));
            }
        }

        dataset = expectedDataSetFactory.getDataset(instance, stat, sourceType, sensorType);
        documents = expectedDocumentFactory.produceExpectedData(instance);
    }

    private static void randomArrayValue(String user, String source, SourceType sourceType,
            SensorType sensorType, DescriptiveStatistic stat, int samples, boolean singleWindow)
            throws InstantiationException, IllegalAccessException {
        ExpectedArrayValue instance = new ExpectedArrayValue(user, source);

        Long start = new Date().getTime();

        Double[] array;
        for (int i = 0; i < samples; i++) {

            array = new Double[3];
            array[0] = ThreadLocalRandom.current().nextDouble();
            array[1] = ThreadLocalRandom.current().nextDouble();
            array[2] = ThreadLocalRandom.current().nextDouble();

            instance.add(Utility.getStartTimeWindow(start), start, array);

            if (singleWindow) {
                start += TimeUnit.SECONDS.toMillis(
                    ThreadLocalRandom.current().nextInt(1, 12));
            } else {
                start += 1;
            }
        }

        dataset = expectedDataSetFactory.getDataset(instance, stat, sourceType, sensorType);
        documents = expectedDocumentFactory.produceExpectedData(instance);
    }

    /**
     * Returns a Map containing a {@code Dataset} and a {@code Collection<Document>} randomly
     *      generated mocking the behaviour of the RADAR-CNS Platform.
     */
    public static Map<String, Object> getDatasetAndDocumentsRandom(String user, String source,
            SourceType sourceType, SensorType sensorType, DescriptiveStatistic stat, int samples,
            boolean singleWindow)
            throws InstantiationException, IllegalAccessException {
        switch (sourceType) {
            case ANDROID: break;
            case BIOVOTION: break;
            case EMPATICA: return getBoth(user, source, sourceType, sensorType, stat, samples,
                    singleWindow);
            case PEBBLE: break;
            default: break;
        }

        throw new UnsupportedOperationException(sourceType.name() + " is not"
            + " currently supported.");
    }

    /**
     * Returns a {@code Dataset} randomly generated that mocks the behaviour of
     *      the RADAR-CNS Platform.
     */
    public static Dataset getDatasetRandom(String user, String source, SourceType sourceType,
            SensorType sensorType, DescriptiveStatistic stat, int samples, boolean singleWindow)
            throws InstantiationException, IllegalAccessException {
        switch (sourceType) {
            case ANDROID: break;
            case BIOVOTION: break;
            case EMPATICA: return getDataset(user, source, sourceType, sensorType, stat, samples,
                            singleWindow);
            case PEBBLE: break;
            default: break;
        }

        throw new UnsupportedOperationException(sourceType.name() + " is not"
            + " currently supported.");
    }

    /**
     * Returns a {@code Collection<Document>} randomly generated that mocks the behaviour of
     *      the RADAR-CNS Platform.
     */
    public static List<Document> getDocumentsRandom(String user, String source,
            SourceType sourceType, SensorType sensorType, DescriptiveStatistic stat, int samples,
            boolean singleWindow)
            throws InstantiationException, IllegalAccessException {
        switch (sourceType) {
            case ANDROID: break;
            case BIOVOTION: break;
            case EMPATICA: return getDocument(user, source, sourceType, sensorType, stat, samples,
                            singleWindow);
            case PEBBLE: break;
            default: break;
        }

        throw new UnsupportedOperationException(sourceType.name() + " is not"
            + " currently supported.");
    }

    private static Dataset getDataset(String user, String source, SourceType sourceType,
            SensorType sensorType, DescriptiveStatistic stat, int samples, boolean singleWindow)
            throws InstantiationException, IllegalAccessException {
        nextValue(user, source, sourceType, sensorType, stat, samples, singleWindow);
        return dataset;
    }

    private static List<Document> getDocument(String user, String source, SourceType sourceType,
            SensorType sensorType, DescriptiveStatistic stat, int samples, boolean singleWindow)
            throws InstantiationException, IllegalAccessException {
        nextValue(user, source, sourceType, sensorType, stat, samples, singleWindow);
        return documents;
    }

    private static Map<String, Object> getBoth(String user, String source, SourceType sourceType,
            SensorType sensorType, DescriptiveStatistic stat, int samples, boolean singleWindow)
            throws InstantiationException, IllegalAccessException {
        nextValue(user, source, sourceType, sensorType, stat, samples, singleWindow);

        Map<String, Object> map = new HashMap();
        map.put(DATASET, dataset);
        map.put(DOCUMENTS, documents);
        return map;
    }

    private static void nextValue(String user, String source, SourceType sourceType,
            SensorType sensorType, DescriptiveStatistic stat, int samples, boolean singleWindow)
            throws IllegalAccessException, InstantiationException {
        switch (sensorType) {
            case ACCELEROMETER:
                randomArrayValue(user, source, sourceType, sensorType, stat, samples,
                                singleWindow);
                break;
            default: randomDoubleValue(user, source, sourceType, sensorType, stat, samples,
                                singleWindow);
                break;
        }
    }

    /** Generates and returns a randomly generated {@code ApplicationStatus} mocking data sent
     *      by RADAR-CNS pRMT.
     **/
    public static Map<String, Document> getRandomApplicationStatus(String user, String source) {
        String ipAdress = getRandomIp();
        ServerStatus serverStatus = ServerStatus.values()[
                ThreadLocalRandom.current().nextInt(0, ServerStatus.values().length)];
        Double uptime = ThreadLocalRandom.current().nextDouble();
        int recordsCached = ThreadLocalRandom.current().nextInt();
        int recordsSent = ThreadLocalRandom.current().nextInt();
        int recordsUnsent = ThreadLocalRandom.current().nextInt();

        return getRandomApplicationStatus(user, source, ipAdress, serverStatus, uptime,
                recordsCached, recordsSent, recordsUnsent);
    }

    /** Generates and returns a ApplicationStatus using the given inputs. **/
    public static Map<String, Document> getRandomApplicationStatus(String user, String source,
            String ipAddress, ServerStatus serverStatus, Double uptime, int recordsCached,
            int recordsSent, int recordsUnsent) {
        String id = user + "-" + source;

        Document uptimeDoc = new Document("_id", id)
                .append("user", user)
                .append("source", source)
                .append("applicationUptime", uptime);

        Document statusDoc = new Document("_id", id)
                .append("user", user)
                .append("source", source)
                .append("clientIP", ipAddress)
                .append("serverStatus", serverStatus.toString());

        Document recordsDoc = new Document("_id", id)
                .append("user", user)
                .append("source", source)
                .append("recordsCached", recordsCached)
                .append("recordsSent", recordsSent)
                .append("recordsUnsent", recordsUnsent);

        Map<String, Document> documents = new HashMap<>();
        documents.put(STATUS_COLLECTION, statusDoc);
        documents.put(RECORD_COLLECTION, recordsDoc);
        documents.put(UPTIME_COLLECTION, uptimeDoc);
        return documents;
    }

    /** Returns a String representing a random IP address. **/
    public static String getRandomIp() {
        long ip = ThreadLocalRandom.current().nextLong();
        StringBuilder result = new StringBuilder(15);

        for (int i = 0; i < 4; i++) {
            result.insert(0,Long.toString(ip & 0xff));
            if (i < 3) {
                result.insert(0,'.');
            }
            ip = ip >> 8;
        }
        return result.toString();
    }

}
