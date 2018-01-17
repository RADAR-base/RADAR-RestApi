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

public class CachedMap<S, T> {
    private final ThrowingSupplier<? extends Collection<T>> retriever;
    private final Function<T, S> keyExtractor;
    private final Duration invalidateAfter;
    private final Duration retryAfter;
    private Temporal lastFetch;
    private Map<S, T> cache;

    public CachedMap(ThrowingSupplier<? extends Collection<T>> retriever,
            Function<T, S> keyExtractor, Duration invalidateAfter, Duration retryAfter) {
        this.retriever = retriever;
        this.keyExtractor = keyExtractor;
        this.invalidateAfter = invalidateAfter;
        this.retryAfter = retryAfter;
        this.lastFetch = Instant.MIN;
    }

    public Map<S, T> get() throws IOException {
        return get(false);
    }

    public Map<S, T> get(boolean force) throws IOException {
        if (cache == null
                || RadarConverter.isThresholdPassed(lastFetch, invalidateAfter)
                || force) {
            cache = retriever.get().stream()
                .collect(Collectors.toMap(keyExtractor, Function.identity()));
            lastFetch = Instant.now();
        }
        return cache;
    }

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

    public boolean mayRetry() {
        return RadarConverter.isThresholdPassed(lastFetch, retryAfter);
    }


    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws IOException;
    }
}
