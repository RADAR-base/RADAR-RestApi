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
            String sourceId, SourceTypeDTO sourceType) {

        // get the last document sorted by timeEnd
        MongoCursor<Document> cursor = MongoHelper
                .findDocumentByProjectAndSubjectAndSource(projectId, subjectId, sourceId,
                        KEY.concat(".").concat(END), DESCENDING, null,
                        MongoHelper.getCollection(this
                                        .mongoClient,
                                sourceType.getSourceStatisticsMonitorTopic()));

        if (!cursor.hasNext()) {
            LOGGER.debug("Empty cursor for collection {}",
                    sourceType.getSourceStatisticsMonitorTopic());
        }

        long timeStart = Long.MAX_VALUE;
        long timeEnd = Long.MIN_VALUE;
        if (cursor.hasNext()) {
            Document document = cursor.next();
            Document key = (Document) document.get(KEY);
            timeStart = Math.min(timeStart, key.getDate(START).getTime());
            timeEnd = Math.max(timeEnd, key.getDate(END).getTime());
        }

        cursor.close();
        return new EffectiveTimeFrame(RadarConverter.getISO8601(timeStart), RadarConverter
                .getISO8601(timeEnd));

    }
}
