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

import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.junit.Test;
import org.radarcns.domain.restapi.TimeWindow;
import org.radarcns.domain.restapi.dataset.DataItem;
import org.radarcns.domain.restapi.dataset.Dataset;
import org.radarcns.domain.restapi.header.EffectiveTimeFrame;
import org.radarcns.mongo.util.MongoHelper;
import org.radarcns.mongo.util.MongoHelper.Stat;
import org.radarcns.integration.util.RandomInput;
import org.radarcns.util.RadarConverter;

/**
 * ExpectedValueTest Test.
 */
public class ExpectedValueTest {

    private static final String SUBJECT = "UserID_0";
    private static final String SOURCE = "SourceID_0";
    private static final TimeWindow TIME_WINDOW = TimeWindow.TEN_SECOND;
    private static final int SAMPLES = 10;

    @Test
    public void matchDatasetOnDocuments() throws Exception {
        Map<String, Object> map = RandomInput.getDatasetAndDocumentsRandom(SUBJECT, SOURCE,
                "EMPATICA", "HEART_RATE", COUNT, TIME_WINDOW, SAMPLES, false);

        List<Document> docs = (List<Document>) map.get(RandomInput.DOCUMENTS);
        int count = 0;
        for (Document doc : docs) {
            count += doc.getDouble(Stat.count.getParam()).intValue();
        }
        assertEquals(SAMPLES, count);

        Dataset dataset = (Dataset) map.get(RandomInput.DATASET);

        count = 0;
        for (DataItem item : dataset.getDataset()) {
            count += (Double)  item.getSample();
        }
        assertEquals(SAMPLES, count);

        EffectiveTimeFrame window1 = new EffectiveTimeFrame(
                RadarConverter.getISO8601(docs.get(0).getDate(MongoHelper.START)),
                RadarConverter.getISO8601(docs.get(docs.size() - 1).getDate(MongoHelper.END)));

        EffectiveTimeFrame window2 = dataset.getHeader().getEffectiveTimeFrame();
        assertEquals(true, compareEffectiveTimeFrame(window1, window2));
    }

    /**
     * Compare two {@code EffectiveTimeFrame} values.
     *
     * @param window1 first component that to has to be compared
     * @param window2 second component that to has to be compared
     * @return {@code true} if they match, false otherwise
     * @see EffectiveTimeFrame
     **/
    public static boolean compareEffectiveTimeFrame(EffectiveTimeFrame window1,
                EffectiveTimeFrame window2) {
        return window1.getStartDateTime().equals(window2.getStartDateTime())
                && window1.getEndDateTime().equals(window2.getEndDateTime());
    }
}
