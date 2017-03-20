package org.radarcns.integration.util;

/*
 *  Copyright 2016 Kings College London and The Hyve
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

import static org.radarcns.dao.mongo.AndroidDAO.RECORD_COLLECTION;
import static org.radarcns.dao.mongo.AndroidDAO.STATUS_COLLECTION;
import static org.radarcns.dao.mongo.AndroidDAO.UPTIME_COLLECTION;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.avro.specific.SpecificRecord;
import org.bson.Document;
import org.radarcns.avro.restapi.app.Application;
import org.radarcns.avro.restapi.dataset.Dataset;
import org.radarcns.avro.restapi.dataset.Item;
import org.radarcns.avro.restapi.header.EffectiveTimeFrame;
import org.radarcns.avro.restapi.header.Header;
import org.radarcns.avro.restapi.sensor.Unit;
import org.radarcns.config.Properties;
import org.radarcns.dao.mongo.util.MongoHelper;
import org.radarcns.dao.mongo.util.MongoHelper.Stat;
import org.radarcns.listener.MongoDBContextListener;
import org.radarcns.util.RadarConverter;

public class Utility {

    /**
     * Returns a MongoDB client using settings stored in the resource folder.
     */
    public static MongoClient getMongoClient() {
        List<MongoCredential> credentials = Properties.getInstance().getMongoDbCredential();
        MongoClient client = new MongoClient(Properties.getInstance().getMongoHosts(),credentials);
        if (!MongoDBContextListener.checkMongoConnection(client)) {
            client = null;
        }

        return client;
    }

    /**
     * @param value Long value that has to be converted.
     * @return the number of milliseconds since January 1, 1970, 00:00:00 GMT representing the
     *      initial time of a Kafka time window.
     **/
    public static Long getStartTimeWindow(Long value) {
        Double timeDouble = value.doubleValue() / 10000d;
        return timeDouble.longValue() * 10000;
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
     * @param docs list of Documents that has to be coverted
     * @param stat filed extracted from the document
     * @param unit measurement unit useful to generate the dataset's header
     * @param recordClass class used compute the Item
     * @return a Dataset rep all required document
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static Dataset convertDocToDataset(List<Document> docs, Stat stat, Unit unit,
            Class<? extends SpecificRecord> recordClass)
        throws IllegalAccessException, InstantiationException {
        EffectiveTimeFrame eftHeader = new EffectiveTimeFrame(
            RadarConverter.getISO8601(docs.get(0).getDate("start")),
            RadarConverter.getISO8601(docs.get(docs.size() - 1).getDate("end")));

        List<Item> itemList = new LinkedList<>();
        for (Document doc : docs) {
            SpecificRecord record = recordClass.newInstance();
            switch (stat) {
                case quartile:
                    throw new UnsupportedOperationException("Not yet implemented");
                default:
                    record.put(record.getSchema().getField("value").pos(),
                        doc.getDouble(stat.getParam()));
            }
            itemList.add(new Item(record, new EffectiveTimeFrame(
                RadarConverter.getISO8601(doc.getDate("start")),
                RadarConverter.getISO8601(doc.getDate("end")))));
        }

        Header header = new Header(RadarConverter.getDescriptiveStatistic(stat), unit, eftHeader);
        return new Dataset(header, itemList);
    }

    /**
     * Makes an HTTP request to given URL.
     * @param url end-point
     * @return HTTP Response
     * @throws IOException
     */
    public static Response makeRequest(String url) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                    .header("User-Agent", "Mozilla/5.0")
                    .url(url)
                    .build();

        return client.newCall(request).execute();
    }

    /**
     * Converts Bson Document into an Application
     * @param documents map
     * @return
     */
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
     * Converts the give a timestamp to a Human readable date format.
     * @param timestamp value in millisecond that has to be converted
     * @return String representing a date
     */
    public static String timestampToString(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdf.format(new Date(timestamp));
    }
}
