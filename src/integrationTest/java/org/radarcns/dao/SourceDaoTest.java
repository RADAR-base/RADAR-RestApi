//package org.radarcns.dao;
//
///*
// * Copyright 2017 King's College London and The Hyve
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//import static com.mongodb.client.model.Filters.eq;
//import static org.junit.Assert.assertEquals;
//import static org.radarcns.dao.AndroidDaoTest.ANDROID;
//import static org.radarcns.dao.AndroidDaoTest.EMPATICA;
//import static org.radarcns.restapi.header.DescriptiveStatistic.COUNT;
//
//import com.mongodb.MongoClient;
//import com.mongodb.client.MongoCollection;
//import com.mongodb.client.MongoCursor;
//import com.mongodb.client.model.Filters;
//import org.bson.Document;
//import org.junit.After;
//import org.junit.Test;
//import org.radarcns.catalogue.TimeWindow;
//import org.radarcns.mongo.util.MongoHelper;
//import org.radarcns.integration.util.RandomInput;
//import org.radarcns.integration.util.Utility;
//
///**
// * UserDao Test.
// */
//public class SourceDaoTest {
//
//    //private static final Logger logger = LoggerFactory.getLogger(SourceDaoTest.class);
//
//    private static final String SUBJECT = "UserID_0";
//    private static final String SOURCE = "SourceID_0";
//    private static final String SOURCE_TYPE = EMPATICA;
//    private static final String SENSOR_TYPE = "HEART_RATE";
//    private static final TimeWindow TIME_WINDOW = TimeWindow.TEN_SECOND;
//    private static final int SAMPLES = 10;
//
//    @Test
//    public void test() throws Exception {
//        MongoClient client = Utility.getMongoClient();
//
//        MongoCollection<Document> collection = MongoHelper.getCollection(client,
//                DataSetService.getInstance(SENSOR_TYPE).getCollectionName(
//                    SOURCE_TYPE, TIME_WINDOW));
//
//        collection.insertMany(RandomInput.getDocumentsRandom(SUBJECT, SOURCE, SOURCE_TYPE,
//                SENSOR_TYPE, COUNT, TIME_WINDOW, SAMPLES, false));
//
//        assertEquals(SOURCE_TYPE, SourceDataAccessObject.getSourceType(SOURCE, client));
//
//        Utility.insertMixedDocs(client,
//                RandomInput.getRandomApplicationStatus(SUBJECT, SOURCE.concat("1")));
//
//        assertEquals(ANDROID, SourceDataAccessObject.getSourceType(SOURCE.concat("1"), client));
//
//        assertEquals(2, SourceDataAccessObject.findAllSourcesByUser(SUBJECT,
//                client).getSources().size());
//
//        String extractedSourceId = null;
//        String extractedSourceType = null;
//        collection = MongoHelper.getCollection(client, MongoHelper.DEVICE_CATALOG);
//        MongoCursor<Document> cursor = collection.find(Filters.and(eq(MongoHelper.ID, SOURCE)))
//                .iterator();
//        if (cursor.hasNext()) {
//            Document doc = cursor.next();
//            extractedSourceId = doc.getString(MongoHelper.ID);
//            extractedSourceType = doc.getString(MongoHelper.SOURCE_TYPE);
//        }
//        assertEquals(SOURCE, extractedSourceId);
//        assertEquals(EMPATICA, extractedSourceType);
//
//        extractedSourceId = null;
//        extractedSourceType = null;
//        collection = MongoHelper.getCollection(client, MongoHelper.DEVICE_CATALOG);
//        cursor = collection.find(Filters.and(eq(MongoHelper.ID, SOURCE.concat("1")))).iterator();
//        if (cursor.hasNext()) {
//            Document doc = cursor.next();
//            extractedSourceId = doc.getString(MongoHelper.ID);
//            extractedSourceType = doc.getString(MongoHelper.SOURCE_TYPE);
//        }
//        assertEquals(SOURCE.concat("1"), extractedSourceId);
//        assertEquals(ANDROID, extractedSourceType);
//
//        dropAndClose(client);
//    }
//
//    @After
//    public void dropAndClose() {
//        dropAndClose(Utility.getMongoClient());
//    }
//
//    /** Drops all used collections to bring the database back to the initial state, and close the
//     *      database connection.
//     **/
//    public void dropAndClose(MongoClient client) {
//        Utility.dropCollection(client, MongoHelper.DEVICE_CATALOG);
//        Utility.dropCollection(client, DataSetService.getInstance(
//                SENSOR_TYPE).getCollectionName(SOURCE_TYPE, TIME_WINDOW));
//        Utility.dropCollection(client, AndroidAppDataAccessObject.getInstance().getCollections());
//        client.close();
//    }
//}
