package org.radarcns.pipeline.data;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.radarcns.pipeline.EndToEndTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class Metronome {

    //private static final Logger logger = LoggerFactory.getLogger(Metronome.class);

    public static List<Long> timestamps(int samples, int frequency, int baseFrequency) {
        checkInput(samples, frequency, baseFrequency);

        List<Long> timestamps = new ArrayList<>();

        long shift = samples / frequency;
        final long baseTime = TimeUnit.MILLISECONDS.toNanos(
                System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(shift));
        final long timeStep = 1_000_000_000L / baseFrequency;

        int iteration = 0;

        while (timestamps.size() < samples) {
            if ((iteration % baseFrequency + 1) % (baseFrequency / frequency) == 0) {
                timestamps.add(timestamps.size(), TimeUnit.NANOSECONDS.toMillis(
                        baseTime + iteration * timeStep));
            }

            iteration++;
        }

        return timestamps;
    }

    private static void checkInput(int samples, int frequency, int baseFrequency) {
        if (samples < 0) {
            throw new IllegalArgumentException("The amount of samples must be positve");
        }

        if (frequency < 0) {
            throw new IllegalArgumentException("Frequency must be bigger than zero");
        }

        if (baseFrequency < frequency) {
            throw new IllegalArgumentException("BaseFrequency cannot be smaller than frequency");
        }
    }
}
