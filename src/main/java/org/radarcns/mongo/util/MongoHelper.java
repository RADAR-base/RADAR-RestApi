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

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
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
    public static final String FIELDS = "fields";
    public static final String QUARTILE = "quartile";
    public static final String COUNT = "count";
    public static final String NAME = "name";


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
     * Finds all documents within a time window belonging to the given subject, source and project.
     *
     * @param projectName of the project
     * @param subjectId is the subjectID
     * @param sourceId is the sourceID
     * @param start is the start time of the queried timewindow
     * @param end is the end time of the queried timewindow
     * @param collection is the MongoDB that will be queried
     * @return a MongoDB cursor containing all documents from the query.
     */
    public static MongoCursor<Document> findDocumentsByProjectAndSubjectAndSourceInWindow(
            String projectName, String subjectId, String sourceId, Date start, Date end,
            MongoCollection<Document> collection) {
        BasicDBObject query = getByProjectSubjectSource(projectName, subjectId, sourceId)
                .append(KEY.concat(".").concat(START), new BasicDBObject("$gte" , start))
                .append(KEY.concat(".").concat(END), new BasicDBObject("$lte" , end));
        FindIterable<Document> result = collection.find(query).sort(new BasicDBObject(START, ASCENDING));

        return result.iterator();
    }

    /**
     * Finds all documents belonging to the given subject, source and project.
     *
     * @param project is the projectName
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param sortBy It is optional. {@code 1} means ascending while {@code -1} means descending
     * @param limit is the number of document that will be retrieved
     * @param collection is the MongoDB that will be queried
     * @return a MongoDB cursor containing all documents from query.
     */
    public static MongoCursor<Document> findDocumentByProjectAndSubjectAndSource(String
            project, String subject, String source, String sortBy, int order, Integer limit,
            MongoCollection<Document> collection) {
        FindIterable<Document> result;

        if (sortBy == null) {
            result = collection.find(getByProjectSubjectSource(project, subject, source));
        } else {
            result = collection.find(getByProjectSubjectSource(project, subject, source))
                    .sort(new BasicDBObject(sortBy, order));
        }

        if (limit != null) {
            result = result.limit(limit);
        }

        return result.iterator();
    }

    private static BasicDBObject getByProjectSubjectSource(String projectName, String subjectId,
            String sourceId) {
        return new BasicDBObject().append(KEY.concat(".").concat(PROJECT_ID), projectName)
                .append(KEY.concat(".").concat(USER_ID), subjectId)
                .append(KEY.concat(".").concat(SOURCE_ID), sourceId);

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