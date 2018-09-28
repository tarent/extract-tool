package de.tarent.extract;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;

@RunWith(MockitoJUnitRunner.class)
public class DefaultCountStrategyTest {

	@Mock
	private JdbcTemplate jdbcTemplate;
	@Mock
	private ExtractorQuery query;
	private DefaultCountStrategy strategy;

	@Before
	public void setup() {
		strategy = new DefaultCountStrategy(jdbcTemplate);
	}

	@Test
	public void countQueryDoesNotIncludeOrderByClause() {
		when(query.getOrderBy()).thenReturn("foo ASC");
		when(query.getSql()).thenReturn("SELECT quatsch FROM unsinn");
		ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
		strategy.count(query);
		verify(jdbcTemplate).queryForObject(sqlCaptor.capture(), eq(Integer.class));
		assertEquals(-1,sqlCaptor.getValue().indexOf("foo ASC"));
	}

}
