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

package org.radarcns.listener;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.util.List;
import org.glassfish.jersey.internal.inject.DisposableSupplier;
import org.radarcns.config.Properties;

/**
 * Factory to creates a singleton MongoClient with the correct credentials.
 */
public class MongoFactory implements DisposableSupplier<MongoClient> {

    @Override
    public MongoClient get() {
        MongoCredential credentials = Properties.getApiConfig().getMongoDbCredentials();
        List<ServerAddress> hosts = Properties.getApiConfig().getMongoDbHosts();

        return new MongoClient(hosts, credentials, MongoClientOptions.builder().build());
    }

    @Override
    public void dispose(MongoClient client) {
        if (client != null) {
            client.close();
        }
    }
}
