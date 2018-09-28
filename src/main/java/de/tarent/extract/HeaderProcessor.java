package de.tarent.extract;

import java.io.IOException;

/*-
 * Extract-Tool is Copyright
 *  © 2015, 2016 Lukas Degener (l.degener@tarent.de)
 *  © 2018 mirabilos (t.glaser@tarent.de)
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
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

	@Deprecated
	public ResultSetValueExtractor[] processHeader(final ResultSet rs, final RowPrinter printer)
			throws ExtractorException {
		try {
			final ResultSetMetaData md = rs.getMetaData();
			return processHeader(md, printer);
		} catch (final Exception e) {
			LOGGER.error(e);
			throw new ExtractorException(e);
		}
	}

	public ResultSetValueExtractor[] processHeader(final ResultSetMetaData md, final RowPrinter printer)
			throws ExtractorException {
		final List<ResultSetValueExtractor> extractors = new ArrayList<ResultSetValueExtractor>();
		final List<String> headers = new ArrayList<String>();
		try {
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
		} catch (SQLException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | IOException e) {
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
		final Properties effectiveProperties = new Properties();
		effectiveProperties.putAll(properties);
		effectiveProperties.putAll(columnProperties);
		return effectiveProperties;
	}
}
