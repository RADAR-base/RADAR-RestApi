package org.radarcns.pipeline.data;

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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.radarcns.integration.model.MockConfigToCsvParser;
import org.radarcns.integration.model.MockConfigToCsvParser.Variable;
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
    public static void validate(MockDataConfig config, Long duration)
        throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
        InvocationTargetException, ParseException {

        MockConfigToCsvParser parser = new MockConfigToCsvParser(config);

        int line = 1;
        String mex = null;

        Date start = null;
        Date end = null;

        Map<Variable, Object> map = parser.next();
        Map<Variable, Object> last = map;
        while (map != null) {
            if (!last.get(Variable.USER).toString().equals(map.get(Variable.USER).toString())) {
                mex = "It is possible to test only one user at time.";
            } else if ( !last.get(Variable.SOURCE).toString().equals(
                    map.get(Variable.SOURCE).toString()) ) {
                mex = "It is possible to test only one source at time.";
            } else if ( !( ((Long)map.get(Variable.TIMESTAMP)).longValue()
                    >= ((Long)last.get(Variable.TIMESTAMP)).longValue() ) ) {
                mex = Variable.TIMESTAMP.toString() + " must increase raw by raw.";

            } else if ( map.get(Variable.VALUE) == null ) {
                mex = Variable.VALUE.toString() + "value to test must be specified.";
            }

            if ( mex != null) {
                mex += " " + config.getDataFile() + " is invalid. Error at line " + line;
                logger.error(mex);
                throw new IllegalArgumentException(mex);
            }

            if (line == 1) {
                start = new Date((Long) map.get(Variable.TIMESTAMP));
            }
            end = new Date((Long) map.get(Variable.TIMESTAMP));

            last = map;
            map = parser.next();
            line++;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


        if (checkDuration(start, end, duration)) {
            mex = config.getDataFile() + " is invalid. Data does not cover "
                    + duration.longValue() + " seconds.";
            logger.error(mex);
            throw new IllegalArgumentException(mex);
        }

        if (line != (config.getFrequency() * duration + 1)) {
            mex = config.getDataFile() + " is invalid. CVS contains less messages tha expected.";
            logger.error(mex);
            throw new IllegalArgumentException(mex);
        }

        parser.close();
    }

    private static boolean checkDuration(Date start, Date end, Long duration) {
        long upperbound = duration.longValue() * 1000;
        long lowerbound = upperbound - 1000;

        long interval = TimeUnit.MILLISECONDS.toSeconds(end.getTime() - start.getTime());

        return interval > lowerbound && upperbound > interval;
    }

}
