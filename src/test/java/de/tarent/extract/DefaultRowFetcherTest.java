package de.tarent.extract;

/*-
 * Extract-Tool is Copyright
 *  © 2015, 2016, 2018 Lukas Degener (l.degener@tarent.de)
 *  © 2018, 2019 mirabilos (t.glaser@tarent.de)
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

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import org.evolvis.tartools.backgroundjobs.BackgroundJobMonitor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

@RunWith(MockitoJUnitRunner.class)
public class DefaultRowFetcherTest {
	@Mock
	private JdbcTemplate jdbcTemplate;
	@Mock
	private ExtractorQuery query;
	@Mock
	private BackgroundJobMonitor monitor;
	@Mock
	private RowPrinter printer;
	@Mock
	private RowProcessor rowProcessor;

	private DefaultRowFetcher fetcher;
	@Before
	public void setup() {
		fetcher = new DefaultRowFetcher(jdbcTemplate);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void queryIncludesOrderByClause() {
		when(query.getOrderBy()).thenReturn("foo ASC");
		when(query.getSql()).thenReturn("SELECT quatsch FROM unsinn");
		ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
		fetcher.fetch(query, monitor, printer, rowProcessor);
		verify(jdbcTemplate).query(sqlCaptor.capture(), any(ResultSetExtractor.class));
		assertEquals("SELECT quatsch FROM unsinn ORDER BY foo ASC", sqlCaptor.getValue());
	}
}
