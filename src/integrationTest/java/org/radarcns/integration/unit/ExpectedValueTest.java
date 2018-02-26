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

package org.radarcns.integration.unit;

import static org.junit.Assert.assertEquals;
import static org.radarcns.domain.restapi.header.DescriptiveStatistic.COUNT;
import static org.radarcns.mongo.util.MongoHelper.KEY;
import static org.radarcns.mongo.util.MongoHelper.VALUE;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.junit.Test;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.dataset.DataItem;
import org.radarcns.domain.restapi.dataset.Dataset;
import org.radarcns.domain.restapi.header.TimeFrame;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.mongo.util.MongoHelper;
import org.radarcns.mongo.util.MongoHelper.Stat;

/**
 * ExpectedValueTest Test.
 */
public class ExpectedValueTest {

    private static final String SUBJECT = "UserID_0";
    private static final String SOURCE = "SourceID_0";
    private static final String PROJECT = "radar";
    private static final TimeWindow TIME_WINDOW = TimeWindow.TEN_SECOND;
    private static final int SAMPLES = 10;

    @Test
    public void matchDatasetOnDocuments() throws Exception {
        Map<String, Object> map = RandomInput.getDatasetAndDocumentsRandom(PROJECT, SUBJECT, SOURCE,
                "empatica_e4_v1", "HEART_RATE", COUNT, TIME_WINDOW, SAMPLES, false, Instant.now());

        List<Document> docs = (List<Document>) map.get(RandomInput.DOCUMENTS);
        int count = 0;
        for (Document doc : docs) {

            count += ((Document) doc.get(VALUE)).getDouble(Stat.count.getParam()).intValue();
        }
        assertEquals(SAMPLES, count);

        Dataset dataset = (Dataset) map.get(RandomInput.DATASET);

        count = 0;
        for (DataItem item : dataset.getDataset()) {
            count += (Double) item.getValue();
        }
        assertEquals(SAMPLES, count);

        TimeFrame window1 = new TimeFrame(
                ((Document) docs.get(0).get(KEY)).getDate(MongoHelper.START),
                ((Document) docs.get(docs.size() - 1).get(KEY)).getDate(MongoHelper.END));

        TimeFrame window2 = dataset.getHeader().getEffectiveTimeFrame();
        assertEquals(window1, window2);
    }

}
