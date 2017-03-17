package org.radarcns.config;

/*
 *  Copyright 2016 Kings College London and The Hyve
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

import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.util.List;

/**
 * Configuration abstraction.
 */
public interface RadarConfig {
    /**
     * Returns the list of all known MongoDB instances.
     * @return MongoDB instances as List
     */
    List<ServerAddress> getMongoDbHosts();

    /**
     * Returns the list of all known MongoDB credentials.
     * @return MongoDB credentials as List
     */
    List<MongoCredential> getMongoDbCredentials();

    /**
     * Returns a String representing the MongoDB database name.
     * @return MongoDB database name as String
     */
    String getMongoDbName();
}
