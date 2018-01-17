package org.radarcns.status.hdfs;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains bins data from the HDFS converter.
 * This data is serializable with Jackson.
 */
public class HdfsBinsData {
    private static final Logger logger = LoggerFactory.getLogger(HdfsBinsData.class);
    private static final DateTimeFormatter HDFS_STATUS_TIME_FORMAT = DateTimeFormatter
            .ofPattern("yyyyMMdd_HH")
            .withZone(ZoneOffset.UTC);

    @JsonProperty
    private final Map<String, HdfsTopicStatistics> topics;

    /**
     * Parse the bins data from given path. Lines that cannot be parsed are logged.
     * @param path file path to read from.
     * @return data from bins.
     * @throws IOException if the file cannot be read.
     */
    public static HdfsBinsData parse(Path path) throws IOException {
        ZonedDateTime healthyCutoff = ZonedDateTime.now(ZoneOffset.UTC).minus(Duration.ofHours(3));
        try (BufferedReader br = Files.newBufferedReader(path)) {
            // header line to skip
            String line = br.readLine();
            if (line == null) {
                return new HdfsBinsData(Collections.emptyMap());
            }

            Map<String, HdfsTopicStatistics> bins = new HashMap<>();

            line = br.readLine();
            int lineNumber = 2;
            while (line != null) {
                String[] dataCsv = line.split(",");
                if (dataCsv.length != 4) {
                    logger.warn("Line {} does not contain 4 elements: {}", lineNumber, line);
                } else {
                    parseLine(dataCsv, lineNumber, bins, healthyCutoff);
                }
                line = br.readLine();
                lineNumber++;
            }

            return new HdfsBinsData(bins);
        }
    }

    /** Parses a single line in bins.csv and adds it to a topic map. */
    private static void parseLine(String[] line, int lineNumber,
            Map<String, HdfsTopicStatistics> topics, ZonedDateTime healthyCutoff) {
        ZonedDateTime timestamp;
        try {
            timestamp = ZonedDateTime.parse(line[2], HDFS_STATUS_TIME_FORMAT)
                    .withZoneSameInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ex) {
            logger.warn("Failed to parse date {} on line {}", line[2], lineNumber, ex);
            return;
        }

        String topic = line[0];
        String sourceId = line[1];
        long numRecords = Long.parseLong(line[3]);
        String status = timestamp.isAfter(healthyCutoff) ? "healthy" : "unhealthy";

        HdfsSourceStatus source = new HdfsSourceStatus(sourceId, status, timestamp,1L, numRecords);

        topics.compute(topic, (k, list) -> {
            if (list == null) {
                list = new HdfsTopicStatistics();
            }
            list.getSources().merge(sourceId, source,
                    (source1, source2) -> {
                        HdfsSourceStatus result;
                        // take the latest of the two time stamps.
                        if (source1.getTimestamp().isAfter(source2.getTimestamp())) {
                            result = source1;
                        } else {
                            result = source2;
                        }
                        result.setCount(source1.getCount() + source2.getCount());
                        result.setTotal(source1.getTotal() + source2.getTotal());
                        return result;
                    });
            return list;
        });
    }

    public HdfsBinsData(Map<String, HdfsTopicStatistics> topics) {
        this.topics = topics;
    }

    public Map<String, HdfsTopicStatistics> getTopics() {
        return topics;
    }
}
