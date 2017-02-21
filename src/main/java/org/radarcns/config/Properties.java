package org.radarcns.config;

import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Properties handler.
 */
public class Properties {

    private final Logger logger = LoggerFactory.getLogger(Properties.class);

    // Useful for AWS deploy
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    private static final String PATH_FILE = "/usr/share/tomcat8/conf/";
    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    private static final String NAME_FILE = "radar.yml";

    private RadarConfig config;

    private static final Properties instance = new Properties();

    public static Properties getInstance() {
        return instance;
    }

    private Properties() {
        initDockerConfig();

        if (config == null) {
            initYamlConfig();
        }
    }

    private void initDockerConfig() {
        try {
            config = new DockerConfig();

            logger.info("Properties fetched from env variables");
        } catch (Exception ex) {
            logger.warn("Impossible load Docker properties", ex);
        }
    }

    private void initYamlConfig() {
        try {
            Yaml yaml = new Yaml();
            InputStream in = Files.newInputStream(Paths.get(PATH_FILE + NAME_FILE));
            config = yaml.loadAs(in, YamlConfig.class);

            logger.info("Properties fetched from .yml file");
        } catch (IOException ex) {
            logger.warn("Impossible load Yaml properties", ex);
        }
    }

    public List<ServerAddress> getMongoHosts() {
        return config.getMongoDbHosts();
    }

    public List<MongoCredential> getMongoDbCredential() {
        return config.getMongoDbCredential();
    }

    public String getMongoDbName() {
        return config.getMongoDbName();
    }
}
