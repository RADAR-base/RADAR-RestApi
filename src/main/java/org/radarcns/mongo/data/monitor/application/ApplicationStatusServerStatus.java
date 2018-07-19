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

package org.radarcns.mongo.data.monitor.application;

import org.bson.Document;
import org.radarcns.domain.restapi.Application;
import org.radarcns.util.RadarConverter;

public class ApplicationStatusServerStatus extends MongoApplicationStatusWrapper {

    public static final String STATUS_COLLECTION = "application_server_status";

    //TODO take field names from RADAR MongoDb Connector
    @Override
    protected Application getApplication(Document doc, Application app) {
        app.setIpAddress(doc.getString("clientIP"));
        app.setServerStatus(RadarConverter.getServerStatus(doc.getString("serverStatus")));

        return app;
    }

    @Override
    public String getCollectionName() {
        return STATUS_COLLECTION;
    }

}
