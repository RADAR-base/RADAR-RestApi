package org.radarcns.integrationtest.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.File;
import java.nio.file.NoSuchFileException;

public class MockDataConfig {
    private String topic;
    @JsonProperty("rest_call")
    private String restCall;
    @JsonProperty("key_schema")
    private String keySchema;
    @JsonProperty("file")
    private String dataFile;
    @JsonProperty("value_schema")
    private String valueSchema;
    @JsonProperty("values_to_test")
    private String valuesToTest;

    /**
     * @return Comma Separated Values {@code File}.
     **/
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    public File getCVSFile() throws NoSuchFileException {
        File directDataFile = new File(dataFile);
        if (directDataFile.exists()) {
            return directDataFile;
        } else {
            throw new NoSuchFileException(dataFile + " cannot be found.");
        }
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getRestCall() {
        return restCall;
    }

    public void setRestCall(String restCall) {
        this.restCall = restCall;
    }

    public String getKeySchema() {
        return keySchema;
    }

    public void setKeySchema(String keySchema) {
        this.keySchema = keySchema;
    }

    public String getDataFile() {
        return dataFile;
    }

    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
    }

    public String getValueSchema() {
        return valueSchema;
    }

    public void setValueSchema(String valueSchema) {
        this.valueSchema = valueSchema;
    }

    public String getValuesToTest() {
        return valuesToTest;
    }

    public void setValuesToTest(String valuesToTest) {
        this.valuesToTest = valuesToTest;
    }
}
