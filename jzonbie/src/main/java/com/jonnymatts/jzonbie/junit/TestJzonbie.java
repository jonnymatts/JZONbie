package com.jonnymatts.jzonbie.junit;

import com.jonnymatts.jzonbie.Jzonbie;
import com.jonnymatts.jzonbie.JzonbieOptions;

import java.nio.file.Files;

import static com.jonnymatts.jzonbie.JzonbieOptions.options;

public class TestJzonbie extends Jzonbie {

    public TestJzonbie() {
        super(options().withHomePath(getTestZombieHomePath()));
    }

    public TestJzonbie(JzonbieOptions jzonbieOptions) {
        super(jzonbieOptions.withHomePath(getTestZombieHomePath()));
    }

    private static String getTestZombieHomePath() {
        try {
            return Files.createTempDirectory("tempTestJzonbieDir").toAbsolutePath().toString();
        } catch (Exception e) {
            throw new RuntimeException("Could not create Test Jzonbie", e);
        }
    }
}
