package org.radarcns.config;

import static java.util.Collections.singletonList;

import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Francesco Nobilia on 10/11/2016.
 */
public class YamlConfig implements RadarConfig{

//    private final Logger logger = LoggerFactory.getLogger(YamlConfig.class);

    private Date released;
    private String version;
    private Map<String,String> mongoHosts;
    private Map<String,String> mongoUser;

    public Date getReleased() {
        return released;
    }

    public void setReleased(Date released) {
        this.released = released;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getMongoHosts() {
        return mongoHosts;
    }

    public void setMongoHosts(Map<String, String> mongoHosts) {
        this.mongoHosts = mongoHosts;
    }

    public Map<String, String> getMongoUser() {
        return mongoUser;
    }

    public void setMongoUser(Map<String, String> mongoUser) {
        this.mongoUser = mongoUser;
    }

    public List<ServerAddress> getMongoDBHosts(){

        List<ServerAddress> mongoHostsTemp = new LinkedList<>();
        for(String key : mongoHosts.keySet()){
            mongoHostsTemp.add(new ServerAddress(key,Integer.valueOf(mongoHosts.get(key))));
        }

        return mongoHostsTemp;
    }

    public List<MongoCredential> getMongoDBCredential(){
        return singletonList(MongoCredential.createCredential(mongoUser.get("usr"), mongoUser.get("db"), mongoUser.get("pwd").toCharArray()));
    }

    public String getMongoDbName() {
        return mongoUser.get("db");
    }
}
