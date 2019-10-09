package org.radarcns.integration;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

import com.mongodb.client.MongoCollection;
import java.util.Collection;
import org.bson.Document;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.junit.rules.ExternalResource;
import org.radarcns.config.ApplicationConfig;
import org.radarcns.mongo.util.MongoWrapper;

/**
 * Rule to get a MongoClient. After use, all collections that were accessed are dropped again.
 */
public class MongoRule extends ExternalResource {
    private final ApplicationConfig config;
    private MongoWrapper client;
    private MongoWrapper originalClient;
    private ConcurrentHashSet<String> queriedCollections;

    public MongoRule(ApplicationConfig config) {
        this.config = config;
    }

    @Override
    public void before() {
        queriedCollections = new ConcurrentHashSet<>();
        client = null;
        originalClient = null;
    }

    /** Get a MongoClient that keeps track of the collections that are queried. */
    public MongoWrapper getClient() {
        if (client == null) {
            final MongoWrapper originalClient = getOriginalClient();
            client = spy(originalClient);
            // test that only the wanted queriedCollections are queried.
            doAnswer(invocation -> {
                String collectionName = invocation.getArgument(0);
                queriedCollections.add(collectionName);
                return originalClient.getCollection(collectionName);
            }).when(client).getCollection(anyString());
        }
        return client;
    }

    public MongoCollection<Document> getCollection(String name) {
        return getClient().getCollection(name);
    }

    /** Get the unmodified MongoClient. */
    private MongoWrapper getOriginalClient() {
        if (originalClient == null) {
            originalClient = new MongoWrapper(config);
        }
        return originalClient;
    }

    @Override
    public void after() {
        if (originalClient != null) {
            queriedCollections.forEach(c -> originalClient.getCollection(c).drop());
            originalClient.close();
        }
    }
}
