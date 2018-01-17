package org.radarcns.status.hdfs;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class HdfsBinsDataTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void parse() throws IOException {
        Path binsPath = folder.newFile().toPath();
        LocalDateTime nextYear = LocalDateTime.now().plus(Duration.ofDays(366));
        Files.write(binsPath, Arrays.asList(
                "topic,device,timestamp,count",
                "t1,d1,20170101_10,15",
                "t1,d1,20170101_11,15",
                "t1,d2,20170101_11,10",
                "t2,d1,20170101_11,7",
                "t2,d1," + nextYear.getYear() + "0101_10,7"));

        HdfsBinsData data = HdfsBinsData.parse(binsPath);
        assertThat(data.getTopics().keySet(), hasItems("t1", "t2"));
        assertThat(data.getTopics().get("t1").getSources().keySet(), hasItems("d1", "d2"));
        assertThat(data.getTopics().get("t2").getSources().keySet(), hasItems("d1"));

        HdfsSourceStatus t1D1 = data.getTopics().get("t1").getSources().get("d1");
        assertThat(t1D1.getCount(), is(2L));
        assertThat(t1D1.getTotal(), is(30L));
        assertThat(t1D1.getStatus(), equalTo("unhealthy"));
        assertThat(t1D1.getLastUpdate(), equalTo("2017-01-01T11:00:00Z"));

        HdfsSourceStatus t2D1 = data.getTopics().get("t2").getSources().get("d1");
        assertThat(t2D1.getCount(), is(2L));
        assertThat(t2D1.getTotal(), is(14L));
        assertThat(t2D1.getStatus(), equalTo("healthy"));
        assertThat(t2D1.getLastUpdate(), equalTo(nextYear.getYear() + "-01-01T10:00:00Z"));
    }
}