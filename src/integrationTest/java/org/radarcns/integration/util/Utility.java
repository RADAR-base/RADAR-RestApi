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

import static org.radarcns.mongo.data.monitor.ApplicationStatusRecordCounter.RECORD_COLLECTION;
import static org.radarcns.mongo.data.monitor.ApplicationStatusServerStatus.STATUS_COLLECTION;
import static org.radarcns.mongo.data.monitor.ApplicationStatusUpTime.UPTIME_COLLECTION;
import static org.radarcns.mongo.util.MongoHelper.VALUE;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.radarcns.config.Properties;
import org.radarcns.domain.restapi.Application;
import org.radarcns.domain.restapi.dataset.DataItem;
import org.radarcns.domain.restapi.dataset.Dataset;
import org.radarcns.domain.restapi.format.Acceleration;
import org.radarcns.domain.restapi.header.Header;
import org.radarcns.domain.restapi.header.TimeFrame;
import org.radarcns.mongo.util.MongoHelper;
import org.radarcns.util.RadarConverter;

public class Utility {

    /**
     * Returns a MongoDB client using settings stored in the resource folder.
     */
    public static MongoClient getMongoClient() {
        MongoCredential credentials = Properties.getApiConfig().getMongoDbCredentials();

        return new MongoClient(Properties.getApiConfig().getMongoDbHosts(),
                credentials, MongoClientOptions.builder().build());
    }

    /**
     * Drop mongo collection in names.
     *
     * @param client mongoDB client
     * @param names collection names that have to be dropped
     */
    public static void dropCollection(MongoClient client, String... names) {
        for (String tmp : names) {
            MongoHelper.getCollection(client, tmp).drop();
        }
    }

    /**
     * Inserts mixed documents in mixed collections.
     *
     * @param client mongoDb client to access the instance
     * @param map mapping between document and collections
     */
    public static void insertMixedDocs(MongoClient client, Map<String, Document> map) {
        for (String collectionName : map.keySet()) {
            MongoHelper.getCollection(client, collectionName).insertOne(map.get(collectionName));
        }
    }


    /**
     * Converts Bson Document into an ApplicationConfig.
     *
     * @param documents map containing variables to create the ApplicationConfig class
     * @return an ApplicationConfig class
     * @see Application
     */
    //TODO take field names from RADAR Mongo Connector
    public static Application convertDocToApplication(Map<String, Document> documents) {
        return new Application(
                ((Document) documents.get(STATUS_COLLECTION).get(VALUE)).getString("clientIP"),
                ((Document) documents.get(UPTIME_COLLECTION).get(VALUE)).getDouble("uptime"),
                RadarConverter.getServerStatus(
                        ((Document) documents.get(STATUS_COLLECTION).get(VALUE))
                                .getString("serverStatus")),
                ((Document) documents.get(RECORD_COLLECTION).get(VALUE))
                        .getInteger("recordsCached"),
                ((Document) documents.get(RECORD_COLLECTION).get(VALUE)).getInteger("recordsSent"),
                ((Document) documents.get(RECORD_COLLECTION).get(VALUE)).getInteger("recordsUnsent")
        );
    }

    /**
     * Clones the input {@link Dataset}.
     *
     * @param input {@link Dataset} that has to be cloned
     * @return {@link Dataset} cloned from {@code input}
     */
    public static Dataset cloneDataset(Dataset input) {
        Header inputHeader = input.getHeader();
        TimeFrame cloneEffectiveTimeFrame = new TimeFrame(
                inputHeader.getEffectiveTimeFrame().getStartDateTime(),
                inputHeader.getEffectiveTimeFrame().getEndDateTime());
        TimeFrame cloneTimeFrame = new TimeFrame(
                inputHeader.getTimeFrame().getStartDateTime(),
                inputHeader.getTimeFrame().getEndDateTime());
        Header cloneHeader = new Header(inputHeader.getProjectId(), inputHeader.getSubjectId(),
                inputHeader.getSourceId(), inputHeader.getSourceType(),
                inputHeader.getSourceDataType(),
                inputHeader.getDescriptiveStatistic(), inputHeader.getUnit(),
                inputHeader.getTimeWindow(), cloneTimeFrame, cloneEffectiveTimeFrame);

        List<DataItem> cloneItem = new ArrayList<>();
        Object value;
        for (DataItem item : input.getDataset()) {

            if (item.getValue() instanceof Double) {
                value = item.getValue();
            } else if (item.getValue() instanceof Acceleration) {
                Acceleration temp = (Acceleration) item.getValue();
                value = new Acceleration(temp.getX(), temp.getY(), temp.getZ());
            } else {
                throw new IllegalArgumentException(item.getValue().getClass().getCanonicalName()
                        + " is not supported yet");
            }

            cloneItem.add(new DataItem(value, item.getStartDateTime()));
        }

        return new Dataset(cloneHeader, cloneItem);
    }
}
