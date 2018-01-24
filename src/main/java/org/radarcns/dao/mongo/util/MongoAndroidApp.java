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

package org.radarcns.dao.mongo.util;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import java.net.ConnectException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.bson.Document;
import org.radarcns.catalogue.TimeWindow;
import org.radarcns.restapi.app.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic DAO to return Android App status information.
 */
public abstract class MongoAndroidApp extends MongoDataAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoAndroidApp.class);

    /**
     * Returns an {@code Application} initialised with the extracted value.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param client is the mongoDb client instance
     * @return the last seen status update for the given subject and source, otherwise null
     */
    public Application valueBySubjectSource(String subject, String source, Application app,
            MongoClient client) throws ConnectException {

        MongoCursor<Document> cursor = MongoHelper
                .findDocumentBySubjectAndSource(subject, source, null, -1, 1,
                    MongoHelper.getCollection(client, getCollectionName()));

        if (!cursor.hasNext()) {
            LOGGER.debug("Empty cursor");
            cursor.close();
            return null;
        }

        Document doc = cursor.next();
        cursor.close();

        if (app == null) {
            return getApplication(doc, new Application());
        }

        return getApplication(doc, app);

    }

    protected abstract Application getApplication(Document doc, Application app);

    protected abstract String getCollectionName();

    @Override
    public String getCollectionName(String source, TimeWindow interval) {
        return null;
    }

    @Override
    public Set<String> getCollectionNames() {
        return new HashSet<>(Collections.singleton(getCollectionName()));
    }

    @Override
    public String getSourceType(String collection) {
        return "ANDROID";
    }

}