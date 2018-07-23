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

import static org.radarcns.mongo.data.monitor.application.ApplicationStatusRecordCounter.RECORD_COLLECTION;
import static org.radarcns.mongo.data.monitor.application.ApplicationStatusServerStatus.STATUS_COLLECTION;
import static org.radarcns.mongo.data.monitor.application.ApplicationStatusUpTime.UPTIME_COLLECTION;
import static org.radarcns.mongo.util.MongoHelper.VALUE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.radarcns.domain.restapi.dataset.DataItem;
import org.radarcns.domain.restapi.dataset.Dataset;
import org.radarcns.domain.restapi.format.Acceleration;
import org.radarcns.domain.restapi.header.Header;
import org.radarcns.domain.restapi.header.TimeFrame;
import org.radarcns.domain.restapi.monitor.ApplicationStatus;
import org.radarcns.domain.restapi.monitor.QuestionnaireCompletionStatus;
import org.radarcns.util.RadarConverter;

public class Utility {
    /**
     * Converts Bson Document into an QuestionnaireCompletionStatus.
     *
     * @param document Document data to create the QuestionnaireCompletionStatus class
     * @return an ApplicationConfig class
     * @see ApplicationStatus
     */
    //TODO take field names from RADAR Mongo Connector
    public static QuestionnaireCompletionStatus convertDocToQuestionnaireCompletionStatus(Document
            document) {
        return new QuestionnaireCompletionStatus(
                ((Document)document.get(VALUE)).getDouble("time"),
                ((Document)document.get(VALUE)).getString("name"),
                ((Document)document.get(VALUE)).getDouble("completionPercentage"));
    }

    /**
     * Converts Bson Document into an ApplicationStatus.
     *
     * @param documents map containing variables to create the ApplicationStatus class
     * @return an ApplicationStatus class
     * @see ApplicationStatus
     */
    public static ApplicationStatus convertDocToApplicationStatus(Map<String, Document> documents) {
        return new ApplicationStatus(
                ((Document) documents.get(STATUS_COLLECTION).get(VALUE)).getString("clientIP"),
                ((Document) documents.get(UPTIME_COLLECTION).get(VALUE)).getDouble("uptime"),
                RadarConverter.getServerStatus(
                        ((Document) documents.get(STATUS_COLLECTION).get(VALUE))
                                .getString("serverStatus")),
                ((Document) documents.get(RECORD_COLLECTION).get(VALUE))
                        .getInteger("recordsCached"),
                ((Document) documents.get(RECORD_COLLECTION).get(VALUE)).getInteger("recordsSent"),
                ((Document) documents.get(RECORD_COLLECTION).get(VALUE))
                        .getInteger("recordsUnsent"));
    }

    /**
     * Clones the input {@link Dataset}.
     *
     * @param input {@link Dataset} that has to be cloned
     * @return {@link Dataset} cloned from {@code input}
     */
    public static Dataset cloneDataset(Dataset input) {
        Header inputHeader = input.getHeader();
        TimeFrame cloneEffectiveTimeFrame =
                new TimeFrame(inputHeader.getEffectiveTimeFrame().getStartDateTime(),
                        inputHeader.getEffectiveTimeFrame().getEndDateTime());
        TimeFrame cloneTimeFrame = new TimeFrame(inputHeader.getTimeFrame().getStartDateTime(),
                inputHeader.getTimeFrame().getEndDateTime());
        Header cloneHeader = new Header(inputHeader.getProjectId(), inputHeader.getSubjectId(),
                inputHeader.getSourceId(), inputHeader.getSourceType(),
                inputHeader.getSourceDataType(), inputHeader.getDescriptiveStatistic(),
                inputHeader.getUnit(), inputHeader.getTimeWindow(), cloneTimeFrame,
                cloneEffectiveTimeFrame);

        List<DataItem> cloneItem = new ArrayList<>();
        Object value;
        for (DataItem item : input.getDataset()) {

            if (item.getValue() instanceof Double) {
                value = item.getValue();
            } else if (item.getValue() instanceof Acceleration) {
                Acceleration temp = (Acceleration) item.getValue();
                value = new Acceleration(temp.getX(), temp.getY(), temp.getZ());
            } else {
                throw new IllegalArgumentException(
                        item.getValue().getClass().getCanonicalName() + " is not supported yet");
            }

            cloneItem.add(new DataItem(value, item.getStartDateTime()));
        }

        return new Dataset(cloneHeader, cloneItem);
    }
}
