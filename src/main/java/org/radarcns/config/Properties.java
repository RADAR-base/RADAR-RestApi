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
 * Created by Francesco Nobilia on 10/11/2016.
 */
public class Properties {

    private final Logger logger = LoggerFactory.getLogger(Properties.class);

    private RadarConfig config;

    private static final Properties instance = new Properties();

    public static Properties getInstance(){
        return instance;
    }

    /** Docker **/
    private String pathFile = "/usr/local/tomcat/conf/";
    /** ECS **/
//    private String pathFile = "/usr/share/tomcat8/conf/";

    private String nameFile = "radar.yml";

    private Properties(){
        try{
            Yaml yaml = new Yaml();
            InputStream in = Files.newInputStream(Paths.get(pathFile+nameFile));
            config = yaml.loadAs( in, RadarConfig.class );
        }
        catch (IOException ex){
            logger.error("Impossible load properties {}",ex.getMessage());
        }
    }

    public List<ServerAddress> getMongoHosts() {
        return config.getMongoDBHosts();
    }

    public List<MongoCredential> getMongoUsers() {
        return config.getMongoDBUser();
    }

    public String getMongoDbName() {
        return config.getMongoUser().get("db");
    }
}
