package de.tarent.extract;

/*-
 * Extract-Tool is Copyright
 *  © 2015, 2016, 2018 Lukas Degener (l.degener@tarent.de)
 *  © 2018, 2019, 2020 mirabilos (t.glaser@tarent.de)
 *  © 2015 Jens Oberender (j.oberender@tarent.de)
 * Licensor is tarent solutions GmbH, http://www.tarent.de/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.evolvis.tartools.backgroundjobs.BackgroundJobMonitor;
import de.tarent.extract.utils.ExtractorException;

@Component
public class Extractor {
	private static final Logger LOGGER = LogManager.getLogger(Extractor.class);

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	CSVFormat csvFormat = CSVFormat.DEFAULT;

	CountStrategy countStrategy;

	HeaderProcessorFactory headerProcessorFactory = new DefaultHeaderProcessorFactory();

	RowFetcher rowFetcher;

	RowProcessorFactory rowProcessorFactory;

	public Extractor() {
		// used by spring
	}

	@Autowired
	public Extractor(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		this.rowProcessorFactory = new DefaultRowProcessorFactory(jdbcTemplate);
		this.rowFetcher = new DefaultRowFetcher(jdbcTemplate);
		this.countStrategy = new DefaultCountStrategy(jdbcTemplate);
	}

	private ObjectMapper mapper() {
		ObjectMapper mapper;
		try {
			final Class<? extends JsonFactory> yaml = Class
			    .forName("com.fasterxml.jackson.dataformat.yaml.YAMLFactory")
			    .asSubclass(JsonFactory.class);
			mapper = new ObjectMapper(yaml.getDeclaredConstructor().newInstance());
		} catch (final ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
			LOGGER.debug("YAML support not available, using JSON only", e);
			mapper = new ObjectMapper();
		}
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(MapperFeature.AUTO_DETECT_CREATORS, true);
		mapper.configure(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS, true);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
		return mapper;
	}

	private ExtractorQuery loadQuery(final ExtractIo io) {
		ExtractorQuery descriptor;
		try {
			descriptor = mapper().readValue(io.reader(), ExtractorQuery.class);
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

			final HeaderProcessor headerProcessor = headerProcessorFactory.create(query.getMappings(),
			    io.getProperties());
			// before fetching the actual rows, fire a query that will produce no results,
			// but provide us with the column meta data. This allows us to fail early if
			// our configuration is broken.
			final RowProcessor rowProcessor = rowProcessorFactory.create(query.getSql(), printer, headerProcessor);
			final Integer total = countStrategy.count(query);
			monitor.announceTotal(total);
			monitor.reportProgressAbsolute(0);
			rowFetcher.fetch(query, monitor, printer, rowProcessor);
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
