package org.radarcns.status;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class CSVDataController {
    @JsonProperty
    public static List<SourceData> sources;
    public static int curr = 0;

    /**
     *
     * @param csvDataList
     * @return
     */
    public static HashSet<String> getAllTopics(List<CSVData> csvDataList) {
        HashSet<String> topics = new HashSet<>();
        for (CSVData line: csvDataList
             ) {
            topics.add(line.getTopic());
        }
        return topics;
    }


    /**
     * Groups and aggregates CSV data using java streams classes.
     * @param csvDataList list of {@link CSVData} objects read from bins.csv
     * @return a {@link JsonNode} object containing aggregated data for each topic for each source
     */
    public static String getDataOfTopic(List<CSVData> csvDataList) {

        sources  = new ArrayList<>();

        Map<String,List<CSVData>> groupedData = csvDataList.stream().collect(Collectors.groupingBy(CSVData::getTopic));

        Map<String,List<SourceData>> listMap = new HashMap<>();

        groupedData
                .entrySet()
                .stream()
                .forEach(x -> {
                    x.getValue().stream()
                            .forEach(p -> {
                                try {
                                    if(sources.isEmpty()) {

                                        sources.add(new SourceData(p.getDevice(),
                                                checkStatus(convertTimestamp(p.getTimestamp())),
                                                convertTimestamp(p.getTimestamp()),
                                                1L, Long.parseLong(p.getCount())));

                                    } else if (containsSource(p.getDevice())) {

                                        SourceData data = sources.get(curr);
                                        sources.get(curr).setCount(data.getCount() + 1L);

                                        if (compareTimestamp(data.getLastUpdate(),
                                                p.getTimestamp()) < 0) {
                                            sources.get(curr).
                                                    setLastUpdate(convertTimestamp(p.getTimestamp()));
                                            sources.get(curr).
                                                    setStatus(checkStatus(convertTimestamp(p.getTimestamp())));
                                        }

                                        sources.get(curr).setTotal(data.getTotal() +
                                                Long.parseLong(p.getCount()));

                                    } else {
                                        sources.add(new SourceData(p.getDevice(),
                                                checkStatus(convertTimestamp(p.getTimestamp())),
                                                convertTimestamp(p.getTimestamp()),
                                                1L, Long.parseLong(p.getCount())));
                                    }
                                } catch (DateTimeParseException exc) {
                                    exc.printStackTrace();
                                }
                                    }
                            );
                    listMap.put(x.getKey(), sources);
                    sources = new ArrayList<>();
                    curr = 0;
                });

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.valueToTree(listMap);
        return jsonNode.toString();
    }

    /**
     * Compares two timestamps in string format.
     * @param timeStamp1 first timestamp string
     * @param timeStamp2 second timestamp string
     * @return int that gives comparison of two timestamps
     */
    public static int compareTimestamp(String timeStamp1, String timeStamp2) {

        LocalDateTime ldt1 = LocalDateTime.parse(convertTimestamp(timeStamp1));
        LocalDateTime ldt2 = LocalDateTime.parse(convertTimestamp(timeStamp2));
        return ldt1.compareTo(ldt2);
    }


    /**
     * Returns the health of a topic or source based on the amount of time
     * between the timestamp and now.
     * @param timeStamp timestamp in string format
     * @return status as "healthy" or "unhealthy"
     */
    public static String checkStatus(String timeStamp) {
        LocalDateTime timeStmp = LocalDateTime.parse(timeStamp);
        if ( Duration.between(timeStmp, LocalDateTime.now())
                .compareTo(Duration.ofHours(3)) > 0 ) {
            return "unhealthy";
        } else
            return "healthy";
    }

    /**
     * Converts the timestamp from a custom format to ISO standard.
     * @param timestmp the timestamp to be converted
     * @return the timestamp in string format
     * @throws DateTimeParseException
     */
    public static String convertTimestamp (String timestmp) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HH");

        if (isParsable(timestmp)) {
            LocalDateTime dateTime = LocalDateTime.parse(timestmp, formatter);
            return dateTime.toString();
        } else {
            return timestmp;
        }
    }


    /**
     * Checks if a {@link String} value can be parsed into Date-Time.
     * @param input
     * @return true or false
     */
    public static boolean isParsable(String input){
        boolean parsable = true;
        try{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HH");
            LocalDateTime.parse(input, formatter);
        }catch(DateTimeParseException e){
            parsable = false;
        }
        return parsable;
    }

    /**
     * Checks is a new source read form the CSV data is
     * already present in {@link ArrayList} of sources.
     * @param sourceId
     * @return
     */
    public static boolean containsSource(String sourceId) {

        for (SourceData data: sources
             ) {
            if (data.getDevice().equals(sourceId)) {
                curr = sources.indexOf(data);
                return true;
            }
        }
        return false;
    }


}
