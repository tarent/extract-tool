package de.tarent.extract;

import java.io.IOException;
import java.io.StringWriter;

public class TestRowPrinter implements RowPrinter {
    private final StringWriter sw = new StringWriter();

    @Override
    public void printRow(final Iterable<?> values) throws IOException {
        boolean comma = false;
        for (final Object o : values) {
            if (comma) {
                sw.append(',');
            }
            comma = true;
            sw.append(o.toString());
        }
        sw.append('\n');
    }

    @Override
    public String toString() {
        return sw.toString();
    }
}
