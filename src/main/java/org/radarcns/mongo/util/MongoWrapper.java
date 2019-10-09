package org.radarcns.mongo.util;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.io.Closeable;
import java.util.List;
import org.bson.Document;
import org.radarcns.config.ApplicationConfig;

public class MongoWrapper implements Closeable {
  private final MongoClient client;
  private final MongoDatabase database;

  public MongoWrapper(ApplicationConfig config) {
    MongoCredential credentials = config.getMongoDbCredentials();
    List<ServerAddress> hosts = config.getMongoDbHosts();

    client = new MongoClient(hosts, credentials, MongoClientOptions.builder().build());
    database = client.getDatabase(config.getMongoDbName());
  }

  public MongoClient getClient() {
    return this.client;
  }

  public MongoDatabase getDatabase() {
    return this.database;
  }

  public MongoCollection<Document> getCollection(String name) {
    return database.getCollection(name);
  }

  @Override
  public void close() {
    this.client.close();
  }
}
