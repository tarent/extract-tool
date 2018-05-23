package de.tarent.extract;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tarent.extract.utils.ExtractorException;

public class HeaderProcessor {
    private static final Logger LOGGER = LogManager.getLogger(HeaderProcessor.class);

    private final Map<String, ColumnMapping> mappings;

    private final Properties properties;

    public HeaderProcessor(final Map<String, ColumnMapping> mappings) {
        this(mappings, null);
    }

    public HeaderProcessor(Map<String, ColumnMapping> mappings, Properties props) {
        this.mappings = mappings;
        this.properties = props;
    }

    public ResultSetValueExtractor[] processHeader(final ResultSet rs, final RowPrinter printer) throws ExtractorException {
        try {
            final ResultSetMetaData md = rs.getMetaData();
            final List<ResultSetValueExtractor> extractors = new ArrayList<ResultSetValueExtractor>();
            final List<String> headers = new ArrayList<String>();
            final int n = md.getColumnCount();
            for (int col = 0; col < n; col++) {
                final ColumnMapping mapping = mappings.get(md.getColumnLabel(col + 1).toUpperCase());
                if (mapping == null) {
                    extractors.add(null);
                } else {
                    headers.add(mapping.getMapTo());
                    extractors.add(createValueExtractor(mapping));

                }
            }
            printer.printRow(headers);
            return extractors.toArray(new ResultSetValueExtractor[extractors.size()]);
        } catch (final Exception e) {
            LOGGER.error(e);
            throw new ExtractorException(e);
        }
    }

    private ResultSetValueExtractor createValueExtractor(final ColumnMapping mapping)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final Class<? extends ResultSetValueExtractor> clazz = mapping.getExtractWith();
        final Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterTypes().length == 1
                    && constructor.getParameterTypes()[0].isAssignableFrom(Properties.class)) {
                return (ResultSetValueExtractor) constructor.newInstance(mergeProperties(mapping));
            }
            ;
        }
        return clazz.newInstance();
    }

    private Properties mergeProperties(ColumnMapping mapping) {
        final Map<String, ?> columnProperties = mapping.getProperties();
        if (columnProperties == null || columnProperties.isEmpty()) {
            return properties;
        }
        final Properties effectiveProperties=new Properties();
        effectiveProperties.putAll(properties);
        effectiveProperties.putAll(columnProperties);
        return effectiveProperties;
    }
}
