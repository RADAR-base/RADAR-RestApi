package org.radarcns;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.radarcns.avro.restapi.device.Status;
import org.radarcns.monitor.Empatica;

/**
 * Created by Francesco Nobilia on 10/01/2017.
 */
public class EmapticaMonitor {

    @Test
    public void callgetPercentage() throws Exception {
        assertEquals(1.0, Empatica.getPercentage(10.0,10.0), 0.0);
        assertEquals(0.5, Empatica.getPercentage(5.0,10.0), 0.0);
        assertEquals(0.1, Empatica.getPercentage(1.0,10.0), 0.0);
        assertEquals(0.0, Empatica.getPercentage(0.0,10.0), 0.0);
    }

    @Test
    public void callgetStatus() throws Exception {
        assertEquals(Status.fine, Empatica.getStatus(1.0));
        assertEquals(Status.fine, Empatica.getStatus(0.951));
        assertEquals(Status.ok, Empatica.getStatus(0.95));
        assertEquals(Status.ok, Empatica.getStatus(0.801));
        assertEquals(Status.warning, Empatica.getStatus(0.80));
        assertEquals(Status.warning, Empatica.getStatus(0.501));
        assertEquals(Status.bad, Empatica.getStatus(0.50));
        assertEquals(Status.bad, Empatica.getStatus(0.001));
        assertEquals(Status.disconnected, Empatica.getStatus(0.0));
    }

}
