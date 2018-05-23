package de.tarent.extract;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BooleanExtractor implements ResultSetValueExtractor {

    @Override
    public Object extractValue(final ResultSet rs, final int col) throws SQLException {
        // do *not* use getBoolean! we need to detect NULL values!!!!
        final Number i = (Number) rs.getObject(col + 1);
        return i == null ? null : i.intValue() != 0;
    }

}
