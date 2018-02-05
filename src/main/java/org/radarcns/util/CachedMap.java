package org.radarcns.util;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Map that caches the result of a list for a limited time.
 *
 * <p>This class is thread-safe if given retriever and key extractors are thread-safe.
 */
public class CachedMap<S, T> {
    private final ThrowingSupplier<? extends Collection<T>> retriever;
    private final Function<T, S> keyExtractor;
    private final Duration invalidateAfter;
    private final Duration retryAfter;
    private Temporal lastFetch;
    private Map<S, T> cache;

    /**
     * Map that retrieves data from a supplier and converts that to a map with given key extractor.
     * Given retriever and key extractor should be thread-safe to make this class thread-safe.
     * @param retriever supplier of data.
     * @param keyExtractor key extractor of individial data points.
     * @param invalidateAfter invalidate the set of valid results after this duration.
     * @param retryAfter retry on a missing key after this duration.
     */
    public CachedMap(ThrowingSupplier<? extends Collection<T>> retriever,
            Function<T, S> keyExtractor, Duration invalidateAfter, Duration retryAfter) {
        this.retriever = retriever;
        this.keyExtractor = keyExtractor;
        this.invalidateAfter = invalidateAfter;
        this.retryAfter = retryAfter;
        this.lastFetch = Instant.MIN;
    }

    /**
     * Get the cached map, or retrieve a new one if the current one is old.
     * @return map of data
     * @throws IOException if the data could not be retrieved.
     */
    public Map<S, T> get() throws IOException {
        return get(false);
    }

    /**
     * Get the cached map, or retrieve a new one if the current one is old.
     * @param forceRefresh if true, the cache will be refreshed even if it is recent.
     * @return map of data
     * @throws IOException if the data could not be retrieved.
     */
    public Map<S, T> get(boolean forceRefresh) throws IOException {
        if (!forceRefresh) {
            synchronized (this) {
                if (cache != null
                        && !RadarConverter.isThresholdPassed(lastFetch, invalidateAfter)) {
                    return cache;
                }
            }
        }
        Map<S, T> result = retriever.get().stream()
                .collect(Collectors.toMap(keyExtractor, Function.identity()));

        synchronized (this) {
            cache = result;
            lastFetch = Instant.now();
            return cache;
        }
    }

    /**
     * Get a key from the map. If the key is missing, it will check with {@link #mayRetry()} whether
     * the cache may be updated. If so, it will fetch the cache again and look the key up.
     * @param key key of the value to find.
     * @return element
     * @throws IOException if the cache cannot be refreshed.
     * @throws NoSuchElementException if the element is not found.
     */
    public T get(S key) throws IOException, NoSuchElementException {
        T value = get().get(key);
        if (value == null) {
            if (mayRetry()) {
                value = get(true).get(key);
            }
            if (value == null) {
                throw new NoSuchElementException("Cannot find element for key " + key);
            }
        }
        return value;
    }

    /**
     * Whether the cache may be refreshed.
     */
    public synchronized boolean mayRetry() {
        return RadarConverter.isThresholdPassed(lastFetch, retryAfter);
    }

    /**
     * Supplier that may throw an exception. Otherwise similar to
     * {@link java.util.function.Supplier}.
     */
    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws IOException;
    }
}
