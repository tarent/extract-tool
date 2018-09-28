package de.tarent.extract;

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
