package de.tarent.extract;

import java.sql.ResultSet;
import java.util.Arrays;

import de.tarent.extract.utils.ExtractorException;

public class RowProcessor {

    private final ResultSetValueExtractor[] extractors;
    private final Object[] buffer;
    private final int[] columns;
    private final Iterable<?> row;

    public RowProcessor(final ResultSetValueExtractor... extractors) {
        this.extractors = extractors;
        final int[] columnsNew = new int[extractors.length];
        int j = 0;
        for (int i = 0; i < extractors.length; i++) {
            if (extractors[i] != null) {
                columnsNew[j++] = i;
            }
        }
        this.columns = new int[j];
        System.arraycopy(columnsNew, 0, this.columns, 0, j);
        buffer = new Object[j];
        row = Arrays.asList(buffer);
    }

    public void processRow(final ResultSet rs, final RowPrinter printer) {
        try {
            for (int i = 0; i < buffer.length; i++) {
                final int col = columns[i];
                buffer[i] = extractors[col].extractValue(rs, col);
            }
            printer.printRow(row);
        } catch (final Exception e) {
            throw new ExtractorException(e);
        }
    }

}
