package com.cocktailapp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for providing SLF4J loggers for various classes.
 */
public class LoggerUtil {

    /**
     * Returns a logger instance for the given class.
     *
     * @param clazz The class for which the logger is requested.
     * @return The logger instance for the specified class.
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * Prevent instantiation of utility class.
     */
    private LoggerUtil() {
        throw new UnsupportedOperationException("LoggerUtil is a utility class and cannot be instantiated.");
    }
}