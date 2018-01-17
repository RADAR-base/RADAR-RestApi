package org.radarcns.status.hdfs;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.radarcns.status.hdfs.HdfsSourceStatus.Status.HEALTHY;
import static org.radarcns.status.hdfs.HdfsSourceStatus.Status.UNHEALTHY;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

public class HdfsBinsDataTest {
    @Test
    public void parse() throws IOException, URISyntaxException {
        Path binsPath = Paths.get(HdfsBinsData.class.getResource("bins.csv").toURI());

        testBinsResponse(HdfsBinsData.parse(binsPath));
    }

    public static void testBinsResponse(HdfsBinsData data) {
        assertThat(data.getTopics().keySet(), hasItems("t1", "t2"));
        assertThat(data.getTopics().get("t1").getSources().keySet(), hasItems("d1", "d2"));
        assertThat(data.getTopics().get("t2").getSources().keySet(), hasItems("d1"));

        HdfsSourceStatus t1D1 = data.getTopics().get("t1").getSources().get("d1");
        assertThat(t1D1.getCount(), is(2L));
        assertThat(t1D1.getTotal(), is(30L));
        assertThat(t1D1.getStatus(), is(UNHEALTHY));
        assertThat(t1D1.getLastUpdate(), equalTo("2017-01-01T11:00:00Z"));

        HdfsSourceStatus t2D1 = data.getTopics().get("t2").getSources().get("d1");
        assertThat(t2D1.getCount(), is(2L));
        assertThat(t2D1.getTotal(), is(14L));
        assertThat(t2D1.getStatus(), is(HEALTHY));
        assertThat(t2D1.getLastUpdate(), equalTo("2300-01-01T10:00:00Z"));
    }
}