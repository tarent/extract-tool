package de.tarent.extract;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetValueExtractor {
	public Object extractValue(ResultSet rs, int col) throws SQLException;
}
