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
import org.radarcns.avro.restapi.user.Cohort;
import org.radarcns.avro.restapi.user.Patient;

/**
 * Data Access Object for user management.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class UserDataAccessObject {

    //private static final Logger logger = LoggerFactory.getLogger(UserDataAccessObject.class);

    /**
     * Finds all users checking all available collections.
     *
     * @param client MongoDB client
     * @return a study {@code Cohort}
     * @throws ConnectException if MongoDB is not available
     *
     * @see Cohort
     */
    public static Cohort findAllUsers(MongoClient client) throws ConnectException {

        List<Patient> patients = new LinkedList<>();

        Set<String> users = new HashSet<>(
                SensorDataAccessObject.getInstance().findAllUsers(client));

        users.addAll(AndroidAppDataAccessObject.getInstance().findAllUser(client));

        for (String user : users) {
            patients.add(SourceDataAccessObject.findAllSourcesByUser(user, client));
        }

        return new Cohort(0, patients);
    }

}
