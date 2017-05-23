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
import java.util.List;
import java.util.Set;
import javax.servlet.ServletContext;
import org.radarcns.avro.restapi.subject.Cohort;
import org.radarcns.avro.restapi.subject.Subject;
import org.radarcns.dao.mongo.util.MongoHelper;

/**
 * Data Access Object for user management.
 */
public class SubjectDataAccessObject {

    //private static final Logger LOGGER = LoggerFactory.getLogger(SubjectDataAccessObject.class);

    /**
     * Finds all subjects checking all available collections.
     *
     * @param context {@link ServletContext} used to retrieve the client for accessing the
     *      results cache
     * @return a study {@link Cohort}
     * @throws ConnectException if MongoDB is not available
     *
     * @see {@link Subject}
     */
    public static Cohort getAllSubjects(ServletContext context) throws ConnectException {
        return getAllSubjects(MongoHelper.getClient(context));
    }

    /**
     * Finds all subjects checking all available collections.
     *
     * @param client {@link MongoClient} used to connect to the database
     * @return a study {@link Cohort}
     * @throws ConnectException if MongoDB is not available
     *
     * @see {@link Subject}
     */
    public static Cohort getAllSubjects(MongoClient client) throws ConnectException {

        List<Subject> patients = new LinkedList<>();

        Set<String> subjects = new HashSet<>(
                SensorDataAccessObject.getInstance().getAllSubject(client));

        subjects.addAll(AndroidAppDataAccessObject.getInstance().findAllUser(client));

        for (String user : subjects) {
            patients.add(SourceDataAccessObject.findAllSourcesByUser(user, client));
        }

        return new Cohort(0, patients);
    }

    /**
     * Returns all information related to the given Subject identifier.
     *
     * @param subject Subject Identifier
     * @param context {@link ServletContext} used to retrieve the client for accessing the
     *      results cache
     * @return a study {@link Cohort}
     * @throws ConnectException if MongoDB is not available
     *
     * @see {@link Subject}
     */
    public static Subject getSubject(String subject, ServletContext context) throws ConnectException {
        return getSubject(subject, MongoHelper.getClient(context));
    }

    /**
     * Finds all subjects checking all available collections.
     *
     * @param subjectId Subject Identifier
     * @param client {@link MongoClient} used to connect to the database
     * @return a study {@link Cohort}
     * @throws ConnectException if MongoDB is not available
     *
     * @see {@link Subject}
     */
    public static Subject getSubject(String subjectId, MongoClient client) throws ConnectException {
        return SourceDataAccessObject.findAllSourcesByUser(subjectId, client);
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
    public static boolean isSubjectActive(String subject) {
        //TODO must be integrated with the suggested user management tool.
        return true;
    }

    /**
     * Checks if the subject exists.
     *
     * @param subject Subject identifier
     * @param context {@link ServletContext} used to retrieve the client for the subject management
     *      tool
     *
     * @return {@code true} if exist, {@code false} otherwise
     *
     * @throws ConnectException if the connection with MongoDb cannot be established
     */
    public static boolean exist(String subject, ServletContext context) throws ConnectException {
        MongoClient client = MongoHelper.getClient(context);

        return exist(subject, client);
    }

    /**
     * Checks if the subject exists.
     *
     * @param subject Subject identifier
     * @param client {@link MongoClient} used to connect to the database
     *
     * @return {@code true} if exist, {@code false} otherwise
     *
     * @throws ConnectException if the connection with MongoDb cannot be established
     */
    public static boolean exist(String subject, MongoClient client) throws ConnectException {
        //TODO Temporary implementation. It must integrated with the suggested user management tool.
        return !SourceDataAccessObject.findAllSourcesByUser(subject, client).getSources().isEmpty();
    }

}
