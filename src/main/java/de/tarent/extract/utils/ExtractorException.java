package de.tarent.extract.utils;

public class ExtractorException extends RuntimeException {
    private static final long serialVersionUID = 5873090203439740230L;

    public ExtractorException(final Throwable cause) {
        super(cause);
    }

    public ExtractorException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
