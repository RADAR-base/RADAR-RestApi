package org.radarcns.dao.mongo.util;

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

import static org.radarcns.dao.mongo.util.MongoHelper.SOURCE_TYPE;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import java.net.ConnectException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.bson.Document;
import org.radarcns.avro.restapi.header.TimeFrame;
import org.radarcns.avro.restapi.source.Source;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.util.RadarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic function for user management.
 */
public abstract class MongoDataAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDataAccess.class);

    /**
     * Finds all users.
     *
     * @return all distinct userIDs for the current DAO instance, otherwise an empty Collection
     */
    public Collection<String> findAllUser(MongoClient client) throws ConnectException {
        Set<String> set = new HashSet<>();

        MongoCursor<String> cursor;
        for (String collection : getCollectionNames()) {
            cursor = MongoHelper.findAllUser(MongoHelper.getCollection(client, collection));

            if (!cursor.hasNext()) {
                LOGGER.debug("Empty cursor for collection {}", collection);
            }

            while (cursor.hasNext()) {
                set.add(cursor.next());
            }

            cursor.close();
        }

        return set;
    }

    /**
     * Finds all sources for the given user.
     *
     * @param user is the userID
     * @return all distinct sourceIDs for the given collection, otherwise empty Collection
     */
    public Collection<Source> findAllSourcesByUser(String user, MongoClient client)
            throws ConnectException {
        Set<Source> list = new HashSet<>();

        MongoCursor<String> cursor;
        for (String collection : getCollectionNames()) {
            cursor = MongoHelper.findAllSourceByUser(user,
                    MongoHelper.getCollection(client, collection));

            if (!cursor.hasNext()) {
                LOGGER.debug("Empty cursor");
            }

            while (cursor.hasNext()) {
                list.add(new Source(cursor.next(), getSourceType(collection), null));
            }

            cursor.close();
        }
        return list;
    }

    /**
     * Finds source type for the given user checking all available source device collections.
     *
     * @param source is the sourceID
     * @return source type for the given sourceID, otherwise null
     */
    public SourceType findSourceType(String source, MongoClient client)
            throws ConnectException {
        SourceType type;

        MongoCursor<Document> cursor;
        for (String collection : getCollectionNames()) {

            cursor = MongoHelper.findDocumentBySource(source, null, 0, 1,
                MongoHelper.getCollection(client, collection));

            if (cursor.hasNext()) {
                type = getSourceType(collection);
                if (type != null) {
                    return type;
                }
            }

            cursor.close();
        }

        return null;
    }

    /**
     * Writes the source type on the source catalog.
     *
     * @param source is the sourceID
     * @param type the source type that is assigned to the sourceID
     * @param client MongoDb client
     *
     * @throws ConnectException if MongoDB is not available
     * @throws MongoException if something goes wrong with the write
     */
    public static void writeSourceType(String source, SourceType type, MongoClient client)
            throws ConnectException, MongoException {
        Document doc = new Document().append("_id", source).append(SOURCE_TYPE, type.toString());

        MongoCollection<Document> collection = MongoHelper.getCollection(client,
                MongoHelper.DEVICE_CATALOG);

        collection.insertOne(doc);
    }

    /**
     * Returns the current collection.
     *
     * @param client is the MongoDb client instance
     * @param interval useful to identify which collection has to be queried. A sensor has a
     *      collection for each time frame or time window
     * @return the MongoDb collection
     */
    protected MongoCollection<Document> getCollection(MongoClient client, SourceType source,
            TimeFrame interval) throws ConnectException {
        return MongoHelper.getCollection(client,getCollectionName(source, interval));
    }

    /**
     * Finds source type for the given user using the source catalog.
     *
     * @param source is the sourceID
     * @return source type for the given sourceID, otherwise null
     */
    public static SourceType getSourceType(String source, MongoClient client)
        throws ConnectException {
        SourceType type = null;

        MongoCursor<Document> cursor = MongoHelper.findDocumentById(source, null,
                0, 1, MongoHelper.getCollection(client, MongoHelper.DEVICE_CATALOG));

        if (!cursor.hasNext()) {
            LOGGER.debug("Empty cursor");
        }

        Document doc = cursor.tryNext();

        if (doc != null) {
            type = RadarConverter.getSourceType(doc.getString(SOURCE_TYPE));
        }

        cursor.close();

        return type;
    }

    /**
     * @implSpec this function must be override by the subclass.
     * @return covert collection name to the source type.
     */
    public abstract SourceType getSourceType(String collection);

    /**
     * @implSpec this function must be override by the subclass.
     * @return the MongoDB Collection name associated to the source type for the given time frame
     */
    public abstract String getCollectionName(SourceType source, TimeFrame interval);

    /**
     * @implSpec this function must be override by the subclass.
     * @return all available collection for the current instance.
     */
    public abstract Set<String> getCollectionNames();

}
