package org.radarcns.dao.mongo.data.android;

import org.bson.Document;
import org.radarcns.avro.restapi.app.Application;
import org.radarcns.dao.mongo.util.MongoAndroidApp;
import org.slf4j.LoggerFactory;

/*
 *  Copyright 2016 King's College London and The Hyve
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
public class AndroidRecordCounter extends MongoAndroidApp {

    public static final String RECORD_COLLECTION = "application_record_counts";

    public AndroidRecordCounter() {}

    //TODO take field names from schemas
    @Override
    protected Application getApplication(Document doc, Application app) {
        app.setRecordsCached(doc.getInteger("recordsCached"));
        app.setRecordsSent(doc.getInteger("recordsSent"));
        app.setRecordsUnsent(doc.getInteger("recordsUnsent"));

        return app;
    }

    @Override
    protected String getCollectionName() {
        return RECORD_COLLECTION;
    }
}
