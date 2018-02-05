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

package org.radarcns.dao;

import com.mongodb.MongoClient;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.radarcns.dao.mongo.util.MongoDataAccess;
import org.radarcns.monitor.Monitors;
import org.radarcns.restapi.source.Source;
import org.radarcns.restapi.subject.Subject;

/**
 * Data Access Object for subject management.
 */
public class SourceDataAccessObject {
    /**
     * Given a sourceID, it finds what is the associated source type.
     *
     * @param source is the SourceID
     * @param client MongoDB client
     * @return source type associated with the given source
     */
    public static String getSourceType(String source, MongoClient client) {
        String type = MongoDataAccess.getSourceType(source, client);

        if (type == null) {
            type = SensorDataAccessObject.getInstance().getSourceType(source, client);

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
     * @param subject subject identifier.
     * @param client MongoDb client
     * @return a {@code Subject} object
     */
    public static Subject findAllSourcesByUser(String subject, MongoClient client) {
        Set<Source> sources = new HashSet<>();

        sources.addAll(SensorDataAccessObject.getInstance().getAllSources(
                subject, client));
        sources.addAll(AndroidAppDataAccessObject.getInstance().findAllSourcesBySubject(
                subject, client));

        Monitors monitor = Monitors.getInstance();

        List<Source> updatedSources = new ArrayList<>(sources.size());

        for (Source source : sources) {
            try {
                updatedSources.add(monitor.getState(client, subject, source.getId(),
                        source.getType()));
            } catch (NoSuchElementException ex) {
                updatedSources.add(source);
            }
        }

        return new Subject(subject, SubjectDataAccessObject.isSubjectActive(subject),
                SensorDataAccessObject.getInstance().getEffectiveTimeFrame(subject, client),
                updatedSources);
    }

}
