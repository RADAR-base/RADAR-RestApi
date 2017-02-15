package org.radarcns.integrationtest.config;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MockConfig {
    private List<MockDataConfig> data;

    public List<MockDataConfig> getData() {
        return data;
    }

    public void setData(List<MockDataConfig> data) {
        this.data = data;
    }

    public static MockConfig load(File file) throws IOException {
        return new ConfigLoader().load(file, MockConfig.class);
    }

    public static MockConfig load(ClassLoader classLoader) throws IOException {
        File mockFile = new File(classLoader.getResource("mock_data.yml").getFile());
        return new ConfigLoader().load(mockFile, MockConfig.class);
    }
}
