package org.radarcns.pipeline.data;

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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.radarcns.integration.model.MockConfigToCsvParser;
import org.radarcns.integration.model.MockRecord;
import org.radarcns.mock.MockDataConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CSV files must be validate before using since MockAggregator can handle only files containing
 *      unique User_ID and Source_ID and having increasing timestamp at each raw.
 */
public class CsvValidator {

    private static final Logger logger = LoggerFactory.getLogger(CsvValidator.class);

    /**
     * Verify whether the CSV file can be used or not.
     * @param config configuration item containing the CSV file path.
     * @throws IllegalArgumentException if the CSV file does not respect the constrains.
     */
    public static void validate(MockDataConfig config, long duration, File root)
        throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
        InvocationTargetException, ParseException {

        MockConfigToCsvParser parser = new MockConfigToCsvParser(config, root);

        int line = 1;
        String mex = null;

        Date start = null;
        Date end = null;

        MockRecord.DoubleType record = parser.nextDoubleRecord();
        MockRecord.DoubleType last = record;
        while (record != null) {
            if (!last.getKey().equals(record.getKey())) {
                mex = "It is possible to test only one user/source at time.";
            } else if (record.getTimeMillis() < last.getTimeMillis() ) {
                mex = "time must increase raw by raw.";
            }

            if ( mex != null) {
                mex += " " + config.getDataFile() + " is invalid. Error at line " + line;
                logger.error(mex);
                throw new IllegalArgumentException(mex);
            }

            if (line == 1) {
                start = new Date(record.getTimeMillis());
            }
            end = new Date(record.getTimeMillis());

            last = record;
            record = parser.nextDoubleRecord();
            line++;
        }

        if (checkDuration(start, end, duration)) {
            mex = config.getDataFile() + " is invalid. Data does not cover "
                    + duration + " seconds.";
            logger.error(mex);
            throw new IllegalArgumentException(mex);
        }

        if (line != (config.getFrequency() * duration + 1)) {
            mex = config.getDataFile() + " is invalid. CSV contains fewer messages than expected.";
            logger.error(mex);
            throw new IllegalArgumentException(mex);
        }

        parser.close();
    }

    private static boolean checkDuration(Date start, Date end, long duration) {
        long upperbound = duration * 1000L;
        long lowerbound = upperbound - 1000L;

        long interval = TimeUnit.MILLISECONDS.toSeconds(end.getTime() - start.getTime());

        return interval > lowerbound && upperbound > interval;
    }
}
