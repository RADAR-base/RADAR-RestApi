package org.radarcns.util;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;

public class CachedMapTest {

    private AtomicInteger calls;
    private CachedMap<String, String> map;

    /**
     * Set up a basic map and empty calls.
     */
    @Before
    public void setUp() {
        calls = new AtomicInteger(0);
        map = new CachedMap<>(
                () -> {
                    calls.incrementAndGet();
                    return Arrays.asList("test1", "test2");
                },
                Function.identity(), Duration.ofMillis(300), Duration.ofMillis(150));
    }

    @Test
    public void get() throws IOException, InterruptedException {
        assertThat(map.get(), hasEntry("test1", "test1"));
        assertThat(calls.get(), is(1));
        Thread.sleep(350);
        assertThat(map.get(), hasEntry("test2", "test2"));
        assertThat(calls.get(), is(2));
        assertThat(map.get(), hasEntry("test2", "test2"));
        assertThat(calls.get(), is(2));
    }

    @Test
    public void get1() throws IOException {
        assertThat(map.get(true), hasEntry("test1", "test1"));
        assertThat(calls.get(), is(1));
        assertThat(map.get(true), hasEntry("test2", "test2"));
        assertThat(calls.get(), is(2));
        assertThat(map.get(true), hasEntry("test2", "test2"));
        assertThat(calls.get(), is(3));
    }

    @Test
    public void get2() throws IOException, InterruptedException {
        assertThat(map.get("test1"), equalTo("test1"));
        assertThat(calls.get(), is(1));
        try {
            map.get("test3");
            fail();
        } catch (NoSuchElementException ex) {
            // success
        }
        assertThat(calls.get(), is(1));
        Thread.sleep(200);
        try {
            map.get("test3");
            fail();
        } catch (NoSuchElementException ex) {
            // success
        }
        assertThat(calls.get(), is(2));
        assertThat(map.get("test1"), equalTo("test1"));
        assertThat(calls.get(), is(2));
    }
}