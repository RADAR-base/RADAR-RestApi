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

import javax.inject.Inject;
import org.glassfish.jersey.internal.inject.DisposableSupplier;
import org.radarcns.config.ApplicationConfig;
import org.radarcns.mongo.util.MongoWrapper;

/**
 * Factory to creates a singleton MongoClient with the correct credentials.
 */
public class MongoFactory implements DisposableSupplier<MongoWrapper> {

    private final ApplicationConfig config;

    @Inject
    public MongoFactory(ApplicationConfig config) {
        this.config = config;
    }

    @Override
    public MongoWrapper get() {
        return new MongoWrapper(config);
    }

    @Override
    public void dispose(MongoWrapper client) {
        if (client != null) {
            client.close();
        }
    }
}
