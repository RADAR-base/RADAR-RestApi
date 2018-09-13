/*
 * Copyright 2017 King's College London and The Hyve
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

package org.radarcns.mongo.util;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lt;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.radarcns.config.Properties;
import org.radarcns.domain.restapi.header.TimeFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic MongoDB helper.
 */
public class MongoHelper {

    private static final Logger logger = LoggerFactory.getLogger(MongoHelper.class);

    public static final String ID = "_id";
    public static final String KEY = "key";
    public static final String VALUE = "value";
    public static final String USER_ID = "userId";
    public static final String SOURCE_ID = "sourceId";
    public static final String PROJECT_ID = "projectId";
    public static final String START = "timeStart";
    public static final String END = "timeEnd";
    public static final String FIELDS = "fields";
    public static final String QUARTILE = "quartile";
    public static final String COUNT = "count";
    public static final String NAME = "name";


    public static final int ASCENDING = 1;
    public static final int DESCENDING = -1;

    private static Map<String, List<String>> indexMap = new HashMap<>();

    private static final Bson indexProjectSubjectSourceTimestart = Indexes.ascending(
            KEY + "." + PROJECT_ID,
            KEY + "." + USER_ID,
            KEY + "." + SOURCE_ID,
            KEY + "." + START);

    private static final Bson indexProjectSubjectSource = Indexes.ascending(
            KEY + "." + PROJECT_ID,
            KEY + "." + USER_ID,
            KEY + "." + SOURCE_ID);

    /**
     * Finds whether document is available for given query parameters.
     * https://stackoverflow.com/a/8390458/822964 suggests find().limit(1).count(true) is the
     * optimal way to do it. In the Java driver, this is achieved by setting the count options.
     *
     * @param collection is the MongoDB that will be queried
     * @param projectName of the project
     * @param subjectId is the subjectID
     * @param sourceId is the sourceID
     * @param timeFrame is the time frame of the queried timewindow
     * @return a MongoDB cursor containing all documents from the query.
     */
    public static boolean hasDataForSource(
            MongoCollection<Document> collection, String projectName, String subjectId,
            String sourceId, TimeFrame timeFrame) {
        createIndexIfNotAvailable(collection, indexProjectSubjectSourceTimestart);
        return collection.count(
                filterSource(projectName, subjectId, sourceId, timeFrame),
                new CountOptions().limit(1)) > 0;
    }

    /**
     * Finds all documents within a time window belonging to the given subject, source and project.
     * Close the returned iterator after use, for example with a try-with-resources construct.
     *
     * @param collection is the MongoDB that will be queried
     * @param projectName of the project
     * @param subjectId is the subjectID
     * @param sourceId is the sourceID
     * @param timeFrame the queried timewindow
     * @return a MongoDB cursor containing all documents from the query.
     */
    public static MongoCursor<Document> findDocumentsBySource(
            MongoCollection<Document> collection, String projectName, String subjectId,
            String sourceId, TimeFrame timeFrame) {
        createIndexIfNotAvailable(collection, indexProjectSubjectSourceTimestart);
        Bson querySource = filterSource(projectName, subjectId, sourceId, timeFrame);
        BasicDBObject sortStartTime = new BasicDBObject(KEY + "." + START, ASCENDING);

        if (logger.isDebugEnabled()) {
            BsonDocument findQueryDocument = querySource.toBsonDocument(
                    Document.class, collection.getCodecRegistry());
            logger.debug("Filtering query {} and sorting by {}", findQueryDocument, sortStartTime);
        }

        return collection
                .find(querySource)
                .sort(sortStartTime)
                .iterator();
    }

    /**
     * Finds all documents belonging to the given subject, source and project.
     * Close the returned iterator after use, for example with a try-with-resources construct.
     *
     * @param collection MongoDB collection name that will be queried
     * @param project project name
     * @param subject subject ID
     * @param source source ID
     * @param sortBy Field to sort by. If sortBy is {@code null}, the data will not be sorted.
     *               The field should be prefixed with {@link MongoHelper#KEY} or
     *               {@link MongoHelper#VALUE}.
     * @param order {@code 1} means ascending while {@code -1} means descending
     * @param limit is the number of document that will be retrieved. If the limit is {@code null},
     *              no limit is used.
     * @return a MongoDB cursor containing all documents from query.
     * @throws IllegalArgumentException if sortBy does not start with a key or value object.
     */
    public static MongoCursor<Document> findDocumentBySource(
            MongoCollection<Document> collection, String project, String subject, String source,
            String sortBy, int order, Integer limit) {

        createIndexIfNotAvailable(collection, indexProjectSubjectSource);

        FindIterable<Document> result = collection.find(filterSource(project, subject, source));

        if (sortBy != null) {
            if (!sortBy.startsWith(KEY + ".") && !sortBy.startsWith(VALUE + ".")) {
                throw new IllegalArgumentException(
                        "Should sort by a MongoHelper.KEY or MongoHelper.VALUE property.");
            }
            result = result.sort(new BasicDBObject(sortBy, order));
        }
        if (limit != null) {
            result = result.limit(limit);
        }

        return result.iterator();
    }

    private static Bson filterSource(String projectName, String subjectId, String sourceId) {
        return and(eq(KEY + "." + PROJECT_ID, projectName),
                eq(KEY + "." + USER_ID, subjectId),
                eq(KEY + "." + SOURCE_ID, sourceId));
    }

    private static Bson filterSource(String projectName, String subjectId, String sourceId,
            TimeFrame timeFrame) {
        return and(eq(KEY + "." + PROJECT_ID, projectName),
                eq(KEY + "." + USER_ID, subjectId),
                eq(KEY + "." + SOURCE_ID, sourceId),
                gte(KEY + "." + START, Date.from(timeFrame.getStartDateTime())),
                lt(KEY + "." + START, Date.from(timeFrame.getEndDateTime())));
    }

    /**
     * Returns the needed MongoDB collection.
     *
     * @param client the MongoDB client
     * @param collection is the name of the returned connection
     * @return the MongoDB collection named collection.
     */
    public static MongoCollection<Document> getCollection(MongoClient client, String collection) {
        return client.getDatabase(Properties.getApiConfig().getMongoDbName())
                .getCollection(collection);
    }

    /**
     * Creates given index on the collection, if it is not already created for given collection.
     * Having indexes created in background prevents other operations being blocked.
     * @param collection mongoCollection to index.
     * @param index index to be created.
     */
    private static void createIndexIfNotAvailable(MongoCollection<Document> collection,
            Bson index) {
        if (indexMap.containsKey(collection.getNamespace().getCollectionName())) {
            List<String> availableIndexes = indexMap.get(collection.getNamespace()
                    .getCollectionName());
            if (!availableIndexes.contains(index.toString())) {
                collection.createIndex(index, new IndexOptions().background(true));
                availableIndexes.add(index.toString());
            }

        } else {
            collection.createIndex(index, new IndexOptions().background(true));
            List<String> indexList = new ArrayList<>();
            indexList.add(index.toString());
            indexMap.put(collection.getNamespace().getCollectionName(), indexList);
        }
    }

    /**
     * Enumerate all available statistical values. The string value represents the Document field
     * that has to be used to compute the result.
     */
    public enum Stat {
        avg("avg"),
        count("count"),
        iqr("iqr"),
        max("max"),
        median("quartile"),
        min("min"),
        quartile("quartile"),
        receivedMessage("count"),
        sum("sum");

        private final String param;

        Stat(String param) {
            this.param = param;
        }

        public String getParam() {
            return param;
        }
    }
}