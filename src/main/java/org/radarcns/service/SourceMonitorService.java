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
import static org.radarcns.mongo.util.MongoHelper.END;
import static org.radarcns.mongo.util.MongoHelper.KEY;
import static org.radarcns.mongo.util.MongoHelper.START;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import javax.inject.Inject;
import org.bson.Document;
import org.radarcns.domain.managementportal.SourceTypeDTO;
import org.radarcns.domain.restapi.header.TimeFrame;
import org.radarcns.mongo.util.MongoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic sourceType monitor.
 */
public class SourceMonitorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceMonitorService.class);

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
     * @return calculated {@link TimeFrame} with earliest and latest timestamps or null if no
     *         data was found.
     */
    public TimeFrame getEffectiveTimeFrame(String projectId, String subjectId,
            String sourceId, SourceTypeDTO sourceType) {

        // get the last document sorted by timeEnd
        try (MongoCursor<Document> cursor = MongoHelper.findDocumentBySource(
                MongoHelper.getCollection(this.mongoClient,
                        sourceType.getSourceStatisticsMonitorTopic()),
                projectId, subjectId, sourceId, KEY + "." + END, DESCENDING, 1)) {

            TimeFrame timeFrame = null;
            while (cursor.hasNext()) {
                Document key = (Document) cursor.next().get(KEY);
                timeFrame = TimeFrame.span(timeFrame,
                        new TimeFrame(key.getDate(START), key.getDate(END)));
            }

            if (timeFrame == null) {
                LOGGER.debug("Empty cursor for collection {}",
                        sourceType.getSourceStatisticsMonitorTopic());
            }
            return timeFrame;
        }
    }
}
