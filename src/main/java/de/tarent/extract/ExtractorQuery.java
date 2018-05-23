package de.tarent.extract;

import java.util.HashMap;
import java.util.Map;

public class ExtractorQuery {

    private String sql;
    private Map<String, ColumnMapping> mappings = new HashMap<String, ColumnMapping>();
    private int progressInterval = 500;

    public String getSql() {
        return sql;
    }

    public void setSql(final String sql) {
        this.sql = sql;
    }

    public void setMappings(final Map<String, ColumnMapping> mappings) {
        this.mappings = mappings;
    }

    public Map<String, ColumnMapping> getMappings() {
        return mappings;
    }

    public int getProgressInterval() {
        return progressInterval;
    }

    public void setProgressInterval(final int progressInterval) {
        this.progressInterval = progressInterval;
    }

}
