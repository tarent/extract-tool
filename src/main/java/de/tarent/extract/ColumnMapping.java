package de.tarent.extract;

import java.util.Map;
import java.util.Properties;

public class ColumnMapping {
    private final Class<? extends ResultSetValueExtractor> extractWith;
    private final String mapTo;
    private final Map<String,?> properties;

    public ColumnMapping() {
        this(null, null);
    }

    public ColumnMapping(final String mapTo) {
        this(mapTo, null);
    }

    public ColumnMapping(final String mapTo, final Class<? extends ResultSetValueExtractor> extractWith) {
        this(mapTo,extractWith,null);
    }

    public ColumnMapping(final String mapTo, final Class<? extends ResultSetValueExtractor> extractWith, Map<String, ?> properties) {
        this.mapTo = mapTo;
        this.properties = properties;
        this.extractWith = extractWith == null ? DefaultExtractor.class : extractWith;

    }

    public Class<? extends ResultSetValueExtractor> getExtractWith() {
        return extractWith;
    }

    public String getMapTo() {
        return mapTo;
    }

    public Map<String, ?> getProperties() {
        return properties;
    }
}
