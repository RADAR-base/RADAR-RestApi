package org.radarcns;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.Test;
import org.radarcns.util.RadarConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Francesco Nobilia on 10/01/2017.
 */
public class TimeTest {

    private static final Logger logger = LoggerFactory.getLogger(TimeTest.class);

    private final boolean TEST = false;

    @Test
    public void callTest() throws Exception {
        if( TEST ) {
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date date = new Date();

            date.setTime(1486484730000L);
            logger.info("{} - {}", RadarConverter.getISO8601(date), df.format(date));

            date.setTime(1486484740000L);
            logger.info("{} - {}", RadarConverter.getISO8601(date), df.format(date));
        }
    }

}
