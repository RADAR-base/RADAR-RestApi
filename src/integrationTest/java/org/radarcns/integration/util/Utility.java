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

import static org.radarcns.dao.mongo.data.android.AndroidAppStatus.UPTIME_COLLECTION;
import static org.radarcns.dao.mongo.data.android.AndroidRecordCounter.RECORD_COLLECTION;
import static org.radarcns.dao.mongo.data.android.AndroidServerStatus.STATUS_COLLECTION;
import static org.radarcns.dao.mongo.util.MongoHelper.END;
import static org.radarcns.dao.mongo.util.MongoHelper.START;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.avro.specific.SpecificRecord;
import org.bson.Document;
import org.radarcns.catalogue.TimeWindow;
import org.radarcns.catalogue.Unit;
import org.radarcns.config.Properties;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.dao.mongo.util.MongoHelper.Stat;
import org.radarcns.listener.MongoDbContextListener;
import org.radarcns.restapi.app.Application;
import org.radarcns.restapi.data.Acceleration;
import org.radarcns.restapi.data.DoubleSample;
import org.radarcns.restapi.dataset.Dataset;
import org.radarcns.restapi.dataset.Item;
import org.radarcns.restapi.header.EffectiveTimeFrame;
import org.radarcns.restapi.header.Header;
import org.radarcns.util.RadarConverter;

public class Utility {

    /**
     * Returns a MongoDB client using settings stored in the resource folder.
     */
    public static MongoClient getMongoClient() {
        MongoCredential credentials = Properties.getApiConfig().getMongoDbCredentials();
        MongoClient client = new MongoClient(Properties.getApiConfig().getMongoDbHosts(),
                credentials, MongoClientOptions.builder().build());
        if (!MongoDbContextListener.checkMongoConnection(client)) {
            throw new IllegalStateException("MongoDB connection invalid for hosts "
                    + Properties.getApiConfig().getMongoDbHosts() + " and credentials "
                    + Properties.getApiConfig().getMongoDbCredentials());
        }

        return client;
    }

    /**
     * Drop mongo collection called name.
     * @param client mongoDB client
     * @param name collection name that has to be dropped
     */
    public static void dropCollection(MongoClient client, String name) {
        MongoHelper.getCollection(client, name).drop();
    }

    /**
     * Drop mongo collection in names.
     * @param client mongoDB client
     * @param names collection names that have to be dropped
     */
    public static void dropCollection(MongoClient client, List<String> names) {
        for (String tmp : names) {
            MongoHelper.getCollection(client, tmp).drop();
        }
    }

    /**
     * Inserts mixed documents in mixed collections.
     * @param client mongoDb client to access the instance
     * @param map mapping between document and collections
     */
    public static void insertMixedDocs(MongoClient client, Map<String, Document> map) {
        for (String collectionName : map.keySet()) {
            MongoHelper.getCollection(client, collectionName).insertOne(map.get(collectionName));
        }
    }

    /**
     * Generates a Dataset using the input documents.
     * @param docs list of Documents that has to be converted
     * @param subjectId subject identifier
     * @param sourceId source identifier
     * @param stat filed extracted from the document
     * @param unit measurement unit useful to generate the dataset's header
     * @param timeFrame time interval between two consecutive samples
     * @param recordClass class used compute the Item
     * @return a Dataset rep all required document
     * @throws IllegalAccessException if the item class or its nullary constructor is not accessible
     * @throws InstantiationException if item class cannot be instantiated
     */
    public static Dataset convertDocToDataset(List<Document> docs, String subjectId,
            String sourceId, String sourceType, String sensorType, Stat stat, Unit unit,
            TimeWindow timeFrame, Class<? extends SpecificRecord> recordClass)
            throws IllegalAccessException, InstantiationException {
        EffectiveTimeFrame eftHeader = new EffectiveTimeFrame(
                RadarConverter.getISO8601(docs.get(0).getDate(START)),
                RadarConverter.getISO8601(docs.get(docs.size() - 1).getDate(END)));

        List<Item> itemList = new LinkedList<>();
        for (Document doc : docs) {
            SpecificRecord record = recordClass.newInstance();
            switch (stat) {
                case quartile:
                    throw new UnsupportedOperationException("Not yet implemented");
                default:
                    record.put(record.getSchema().getField("value").pos(),
                            doc.getDouble(stat.getParam()));
                    break;
            }
            itemList.add(new Item(record, RadarConverter.getISO8601(doc.getDate(START))));
        }

        Header header = new Header(subjectId, sourceId, sourceType, sensorType,
                    RadarConverter.getDescriptiveStatistic(stat), unit, timeFrame, eftHeader);

        return new Dataset(header, itemList);
    }

    /**
     * Converts Bson Document into an ApplicationConfig.
     * @param documents map containing variables to create the ApplicationConfig class
     * @return an ApplicationConfig class
     *
     * @see Application
     */
    //TODO take field names from RADAR Mongo Connector
    public static Application convertDocToApplication(Map<String, Document> documents) {
        return new Application(
            documents.get(STATUS_COLLECTION).getString("clientIP"),
            documents.get(UPTIME_COLLECTION).getDouble("applicationUptime"),
            RadarConverter.getServerStatus(
                    documents.get(STATUS_COLLECTION).getString("serverStatus")),
            documents.get(RECORD_COLLECTION).getInteger("recordsCached"),
            documents.get(RECORD_COLLECTION).getInteger("recordsSent"),
            documents.get(RECORD_COLLECTION).getInteger("recordsUnsent")
        );
    }

    /**
     * Converts add data in an InputStream to a String.
     * @param in inputstream
     * @return a String representing the file content
     */
    public static String readAll(InputStream in) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }

    /**
     * Computes the {@link EffectiveTimeFrame} related to the {@code List<Document>}. {@code start}
     *      {@code end} can be used to update an exiting {@link EffectiveTimeFrame} using the given
     *      {@code List<Document>}.
     *
     * @param start time window start time
     * @param end time window end time
     * @param docs list of mock documents that has to be analysed to compute the
     *      {@link EffectiveTimeFrame}
     * @return {@link EffectiveTimeFrame} related to the {@code List<Document>}
     */
    public static EffectiveTimeFrame getExpectedTimeFrame(long start, long end,
            List<Document> docs) {
        long expectedStart = start;
        long expectedEnd = end;

        for (Document doc : docs) {
            expectedStart = Math.min(expectedStart, doc.getDate(START).getTime());
            expectedEnd = Math.max(expectedEnd, doc.getDate(END).getTime());
        }

        return new EffectiveTimeFrame(
            RadarConverter.getISO8601(expectedStart),
            RadarConverter.getISO8601(expectedEnd));
    }

    /**
     * Clones the input {@link Dataset}.
     *
     * @param input {@link Dataset} that has to be cloned
     *
     * @return {@link Dataset} cloned from {@code input}
     */
    public static Dataset cloneDataset(Dataset input) {
        Header inputHeader = input.getHeader();
        EffectiveTimeFrame cloneEffectiveTimeFrame =  new EffectiveTimeFrame(
                inputHeader.getEffectiveTimeFrame().getStartDateTime(),
                inputHeader.getEffectiveTimeFrame().getEndDateTime());
        Header cloneHeader = new Header(inputHeader.getSubjectId(), inputHeader.getSourceId(),
                    inputHeader.getSource(), inputHeader.getType(),
                    inputHeader.getDescriptiveStatistic(), inputHeader.getUnit(),
                    inputHeader.getTimeWindow(), cloneEffectiveTimeFrame);


        List<Item> cloneItem = new ArrayList<>();
        SpecificRecord value;
        for (Item item : input.getDataset()) {

            if (item.getSample() instanceof DoubleSample) {
                value = new DoubleSample(((DoubleSample)item.getSample()).getValue());
            } else if (item.getSample() instanceof Acceleration) {
                Acceleration temp = (Acceleration)item.getSample();
                value = new Acceleration(temp.getX(), temp.getY(), temp.getZ());
            } else {
                throw new IllegalArgumentException(item.getSample().getClass().getCanonicalName()
                        + " is not supported yet");
            }

            cloneItem.add(new Item(value, item.getStartDateTime()));
        }

        return new Dataset(cloneHeader, cloneItem);
    }
}
