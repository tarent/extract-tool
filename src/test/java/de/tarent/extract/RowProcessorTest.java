package de.tarent.extract;

/*-
 * Extract-Tool is Copyright
 *  © 2015, 2016, 2018 Lukas Degener (l.degener@tarent.de)
 *  © 2018, 2019 mirabilos (t.glaser@tarent.de)
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RowProcessorTest {

    @Mock
    private ResultSet rs;
    private RowPrinter printer;

    @Before
    public void setup() {
        printer = new TestRowPrinter();
    }

    @Test
    public void itPrintsEmptyRowsIfNoValueExtractorsAreGiven() throws SQLException, IOException {
        new RowProcessor().processRow(rs, printer);
        verifyZeroInteractions(rs);
        assertThat(printer.toString()).isEqualTo("\n");
    }

    @Test
    public void itUsesTheValueExtractorsGivenAtConstructionTime() throws SQLException, IOException {
        final ResultSetValueExtractor a = extractor("a");
        final ResultSetValueExtractor b = extractor("b");
        new RowProcessor(a, b).processRow(rs, printer);
        verifyZeroInteractions(rs);
        assertThat(printer.toString()).isEqualTo("0:a,1:b\n");
    }

    @Test
    public void itSkipsColumnsForWhichANullExtractorWasGiven() throws SQLException, IOException {
        final ResultSetValueExtractor a = extractor("a");
        final ResultSetValueExtractor b = extractor("b");
        new RowProcessor(a, null, null, b).processRow(rs, printer);
        verifyZeroInteractions(rs);
        assertThat(printer.toString()).isEqualTo("0:a,3:b\n");
    }

    private ResultSetValueExtractor extractor(final String name) throws SQLException {
        final ResultSetValueExtractor mock = new ResultSetValueExtractor() {

            @Override
            public Object extractValue(final ResultSet rs, final int col) throws SQLException {
                return col + ":" + name;
            }
        };
        return mock;
    }
}
