package de.tarent.extract;

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
