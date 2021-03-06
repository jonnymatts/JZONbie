package com.jonnymatts.jzonbie.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

public class Logging {

    public static void setLevel(String loggerName, Level level) {
        final Logger logger = (Logger) LoggerFactory.getLogger(loggerName);
        logger.setLevel(level);
    }
}