package org.pseudosweep.util;

import java.time.Duration;
import java.time.Instant;

public class Stopwatch {

    private Instant start, stop;

    private Stopwatch() {
        start = Instant.now();
    }

    public static Stopwatch start() {
        return new Stopwatch();
    }

    public void stop() {
        stop = Instant.now();
    }

    public Duration duration() {
        Instant end = stop == null ? Instant.now() : stop;
        return Duration.between(start, end);
    }
}
