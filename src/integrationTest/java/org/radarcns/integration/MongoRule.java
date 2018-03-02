package org.radarcns.integration;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.junit.rules.ExternalResource;
import org.radarcns.config.Properties;
import org.radarcns.mongo.util.MongoHelper;

/**
 * Rule to get a MongoClient. After use, all collections that were accessed are dropped again.
 */
public class MongoRule extends ExternalResource {
    private MongoClient client;
    private ConcurrentHashSet<String> queriedCollections;
    private MongoClient originalClient;

    @Override
    public void before() {
        queriedCollections = new ConcurrentHashSet<>();
        client = null;
        originalClient = null;
    }

    /** Get a MongoClient that keeps track of the collections that are queried. */
    public MongoClient getClient() {
        if (client == null) {
            final MongoClient originalClient = getOriginalClient();
            client = spy(originalClient);
            // test that only the wanted queriedCollections are queried.
            doAnswer(invocation -> {
                String dbName = invocation.getArgument(0);
                final MongoDatabase originalDatabase = originalClient.getDatabase(dbName);
                MongoDatabase spiedDatabase = spy(originalDatabase);
                doAnswer(invocation1 -> {
                    String collectionName = invocation1.getArgument(0);
                    queriedCollections.add(collectionName);
                    return originalDatabase.getCollection(collectionName);
                }).when(spiedDatabase).getCollection(anyString());
                return spiedDatabase;
            }).when(client).getDatabase(anyString());
        }
        return client;
    }

    /** Get the unmodified MongoClient. */
    private MongoClient getOriginalClient() {
        if (originalClient == null) {
            MongoCredential credentials = Properties.getApiConfig().getMongoDbCredentials();
            originalClient = new MongoClient(
                    Properties.getApiConfig().getMongoDbHosts(),
                    credentials, MongoClientOptions.builder().build());
        }
        return originalClient;
    }

    /** Get a MongoDB collection. */
    public MongoCollection<Document> getCollection(String collection) {
        queriedCollections.add(collection);
        return MongoHelper.getCollection(getOriginalClient(), collection);
    }

    /** The list of collections that have been queried so far. */
    public ConcurrentHashSet<String> getQueriedCollections() {
        return queriedCollections;
    }

    @Override
    public void after() {
        if (originalClient != null) {
            queriedCollections.forEach(c -> MongoHelper.getCollection(originalClient, c).drop());
            originalClient.close();
        }
    }
}
