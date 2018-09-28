package de.tarent.extract;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.evolvis.tartools.backgroundjobs.BackgroundJobMonitor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

public class DefaultRowFetcher implements RowFetcher {

	final private JdbcTemplate jdbcTemplate;

	public DefaultRowFetcher(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void fetch(final ExtractorQuery query, final BackgroundJobMonitor monitor, final RowPrinter printer,
			final RowProcessor rowProcessor) {
		
		final String sql = query.getOrderBy()==null ? query.getSql() : query.getSql() + " ORDER BY "+ query.getOrderBy();
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
	}

}
