package org.radarcns.unit.monitor;

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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.radarcns.avro.restapi.source.State;
import org.radarcns.monitor.SourceMonitor;
import org.radarcns.source.Empatica;

public class SourceMonitorTest {

    @Test
    public void getSourceTest() {
        Empatica empatica = new Empatica();
        SourceMonitor monitor = new SourceMonitor(empatica);

        assertEquals(empatica, monitor.getSource());
    }

    @Test
    public void getStatusTest(){
        assertEquals(State.FINE, SourceMonitor.getStatus(1d));
        assertEquals(State.FINE, SourceMonitor.getStatus(0.951));

        assertEquals(State.OK, SourceMonitor.getStatus(0.95));
        assertEquals(State.OK, SourceMonitor.getStatus(0.90));
        assertEquals(State.OK, SourceMonitor.getStatus(0.801));

        assertEquals(State.WARNING, SourceMonitor.getStatus(0.80));
        assertEquals(State.WARNING, SourceMonitor.getStatus(0.40));
        assertEquals(State.WARNING, SourceMonitor.getStatus(0.01));

        assertEquals(State.DISCONNECTED, SourceMonitor.getStatus(0d));

        assertEquals(State.UNKNOWN, SourceMonitor.getStatus(-1d));
    }

    @Test
    public void getPercentageTest(){
        assertEquals(1d, SourceMonitor.getPercentage(100d, 100d),
            0d);
        assertEquals(0.95d, SourceMonitor.getPercentage(95d, 100d),
            0d);
        assertEquals(0.50d, SourceMonitor.getPercentage(50d, 100d),
            0d);
        assertEquals(0.1d, SourceMonitor.getPercentage(10d, 100d),
            0d);
        assertEquals(0d, SourceMonitor.getPercentage(0d, 100d),
            0d);
    }

}
