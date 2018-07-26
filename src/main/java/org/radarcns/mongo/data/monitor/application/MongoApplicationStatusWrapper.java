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

package org.radarcns.mongo.data.monitor.application;

import static org.radarcns.mongo.util.MongoHelper.ASCENDING;
import static org.radarcns.mongo.util.MongoHelper.VALUE;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.radarcns.domain.restapi.monitor.ApplicationStatus;
import org.radarcns.mongo.util.MongoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic DAO to return Android App status information.
 */
public abstract class MongoApplicationStatusWrapper {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(MongoApplicationStatusWrapper.class);

    /**
     * Returns an {@code ApplicationStatus} initialised with the extracted value.
     *
     * @param subject is the subjectID
     * @param source is the sourceID
     * @param client is the mongoDb client instance
     * @return the last seen status update for the given subject and sourceType, otherwise null
     */
    public ApplicationStatus valueByProjectSubjectSource(String project, String subject,
            String source, ApplicationStatus app, MongoClient client) {

        MongoCursor<Document> cursor = MongoHelper
                .findDocumentBySource(MongoHelper.getCollection(client, getCollectionName()),
                        project, subject, source, VALUE + ".time", ASCENDING,
                        1);

        if (!cursor.hasNext()) {
            LOGGER.debug("Empty cursor");
            cursor.close();
            return null;
        }

        Document doc = cursor.next();
        cursor.close();

        if (app == null) {
            return getApplication((Document) doc.get(VALUE), new ApplicationStatus());
        }

        return getApplication((Document) doc.get(VALUE), app);

    }

    protected abstract ApplicationStatus getApplication(Document doc, ApplicationStatus app);

    public abstract String getCollectionName();


}