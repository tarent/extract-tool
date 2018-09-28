package de.tarent.extract;

import org.springframework.jdbc.core.JdbcTemplate;

public class DefaultCountStrategy implements CountStrategy {
	final private JdbcTemplate jdbcTemplate;

	public DefaultCountStrategy(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Integer count(ExtractorQuery query) {
		final String countSql = "SELECT COUNT(*) FROM (" + query.getSql() + ") alias42__";
		final Integer total = jdbcTemplate.queryForObject(countSql, Integer.class);
		return total;
	}
}
