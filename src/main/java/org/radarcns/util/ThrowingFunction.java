package org.radarcns.util;

import java.util.function.Function;

/** Function that may throw an unchecked exception. */
@FunctionalInterface
public interface ThrowingFunction<T, R> {
    R apply(T value) throws Exception;

    /**
     * Run a function. If it throws any exception, apply toException to map it to a runtime
     * exception and throw that instead. RuntimeException objects will also be mapped by
     * toException.
     */
    static <T, R> Function<T, R> tryOrRethrow(
            ThrowingFunction<T, R> function,
            Function<Exception, RuntimeException> toException) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Exception e) {
                throw toException.apply(e);
            }
        };
    }
}
