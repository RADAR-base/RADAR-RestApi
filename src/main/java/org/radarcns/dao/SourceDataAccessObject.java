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
import javax.servlet.ServletContext;
import org.radarcns.mongo.util.MongoDataAccess;
import org.radarcns.mongo.util.MongoHelper;

/**
 * Data Access Object for subject management.
 */
public class SourceDataAccessObject {


    private SensorDataAccessObject sensorDataAccessObject ;

    public SourceDataAccessObject(SensorDataAccessObject sensorDataAccessObject) {
        this.sensorDataAccessObject = sensorDataAccessObject;
    }
    /**
     * Given a sourceID, it finds what is the associated source type.
     *
     * @param source is the SourceID
     * @param context {@link ServletContext} used to retrieve the client for accessing the
     *      results cache
     * @return source type associated with the given source
     *
     * @throws ConnectException if MongoDb instance is not available
     */
    public String getSourceType(String source, ServletContext context)
            throws ConnectException {
        return getSourceType(source, MongoHelper.getClient(context));
    }

    /**
     * Given a sourceID, it finds what is the associated source type.
     *
     * @param source is the SourceID
     * @param client MongoDB client
     * @return source type associated with the given source
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
