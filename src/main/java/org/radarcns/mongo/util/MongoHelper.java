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

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lte;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import java.util.Date;
import org.bson.Document;
import org.radarcns.config.Properties;

/**
 * Generic MongoDB helper.
 */
public class MongoHelper {

    //private static final Logger logger = LoggerFactory.getLogger(MongoHelper.class);

    public static final String ID = "_id";
    public static final String KEY = "key";
    public static final String VALUE = "value";
    public static final String USER_ID = "userId";
    public static final String SOURCE_ID = "sourceId";
    public static final String PROJECT_ID = "projectId";
    public static final String START = "timeStart";
    public static final String END = "timeEnd";
    public static final String SOURCE_TYPE = "sourceType";
    public static final String FIELDS = "fields";
    public static final String QUARTILE = "quartile";
    public static final String COUNT = "count";

    public static final String FIRST_QUARTILE = "25";
    public static final String SECOND_QUARTILE = "50";
    public static final String THIRD_QUARTILE = "75";


    public static final int ASCENDING = 1;
    public static final int DESCENDING = -1;

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

    /**
     * Finds all Documents within [start-end] belonging to the given subject for the give
     * sourceType.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param start is the start time of the queried timewindow
     * @param end is the end time of the queried timewindow
     * @param collection is the MongoDB that will be queried
     * @return a MongoDB cursor containing all documents from the query.
     */
    public static MongoCursor<Document> findDocumentByUserSourceWindow(String subject,
            String source, Long start, Long end, MongoCollection<Document> collection) {
        FindIterable<Document> result = collection.find(
                Filters.and(
                        eq(USER_ID, subject),
                        eq(SOURCE_ID, source),
                        gte(START, new Date(start)),
                        lte(END, new Date(end)))).sort(new BasicDBObject(START, 1));

        return result.iterator();
    }

    /**
     * Finds all Documents within [start-end] belonging to the given subject for the give
     * sourceType.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param start is the start time of the queried timewindow
     * @param end is the end time of the queried timewindow
     * @param collection is the MongoDB that will be queried
     * @return a MongoDB cursor containing all documents from the query.
     */
    public static MongoCursor<Document> findDocumentsByProjectAndSubjectAndSourceInWindow(
            String projectName, String subject, String source, Long start, Long end,
            MongoCollection<Document> collection) {
        FindIterable<Document> result = collection.find(
                Filters.and(
                        eq(PROJECT_ID, projectName),
                        eq(USER_ID, subject),
                        eq(SOURCE_ID, source),
                        gte(START, new Date(start)),
                        lte(END, new Date(end)))).sort(new BasicDBObject(START, 1));

        return result.iterator();
    }

    /**
     * Finds all Documents belonging to the given subject for the give sourceType.
     *
     * @param project is the projectName
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param sortBy states the way in which documents have to be sorted. It is optional. {@code 1}
     * means ascending while {@code -1} means descending
     * @param limit is the number of document that will be retrieved
     * @param collection is the MongoDB that will be queried
     * @return a MongoDB cursor containing all documents from query.
     */
    public static MongoCursor<Document> findDocumentByProjectAndSubjectAndSource(String
            project, String subject, String source, String sortBy, int order, Integer limit,
            MongoCollection<Document> collection) {
        FindIterable<Document> result;

        if (sortBy == null) {
            result = collection.find(
                    Filters.and(
                            eq(USER_ID, subject),
                            eq(SOURCE_ID, source),
                            eq(PROJECT_ID, project)));
        } else {
            result = collection.find(
                    Filters.and(
                            eq(USER_ID, subject),
                            eq(SOURCE_ID, source),
                            eq(PROJECT_ID, project))
            ).sort(new BasicDBObject(sortBy, order));
        }

        if (limit != null) {
            result = result.limit(limit);
        }

        return result.iterator();
    }

    /**
     * Finds all Documents belonging to the given subject for the give sourceType.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param sortBy states the way in which documents have to be sorted. It is optional. {@code 1}
     * means ascending while {@code -1} means descending
     * @param limit is the number of document that will be retrieved
     * @param collection is the MongoDB that will be queried
     * @return a MongoDB cursor containing all documents for the given User, SourceDefinition and
     * MongoDB collection
     */
    public static MongoCursor<Document> findDocumentBySubjectAndSource(String subject,
            String source, String sortBy, int order, Integer limit,
            MongoCollection<Document> collection) {
        FindIterable<Document> result;

        if (sortBy == null) {
            result = collection.find(
                    Filters.and(
                            eq(USER_ID, subject),
                            eq(SOURCE_ID, source)));
        } else {
            result = collection.find(
                    Filters.and(
                            eq(USER_ID, subject),
                            eq(SOURCE_ID, source))
            ).sort(new BasicDBObject(sortBy, order));
        }

        if (limit != null) {
            result = result.limit(limit);
        }

        return result.iterator();
    }

    /**
     * Finds all Documents belonging to the given sourceType.
     *
     * @param source is the sourceID
     * @param sortBy states the way in which documents have to be sorted. It is optional. {@code 1}
     * means ascending while {@code -1} means descending
     * @param limit is the number of document that will be retrieved
     * @param collection is the MongoDB that will be queried
     * @return a MongoDB cursor containing all documents from the query.
     */
    protected static MongoCursor<Document> findDocumentBySource(String source,
            String sortBy, int order, Integer limit, MongoCollection<Document> collection) {
        FindIterable<Document> result;

        if (sortBy == null) {
            result = collection.find(
                    Filters.and(
                            eq(SOURCE_ID, source)));
        } else {
            result = collection.find(
                    Filters.and(
                            eq(SOURCE_ID, source))
            ).sort(new BasicDBObject(sortBy, order));
        }

        if (limit != null) {
            result = result.limit(limit);
        }
        return result.iterator();
    }

    /**
     * Finds document with the given ID.
     *
     * @param id Document _id
     * @param sortBy states the way in which documents have to be sorted. It is optional. {@code 1}
     * means ascending while {@code -1} means descending
     * @param limit is the number of document that will be retrieved
     * @param collection is the MongoDB that will be queried
     * @return a MongoDB cursor containing all documents from query.
     */
    protected static MongoCursor<Document> findDocumentById(String id, String sortBy, int order,
            Integer limit, MongoCollection<Document> collection) {
        FindIterable<Document> result;

        if (sortBy == null) {
            result = collection.find(
                    Filters.and(
                            eq(ID, id)));
        } else {
            result = collection.find(
                    Filters.and(
                            eq(ID, id))
            ).sort(new BasicDBObject(sortBy, order));
        }

        if (limit != null) {
            result = result.limit(limit);
        }
        return result.iterator();
    }

    /**
     * Finds all subjects.
     *
     * @param collection is the MongoDB that will be queried
     * @return a MongoDB cursor containing all distinct subjects for the given MongoDB collection
     */
    public static MongoCursor<String> findAllUser(MongoCollection<Document> collection) {
        return collection.distinct(USER_ID, String.class).iterator();
    }

    /**
     * Finds all sources for the given subject.
     *
     * @param subject is the subjectID
     * @param collection is the MongoDB that will be queried
     * @return a MongoDB cursor containing all distinct sources for the given User and MongoDB
     * collection
     */
    public static MongoCursor<String> findAllSourceByUser(String subject,
            MongoCollection<Document> collection) {
        return collection.distinct(SOURCE_ID, String.class)
                .filter(eq(USER_ID, subject)).iterator();
    }

    /**
     * Finds all sources for the given subject and projectId.
     *
     * @param subject is the subjectID
     * @param collection is the MongoDB that will be queried
     * @return a MongoDB cursor containing all distinct sources for the given User and MongoDB
     * collection
     */
    public static MongoCursor<String> findAllSourcesBySubjectAndProject(String subject, String
            projectName,
            MongoCollection<Document> collection) {
        return collection.distinct(SOURCE_ID, String.class)
                .filter(Filters.and(eq(USER_ID, subject), eq(PROJECT_ID, projectName))).iterator();
    }


    /**
     * Returns the needed MongoDB collection.
     *
     * @param client the MongoDB client
     * @param collection is the name of the returned connection
     * @return the MongoDB collection named collection.
     */
    public static MongoCollection<Document> getCollection(MongoClient client, String collection) {
        MongoDatabase database = client.getDatabase(Properties.getApiConfig().getMongoDbName());

        return database.getCollection(collection);
    }
}