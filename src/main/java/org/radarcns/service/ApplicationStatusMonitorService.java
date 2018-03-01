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

package org.radarcns.service;

import com.mongodb.MongoClient;
import java.util.LinkedList;
import java.util.List;
import javax.inject.Inject;
import org.radarcns.domain.restapi.Application;
import org.radarcns.mongo.data.monitor.ApplicationStatusRecordCounter;
import org.radarcns.mongo.data.monitor.ApplicationStatusServerStatus;
import org.radarcns.mongo.data.monitor.ApplicationStatusUpTime;
import org.radarcns.mongo.data.monitor.MongoApplicationStatusWrapper;

/**
 * Data Access Object for Android App Status values.
 */
public class ApplicationStatusMonitorService {


    private final List<MongoApplicationStatusWrapper> dataAccessObjects;

    /**
     * Default constructor. Initiates all the delegate classes to compute Application Status.
     */
    @Inject
    public ApplicationStatusMonitorService() {
        //TODO simplify processing application status
        dataAccessObjects = new LinkedList<>();
        dataAccessObjects.add(new ApplicationStatusUpTime());
        dataAccessObjects.add(new ApplicationStatusRecordCounter());
        dataAccessObjects.add(new ApplicationStatusServerStatus());
    }

    /**
     * Computes the Android App Status realign on different collection.
     *
     * @param project of the subject
     * @param subject identifier
     * @param source identifier
     * @param client is the MongoDb client
     * @return {@code Application} representing the status of the related Android App
     */
    public Application getStatus(String project, String subject, String source, MongoClient
            client) {
        Application app = null;

        for (MongoApplicationStatusWrapper dataAccessObject : dataAccessObjects) {
            app = dataAccessObject
                    .valueByProjectSubjectSource(project, subject, source, app, client);
        }

        return app;
    }
}