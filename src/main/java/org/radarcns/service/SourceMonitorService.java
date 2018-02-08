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

package org.radarcns.service;

import static org.radarcns.mongo.util.MongoHelper.DESCENDING;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import java.net.ConnectException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.bson.Document;
import org.radarcns.domain.managementportal.SourceType;
import org.radarcns.domain.restapi.Source;
import org.radarcns.domain.restapi.States;
import org.radarcns.domain.restapi.header.EffectiveTimeFrame;
import org.radarcns.mongo.util.MongoHelper;
import org.radarcns.util.RadarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic sourceType monitor.
 */
public class SourceMonitorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceMonitorService.class);
    private static final String TIME_START = "timeStart";
    private static final String TIME_END = "timeEnd";

    private final MongoClient mongoClient;


    /**
     * Constructor.
     **/
    @Inject
    public SourceMonitorService(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }


    /**
     * Finds effectiveTimeFrame of a source of a subject under a project by querying
     * source-monitor-statistics of source-type.
     *
     * @param projectId of the subject
     * @param subjectId of the subject
     * @param sourceId of the source
     * @param sourceType of the source
     * @return calculated {@link EffectiveTimeFrame} with earliest and latest timestamps
     */
    public EffectiveTimeFrame getEffectiveTimeFrame(String projectId, String subjectId,
            String sourceId, SourceType sourceType) {

        // get the last document sorted by timeEnd
        MongoCursor<Document> cursor = MongoHelper
                .findDocumentByProjectAndSubjectAndSource(projectId, subjectId, sourceId,
                        TIME_END, DESCENDING, 1, MongoHelper.getCollection(this.mongoClient,
                                sourceType.getSourceStatisticsMonitorTopic()));

        if (!cursor.hasNext()) {
            LOGGER.debug("Empty cursor for collection {}",
                    sourceType.getSourceStatisticsMonitorTopic());
        }
        long timeStart = Long.MIN_VALUE;
        long timeEnd = Long.MAX_VALUE;
        if (cursor.hasNext()) {
            Document document = cursor.next();
            timeStart = Math.max(timeStart, document.getDate(TIME_START).getTime());
            timeEnd = Math.min(timeEnd, document.getDate(TIME_END).getTime());
        }

        cursor.close();
        return new EffectiveTimeFrame(RadarConverter.getISO8601(timeStart), RadarConverter
                .getISO8601(timeEnd));

    }

    /**
     * This will fetch all the sourceIds received under given subjectId and projectId in the
     * provided SourceType.
     *
     * @param projectName of the subject
     * @param subjectId of the subject
     * @param sourceType of the source
     */
    public List<String> getAllSourcesOfSubjectInProject(String projectName, String subjectId,
            SourceType sourceType) {

        // get the last document sorted by timeEnd
        MongoCursor<String> cursor = MongoHelper
                .findAllSourcesBySubjectAndProject(projectName, subjectId,
                        MongoHelper.getCollection(this.mongoClient,
                                sourceType.getSourceStatisticsMonitorTopic()));
        if (!cursor.hasNext()) {
            LOGGER.debug("No records found in collection {} for subject {} and project {}",
                    sourceType.getSourceStatisticsMonitorTopic(), subjectId, projectName);
        }
        List<String> sourceIds = Collections.emptyList();
        if (cursor.hasNext()) {
            String entry = cursor.next();
            sourceIds.add(entry);
        }

        cursor.close();
        return sourceIds;
    }

    /**
     * Checks the status for the given sourceType counting the number of received messages and
     *      checking whether it respects the data frequencies. There is a check for each data.
     *
     * @param subjectId identifier
     * @param sourceId identifier
     * @param client is the MongoDB client
     * @return {@code SourceDefinition} representing a sourceType sourceType
     * @throws ConnectException if the connection with MongoDb is faulty
     *
     * @see Source
     */
    public Source getState(String subjectId, String sourceId, MongoClient client, double countTemp)
            throws ConnectException {

        long tenSec = TimeUnit.SECONDS.toMillis(10);
        long end = (System.currentTimeMillis() / tenSec) * tenSec;
        long start = end - TimeUnit.MINUTES.toMillis(1);

        return getState(subjectId, sourceId, start, end, client, countTemp);
    }

    /**
     * Checks the status for the given sourceType counting the number of received messages and
     *      checking whether it respects the data frequencies. There is a check for each data.
     *
     * @param subject identifier
     * @param sourceType identifier
     * @param start initial time that has to be monitored
     * @param end final time that has to be monitored
     * @param client is the MongoDB client
     * @return {@code SourceDefinition} representing a sourceType sourceType
     * @throws ConnectException if the connection with MongoDb is faulty
     *
     * @see Source
     */
    public Source getState(String subject, String sourceType, long start, long end,
            MongoClient client, double countTemp) throws ConnectException {

        //TODO calculate source state
        return null;
    }

    /**
     * Returns the percentage of received message with respect to the expected value.
     *
     * @param count received messages
     * @param expected expected messages
     * @return the ratio of count over expected
     */
    public static double getPercentage(double count, double expected) {
        return count / expected;
    }

    /**
     * Convert numerical percentage to sourceType status.
     *
     * @param percentage numerical value that has to be converted int Status
     * @return the current {@code Status}
     */
    public static States getStatus(double percentage) {
        if (percentage > 0.95) {
            return States.FINE;
        } else if (percentage > 0.80 && percentage <= 0.95) {
            return States.OK;
        } else if (percentage > 0.0 && percentage <= 0.80) {
            return States.WARNING;
        } else if (percentage == 0.0) {
            return States.DISCONNECTED;
        } else {
            return States.UNKNOWN;
        }
    }

}
