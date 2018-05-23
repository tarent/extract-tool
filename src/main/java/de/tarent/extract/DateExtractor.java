package de.tarent.extract;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DateExtractor implements ResultSetValueExtractor {

    @Override
    public Object extractValue(final ResultSet rs, final int col) throws SQLException {
        final Date date = rs.getDate(col + 1);
        return date == null ? null : date.getTime();
    }

}
