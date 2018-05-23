package de.tarent.extract;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.evolvis.tartools.backgroundjobs.BackgroundJobMonitor;
import de.tarent.extract.utils.ExtractorException;

@Component
public class Extractor {
    private static final Logger LOGGER = LogManager.getLogger(Extractor.class);

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    CSVFormat csvFormat = CSVFormat.DEFAULT;

    public Extractor() {
        // used by spring
    }

    public Extractor(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private ObjectMapper mapper() {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(MapperFeature.AUTO_DETECT_CREATORS, true);
        mapper.configure(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS, true);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
        return mapper;
    }

    private ExtractorQuery loadQuery(final ExtractIo io) {
        ExtractorQuery descriptor;
        try {
            descriptor = mapper().readValue(io.reader(),
                    ExtractorQuery.class);
        } catch (final JsonParseException e) {
            LOGGER.error("Couldn't parse json", e);
            throw new ExtractorException("Couldn't parse json", e);
        } catch (final JsonMappingException e) {
            LOGGER.error("Couldn't map json", e);
            throw new ExtractorException("Couldn't map json", e);
        } catch (final IOException e) {
            LOGGER.error("Could not load configuration", e);
            throw new ExtractorException("Could not load configuration", e);
        }
        return descriptor;
    }

    public void run(final ExtractIo io) {
        run(io, loadQuery(io));
    }

    public void run(final ExtractIo io, final ExtractorQuery query) {
        final BackgroundJobMonitor monitor = io.getMonitor();
        final CSVPrinter csvPrinter;
        try {
            csvPrinter = csvFormat.print(io.writer());
        } catch (final IOException e) {
            LOGGER.error("Could not create writer", e);
            throw new ExtractorException("Could not create writer", e);
        }
        final RowPrinter printer = new RowPrinter() {

            @Override
            public void printRow(final Iterable<?> values) throws IOException {
                csvPrinter.printRecord(values);
            }
        };

        normalizeQuery(query);

        try {
            final String countSql = "SELECT COUNT(*) FROM (" + query.getSql() + ") alias42__";

            final String sql = "SELECT * FROM (" + query.getSql() + ") alias42__";

            // before fetching the actual rows, fire a query that will produce no results,
            // but provide us with the column meta data. This allows us to fail early if
            // our configuration is broken.
            final RowProcessor rowProcessor = jdbcTemplate.query(sql + " WHERE 0=1", new ResultSetExtractor<RowProcessor>() {

                @Override
                public RowProcessor extractData(final ResultSet rs) throws SQLException, DataAccessException {
                    final HeaderProcessor headerProcessor = new HeaderProcessor(query.getMappings(),io.getProperties());
                    ResultSetValueExtractor[] extractors;
                    extractors = headerProcessor.processHeader(rs, printer);
                    return new RowProcessor(extractors);
                }
            });

            final Integer total = jdbcTemplate.queryForObject(countSql, Integer.class);
            monitor.announceTotal(total);
            monitor.reportProgressAbsolute(0);
            jdbcTemplate.query(sql, new ResultSetExtractor<Void>() {

                @Override
                public Void extractData(final ResultSet rs) throws SQLException, DataAccessException {
                    int rownum = 0;
                    while (rs.next()) {
                        rowProcessor.processRow(rs, printer);
                        if (rownum++ % query.getProgressInterval() == 0) {
                            monitor.reportProgressAbsolute(rownum);
                        }
                    }
                    monitor.reportProgressAbsolute(rownum);
                    return null;
                }
            });
        } finally {
            try {
                csvPrinter.close();
            } catch (final IOException e) {
                LOGGER.error("Could not close csv writer", e);
            }
        }
    }

    private void normalizeQuery(final ExtractorQuery query) {
        final Map<String, ColumnMapping> mappings = new HashMap<String, ColumnMapping>();
        for (final Entry<String, ColumnMapping> entry : query.getMappings().entrySet()) {
            mappings.put(entry.getKey().toUpperCase(), entry.getValue());
        }
        query.setMappings(mappings);
    }
}
