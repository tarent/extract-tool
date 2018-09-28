package de.tarent.extract;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;

public class DefaultRowProcessorFactory implements RowProcessorFactory {
	private final JdbcTemplate jdbcTemplate;

	public DefaultRowProcessorFactory(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public RowProcessor create(final String sql, final RowPrinter printer, final HeaderProcessor headerProcessor) {
		return jdbcTemplate.execute(sql, new PreparedStatementCallback<RowProcessor>() {

			@Override
			public RowProcessor doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
				final ResultSetMetaData metaData = ps.getMetaData();
				ResultSetValueExtractor[] extractors = headerProcessor.processHeader(metaData, printer);
				return new RowProcessor(extractors);
			}
		});

	}
}
