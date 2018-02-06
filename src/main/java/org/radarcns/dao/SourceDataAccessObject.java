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
import javax.inject.Inject;
import org.radarcns.mongo.util.MongoDataAccess;

/**
 * Data Access Object for subject management.
 */
public class SourceDataAccessObject {


    private SensorDataAccessObject sensorDataAccessObject ;

    @Inject
    public SourceDataAccessObject(SensorDataAccessObject sensorDataAccessObject) {
        this.sensorDataAccessObject = sensorDataAccessObject;
    }

    /**
     * Given a sourceID, it finds what is the associated sourceType type.
     *
     * @param source is the SourceID
     * @param client MongoDB client
     * @return sourceType type associated with the given sourceType
     *
     * @throws ConnectException if MongoDb instance is not available
     */
    public String getSourceType(String source, MongoClient client)
            throws ConnectException {
        String type = MongoDataAccess.getSourceType(source, client);

        if (type == null) {
            type = this.sensorDataAccessObject.getSourceType(source, client);

            if (type == null) {
                type = AndroidAppDataAccessObject.getInstance().findSourceType(source, client);
            }

            if (type != null) {
                MongoDataAccess.writeSourceType(source, type, client);
            }
        }

        return type;
    }


}
