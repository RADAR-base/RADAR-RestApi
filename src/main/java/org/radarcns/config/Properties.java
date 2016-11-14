package org.radarcns.config;

import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by Francesco Nobilia on 10/11/2016.
 */
public class Properties {

    private final Logger logger = LoggerFactory.getLogger(Properties.class);

    private RadarConfig config;

    private String pathFile = "/usr/local/tomcat/conf/";
    //private String pathFile = "/usr/share/tomcat8/conf/";

    private String nameFile = "radar.yml";

    public Properties(){
        try{
            Yaml yaml = new Yaml();
            InputStream in = Files.newInputStream(Paths.get(pathFile+nameFile));
            this.config = yaml.loadAs( in, RadarConfig.class );
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
}
