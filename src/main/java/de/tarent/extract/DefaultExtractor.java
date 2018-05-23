package de.tarent.extract;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DefaultExtractor implements ResultSetValueExtractor {

	@Override
	public Object extractValue(ResultSet rs, int col) throws SQLException {
		final String string = rs.getString(col + 1);
		return string==null?null:string.trim();
	}

}
