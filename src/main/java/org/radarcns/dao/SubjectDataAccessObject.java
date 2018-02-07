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
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.radarcns.domain.restapi.Source;
import org.radarcns.domain.restapi.Subject;
import org.radarcns.service.SourceMonitorService;

/**
 * Data Access Object for user management.
 */
public class SubjectDataAccessObject {

    //private static final Logger LOGGER = LoggerFactory.getLogger(SubjectDataAccessObject.class);
    private SensorDataAccessObject sensorDataAccessObject;

    private SourceMonitorService sourceMonitorService;

    @Inject
    public SubjectDataAccessObject(SensorDataAccessObject sensorDataAccessObject,
             SourceMonitorService sourceMonitorService) {
        this.sensorDataAccessObject = sensorDataAccessObject;
        this.sourceMonitorService = sourceMonitorService;
    }

    /**
     * Finds all subjects checking all available collections.
     *
     * @param client {@link MongoClient} used to connect to the database
     * @return a list of  {@link Subject}
     * @throws ConnectException if MongoDB is not available
     *
     * @see Subject
     */
    public  List<Subject> getAllSubjects(MongoClient client) throws ConnectException {

        List<Subject> patients = new LinkedList<>();

        Set<String> subjects = new HashSet<>(this.sensorDataAccessObject.getAllSubject(client));

        subjects.addAll(AndroidAppDataAccessObject.getInstance().findAllUser(client));

        for (String user : subjects) {
            patients.add(findAllSourcesByUser(user, client));
        }

        return patients;
    }


    /**
     * Finds all subjects checking all available collections.
     *
     * @param subjectId Subject Identifier
     * @param client {@link MongoClient} used to connect to the database
     * @return a {@link Subject}
     * @throws ConnectException if MongoDB is not available
     *
     * @see Subject
     */
    public  Subject getSubject(String subjectId, MongoClient client) throws ConnectException {
        return this.findAllSourcesByUser(subjectId, client);
    }

    /**
     * Returns if the subject is active or not. If he/she is still active than it means he/she
     *      is still enrolled in some studies.
     *
     * @param subject Subject identifier
     *
     * @return {@code boolean}, {@code true} if the patient is still active meaning that he/she
     *      is still enrolled in some studies. {@code false} otherwise.
     */
    public  boolean isSubjectActive(String subject) {
        //TODO must be integrated with the suggested user management tool.
        return true;
    }

    /**
     * Checks if the subject exists.
     *
     * @param subject Subject identifier
     * @param client {@link MongoClient} used to connect to the database
     *
     * @return {@code true} if exist, {@code false} otherwise
     */
    public  boolean exist(String subject, MongoClient client) throws ConnectException {
        //TODO Temporary implementation. It must integrated with the suggested user management tool.
        return !this.findAllSourcesByUser(subject, client).getSources().isEmpty();
    }

    /**
     * Returns all available sources for the given patient.
     *
     * @param subject subject identifier.
     * @param client MongoDb client
     * @return a {@code Subject} object
     * @throws ConnectException if MongoDB is not available
     */
    public  Subject findAllSourcesByUser(String subject, MongoClient client)
            throws ConnectException {
        Set<Source> sources = new HashSet<>();

        sources.addAll(this.sensorDataAccessObject.getAllSources(
                subject, client));
        sources.addAll(AndroidAppDataAccessObject.getInstance().findAllSourcesBySubject(
                subject, client));



        List<Source> updatedSources = new ArrayList<>(sources.size());

        for (Source source : sources) {
            try {
//                updatedSources.add(monitor.getState(subject, source.getSourceId(), new
//                                SourceTypeIdentifier(source.getSourceTypeProducer(), source
//                                .getSourceTypeModel() , source.getSourceTypeCatalogVersion()).toString(),
//                        client));
            } catch (UnsupportedOperationException ex) {
                updatedSources.add(source);
            }
        }

        return new Subject(subject, isSubjectActive(subject),
                updatedSources);
    }

}
