package org.radarcns.dao;

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

import com.mongodb.MongoClient;
import java.net.ConnectException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import org.radarcns.avro.restapi.source.Source;
import org.radarcns.avro.restapi.source.SourceType;
import org.radarcns.avro.restapi.user.Patient;
import org.radarcns.dao.mongo.util.MongoDataAccess;

/**
 * Data Access Object for user management.
 */
public class SourceDataAccessObject {

    /**
     * Given a sourceID, it finds what is the associated source type.
     *
     * @param source is the SourceID
     * @param client MongoDB client
     * @return {@code SourceType} associated with the given source
     *
     * @throws ConnectException if MongoDb instance is not available
     */
    public static SourceType getSourceType(String source, MongoClient client)
            throws ConnectException {
        SourceType type = MongoDataAccess.getSourceType(source, client);

        if (type == null) {
            type = SensorDataAccessObject.getInstance().findSourceType(source, client);

            if (type == null) {
                type = AndroidAppDataAccessObject.getInstance().findSourceType(source, client);
            }

            if (type != null) {
                MongoDataAccess.writeSourceType(source, type, client);
            }
        }

        return type;
    }


    /**
     * Returns all available sources for the given patient.
     *
     * @param user user identifier.
     * @param client MongoDb client
     * @return a {@code Patient} object
     * @throws ConnectException if MongoDB is not available
     *
     * @see {@link org.radarcns.avro.restapi.user.Patient}
     */
    public static Patient findAllSourcesByUser(String user, MongoClient client)
            throws ConnectException {
        Set<Source> sources = new HashSet<>();

        sources.addAll(SensorDataAccessObject.getInstance().findAllSourcesByUser(user, client));
        sources.addAll(AndroidAppDataAccessObject.getInstance().findAllSoucesByUser(user, client));

        return new Patient(user, new LinkedList<>(sources));
    }

}
