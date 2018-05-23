package de.tarent.extract;

/*-
 * Extract-Tool is Copyright
 *  © 2015, 2016 Lukas Degener (l.degener@tarent.de)
 *  © 2018 mirabilos (t.glaser@tarent.de)
 *  © 2015 Jens Oberender (j.oberender@tarent.de)
 * Licensor is tarent solutions GmbH, http://www.tarent.de/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
