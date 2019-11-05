package com.jonnymatts.jzonbie.history;

import com.jonnymatts.jzonbie.persistence.JzonbiePersistenceException;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.OptionalInt;

import static com.google.common.io.Files.readLines;
import static java.lang.Integer.parseInt;
import static java.nio.file.Files.write;

public class PersistentRequestCount {
    private final File fileLocation;

    public PersistentRequestCount(File fileLocation) {
        this.fileLocation = fileLocation;
    }

    public synchronized int incrementCounter(String requestKey) {
        try {
            ArrayList<String> result = new ArrayList<>();
            boolean found = false;
            int incrementedCount = 1;

            for(String line : readLines(fileLocation, Charset.defaultCharset())) {
                if (line.startsWith(requestKey)) {
                    incrementedCount = incrementLine(result, line);
                    found = true;
                } else {
                    result.add(line);
                }
            }

            if(!found) {
                result.add(requestKey + ":1");
            }

            write(fileLocation.toPath(), result);
            return incrementedCount;
        } catch (Exception e) {
            throw new JzonbiePersistenceException("Failure incrementing persistence counter", e);
        }
    }

    public synchronized OptionalInt getCount(String requestKey) {
        try {
            return readLines(fileLocation, Charset.defaultCharset()).stream()
                    .filter(line -> line.startsWith(requestKey + ":"))
                    .mapToInt(line -> parseInt(line.split(":")[1]))
                    .findFirst();

        } catch (Exception e) {
            throw new JzonbiePersistenceException("Failure reading persistence count", e);
        }
    }

    private int incrementLine(ArrayList<String> result, String line) {
        final String[] splitLine = line.split(":");
        final int incrementedValue = incrementValue(splitLine[1]);
        result.add(createLine(splitLine[0], incrementedValue));
        return incrementedValue;
    }

    private int incrementValue(String value) {
        return parseInt(value) + 1;
    }

    private String createLine(String key, int value) {
        return key + ":" + value;
    }
}
