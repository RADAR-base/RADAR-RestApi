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

package org.radarcns.integration.util;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.radarcns.mongo.data.monitor.application.ApplicationStatusRecordCounter.RECORD_COLLECTION;
import static org.radarcns.mongo.data.monitor.application.ApplicationStatusServerStatus.STATUS_COLLECTION;
import static org.radarcns.mongo.data.monitor.application.ApplicationStatusUpTime.UPTIME_COLLECTION;
import static org.radarcns.mongo.util.MongoHelper.ID;
import static org.radarcns.mongo.util.MongoHelper.KEY;
import static org.radarcns.mongo.util.MongoHelper.PROJECT_ID;
import static org.radarcns.mongo.util.MongoHelper.SOURCE_ID;
import static org.radarcns.mongo.util.MongoHelper.USER_ID;
import static org.radarcns.mongo.util.MongoHelper.VALUE;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.bson.Document;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.dataset.Dataset;
import org.radarcns.domain.restapi.header.DescriptiveStatistic;
import org.radarcns.kafka.ObservationKey;
import org.radarcns.mock.model.ExpectedArrayValue;
import org.radarcns.mock.model.ExpectedDoubleValue;
import org.radarcns.mock.model.ExpectedValue;
import org.radarcns.monitor.application.ServerStatus;
import org.radarcns.stream.collector.DoubleArrayCollector;
import org.radarcns.stream.collector.DoubleValueCollector;
import org.radarcns.util.TimeScale;

/**
 * All supported sources specifications.
 */
public class RandomInput {

    public static final String DATASET = "dataset";
    public static final String DOCUMENTS = "documents";
    private static final String SUPPORTED_SOURCE_TYPE = "empatica_e4_v1";

    private static final ExpectedDocumentFactory expectedDocumentFactory =
            new ExpectedDocumentFactory();
    private static final ExpectedDataSetFactory expectedDataSetFactory =
            new ExpectedDataSetFactory();

    private static ExpectedValue<DoubleValueCollector> randomDoubleValue(
            ObservationKey key, TimeWindow timeWindow, int numberOfRecords, Instant endTime) {
        ExpectedDoubleValue instance = new ExpectedDoubleValue();

        Instant timeStamp = endTime.minus(
                TimeScale.getSeconds(timeWindow) * numberOfRecords, SECONDS);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < numberOfRecords; i++) {
            instance.add(key, timeStamp.toEpochMilli(), random.nextDouble());
            timeStamp = timeStamp.plus(TimeScale.getDuration(timeWindow));
        }

        return instance;
    }

    private static ExpectedValue<DoubleArrayCollector> randomArrayValue(ObservationKey key,
            TimeWindow timeWindow, int numberOfRecords, Instant endTime) {
        ExpectedArrayValue instance = new ExpectedArrayValue();

        ThreadLocalRandom random = ThreadLocalRandom.current();
        Instant timeStamp = endTime.minus(
                TimeScale.getSeconds(timeWindow) * numberOfRecords, SECONDS);
        for (int i = 0; i < numberOfRecords; i++) {
            instance.add(key, timeStamp.toEpochMilli(), random.nextDouble(), random.nextDouble(),
                    random.nextDouble());
            timeStamp = timeStamp.plus(TimeScale.getDuration(timeWindow));
        }

        return instance;
    }

    /**
     * Returns a Map containing a {@code Dataset} and a {@code Collection<Document>} randomly
     * generated mocking the behaviour of the RADAR-CNS Platform.
     */
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public static Map<String, Object> getDatasetAndDocumentsRandom(String project, String user,
            String source, String sourceType, String sourceDataName, DescriptiveStatistic stat,
            TimeWindow timeWindow, int samples, boolean singleWindow, Instant endTime) {
        ObservationKey key = new ObservationKey(project, user, source);

        int numberOfRecords = samples;
        if (singleWindow) {
            numberOfRecords = 1;
        }

        if (SUPPORTED_SOURCE_TYPE.equals(sourceType)) {
            return getBoth(key, sourceType, sourceDataName, stat,
                    timeWindow, numberOfRecords, endTime);
        }

        throw new UnsupportedOperationException(sourceType + " is not"
                + " currently supported.");
    }

    @SuppressWarnings("PMD.ExcessiveParameterList")
    private static Map<String, Object> getBoth(ObservationKey key,
            String sourceType, String sourceDataName, DescriptiveStatistic stat,
            TimeWindow timeWindow, int numberOfRecords, Instant endTime) {
        ExpectedValue<?> expectedValue;
        switch (sourceDataName) {
            case "EMPATICA_E4_v1_ACCELEROMETER":
                expectedValue = randomArrayValue(key, timeWindow, numberOfRecords, endTime);
                break;
            default:
                expectedValue = randomDoubleValue(key, timeWindow, numberOfRecords, endTime);
                break;
        }

        Dataset dataset = expectedDataSetFactory.getDataset(expectedValue, key.getProjectId(),
                key.getUserId(), key.getSourceId(), sourceType, sourceDataName, stat, timeWindow);
        List<Document> documents = expectedDocumentFactory.produceExpectedDocuments(
                expectedValue, timeWindow);

        Map<String, Object> map = new HashMap<>();
        map.put(DATASET, dataset);
        map.put(DOCUMENTS, documents);
        return map;
    }

    /**
     * Generates and returns a randomly generated
     * {@link org.radarcns.domain.restapi.monitor.QuestionnaireCompletionStatus} mock data
     * sent by RADAR-CNS aRMT.
     **/
    public static Document getRandomQuestionnaireCompletionLog(String project,
            String user, String source) {

        ThreadLocalRandom random = ThreadLocalRandom.current();

        double timestamp = random.nextDouble();
        double completionPercentage = random.nextDouble(0d,100d);
        String name = "PHQ8";
        Document completionDoc = new Document()
                .append("time", timestamp)
                .append("name", name)
                .append("completionPercentage", completionPercentage);

        return buildDocumentWithObservationKey(project, user, source, completionDoc);

    }

    /**
     * Generates and returns a randomly generated {@code ApplicationStatus} mocking data sent by
     * RADAR-CNS pRMT.
     **/
    public static Map<String, Document> getRandomApplicationStatus(String project, String user,
            String source) {
        String ipAdress = getRandomIpAddress();

        ThreadLocalRandom random = ThreadLocalRandom.current();

        ServerStatus serverStatus = ServerStatus.values()[
                random.nextInt(0, ServerStatus.values().length)];
        double uptime = random.nextDouble();
        double timestamp = random.nextDouble();
        int recordsCached = random.nextInt();
        int recordsSent = random.nextInt();
        int recordsUnsent = random.nextInt();

        return getRandomApplicationStatus(project, user, source, ipAdress, serverStatus, uptime,
                recordsCached, recordsSent, recordsUnsent, timestamp);
    }

    /**
     * Generates and returns a ApplicationStatus using the given inputs.
     **/
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public static Map<String, Document> getRandomApplicationStatus(String project, String user,
            String source, String ipAddress, ServerStatus serverStatus, double uptime,
            int recordsCached, int recordsSent, int recordsUnsent, double timeStamp) {
        Document uptimeDoc = new Document()
                .append("time", timeStamp)
                .append("uptime", uptime);

        Document statusDoc = new Document()
                .append("time", timeStamp)
                .append("clientIP", ipAddress)
                .append("serverStatus", serverStatus.toString());

        Document recordsDoc = new Document()
                .append("time", timeStamp)
                .append("recordsCached", recordsCached)
                .append("recordsSent", recordsSent)
                .append("recordsUnsent", recordsUnsent);

        Map<String, Document> documents = new HashMap<>();
        documents.put(STATUS_COLLECTION,
                buildDocumentWithObservationKey(project, user, source, statusDoc));
        documents.put(RECORD_COLLECTION,
                buildDocumentWithObservationKey(project, user, source, recordsDoc));
        documents.put(UPTIME_COLLECTION,
                buildDocumentWithObservationKey(project, user, source, uptimeDoc));
        return documents;
    }

    private static Document buildKeyDocument(String projectName, String subjectId,
            String sourceId) {
        return new Document().append(PROJECT_ID, projectName)
                .append(USER_ID, subjectId)
                .append(SOURCE_ID, sourceId);
    }

    private static Document buildDocumentWithObservationKey(String projectName, String subjectId,
            String sourceId,
            Document value) {
        return new Document().append(ID, "{"
                + PROJECT_ID + ":" + projectName + ","
                + USER_ID + ":" + subjectId + ","
                + SOURCE_ID + ":" + sourceId + "}")
                .append(KEY, buildKeyDocument(projectName, subjectId, sourceId))
                .append(VALUE, value);
    }

    /**
     * Returns a String representing a random IP address.
     **/
    private static String getRandomIpAddress() {
        long ip = ThreadLocalRandom.current().nextLong();
        StringBuilder result = new StringBuilder(15);

        for (int i = 0; i < 4; i++) {
            result.insert(0, Long.toString(ip & 0xff));
            if (i < 3) {
                result.insert(0, '.');
            }
            ip = ip >> 8;
        }
        return result.toString();
    }

}
