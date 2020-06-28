package de.tarent.extract;

/*-
 * Extract-Tool is Copyright
 *  © 2015, 2016, 2018 Lukas Degener (l.degener@tarent.de)
 *  © 2018, 2019, 2020 mirabilos (t.glaser@tarent.de)
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HeaderProcessorTest {
    @Mock
    private ResultSet rs;

    private RowPrinter printer;
    private final Map<String, ColumnMapping> mappings = new HashMap<>();

    @Mock
    private ResultSetMetaData metadata;

    public void columnLabels(final String... labels) throws SQLException {

        when(metadata.getColumnCount()).thenReturn(labels.length);
        when(metadata.getColumnLabel(anyInt())).thenAnswer((Answer<String>) invocation -> {
            final int col = invocation.getArgumentAt(0, Integer.class) - 1;
            return labels[col];
        });
    }

    @Before
    public void setup() throws SQLException {

        printer = new TestRowPrinter();
        when(rs.getMetaData()).thenReturn(metadata);
    }

    @Test
    public void itPrintsEmptyRowsIfNoMappingsAreGiven() {

        final ResultSetValueExtractor[] extractors = new HeaderProcessor(mappings).processHeader(metadata, printer);

        assertThat(printer.toString()).isEqualTo("\n");
        assertThat(extractors).isNotNull();
        assertThat(extractors).isEmpty();
    }

    public static class FooExtractor implements ResultSetValueExtractor {

        @Override
        public Object extractValue(final ResultSet rs, final int col) {
            return null;
        }
    }

    public static class ConfigurableExtractor implements ResultSetValueExtractor {

        private final Properties properties;

        public ConfigurableExtractor(Properties properties) {
            this.properties = properties;
        }

        @Override
        public Object extractValue(ResultSet rs, int col) {
            return properties;
        }
    }

    public static class NestedConfigurableExtractor implements ResultSetValueExtractor {

        private final Map<String, ?> properties;

        public NestedConfigurableExtractor(Map<String, ?> properties) {
            this.properties = properties;
        }

        @Override
        public Object extractValue(ResultSet rs, int col) {
            return properties;
        }
    }

    @Test
    public void itUsesMappingsGivenAtConstructionTime() throws SQLException {
        columnLabels("bar", "foo");
        mappings.put("FOO", new ColumnMapping("the.foo", FooExtractor.class));
        mappings.put("BAR", new ColumnMapping("the.bar"));
        final ResultSetValueExtractor[] extractors = new HeaderProcessor(mappings).processHeader(metadata, printer);
        assertThat(printer.toString()).isEqualTo("the.bar,the.foo\n");
        assertThat(extractors.length).isEqualTo(2);
        assertThat(extractors).doesNotContainNull();
        assertThat(extractors[0]).isInstanceOf(DefaultExtractor.class);
        assertThat(extractors[1]).isInstanceOf(FooExtractor.class);
    }

    @Test
    public void itCanPassCustomPropertiesToTheResultSetValueExtractors() throws SQLException {
        Properties props = new Properties();
        props.put("arg.a", "a");
        props.put("arg.b", "b");
        columnLabels("bar", "foo");
        mappings.put("FOO", new ColumnMapping("the.foo", FooExtractor.class));
        mappings.put("BAR", new ColumnMapping("the.bar", ConfigurableExtractor.class));
        final ResultSetValueExtractor[] extractors = new HeaderProcessor(mappings, props).processHeader(metadata, printer);
        assertThat(printer.toString()).isEqualTo("the.bar,the.foo\n");
        assertThat(extractors.length).isEqualTo(2);
        assertThat(extractors).doesNotContainNull();
        assertThat(extractors[0]).isInstanceOf(ConfigurableExtractor.class);
        assertThat(extractors[1]).isInstanceOf(FooExtractor.class);
        Properties effectiveProperties = (Properties) extractors[0].extractValue(rs, 42);
        assertThat(effectiveProperties.getProperty("arg.a")).isEqualTo("a");
        assertThat(effectiveProperties.getProperty("arg.b")).isEqualTo("b");
    }

    @Test
    public void itWillMergePropertiesGivenInTheColumnMapping() throws SQLException {
        Properties props = new Properties();
        props.put("arg.a", "a");
        props.put("arg.b", "b");
        Map<String, String> colProps = new HashMap<>();
        colProps.put("arg.b", "b'");
        colProps.put("arg.c", "c'");
        columnLabels("bar", "foo");
        mappings.put("FOO", new ColumnMapping("the.foo", FooExtractor.class));
        mappings.put("BAR", new ColumnMapping("the.bar", ConfigurableExtractor.class, colProps));
        final ResultSetValueExtractor[] extractors = new HeaderProcessor(mappings, props).processHeader(metadata, printer);
        assertThat(printer.toString()).isEqualTo("the.bar,the.foo\n");
        assertThat(extractors.length).isEqualTo(2);
        assertThat(extractors).doesNotContainNull();
        assertThat(extractors[0]).isInstanceOf(ConfigurableExtractor.class);
        assertThat(extractors[1]).isInstanceOf(FooExtractor.class);
        Properties effectiveProperties = (Properties) extractors[0].extractValue(rs, 42);
        assertThat(effectiveProperties.getProperty("arg.a")).isEqualTo("a");
        assertThat(effectiveProperties.getProperty("arg.b")).isEqualTo("b'");
        assertThat(effectiveProperties.getProperty("arg.c")).isEqualTo("c'");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void itSupportsArbitraryNestedMapsAsProperties() throws SQLException {
        Properties props = new Properties();
        props.put("arg.a", "a");
        props.put("arg.b", "b");
        Map<String, Object> colProps = new HashMap<>();
        colProps.put("arg.b", "b'");
        Map<String, String> nestedMap = new HashMap<>();
        nestedMap.put("arg.d", "d");
        colProps.put("arg.c", nestedMap);
        columnLabels("bar", "foo");
        mappings.put("FOO", new ColumnMapping("the.foo", FooExtractor.class));
        mappings.put("BAR", new ColumnMapping("the.bar", NestedConfigurableExtractor.class, colProps));
        final ResultSetValueExtractor[] extractors = new HeaderProcessor(mappings, props).processHeader(metadata, printer);
        assertThat(printer.toString()).isEqualTo("the.bar,the.foo\n");
        assertThat(extractors.length).isEqualTo(2);
        assertThat(extractors).doesNotContainNull();
        assertThat(extractors[0]).isInstanceOf(NestedConfigurableExtractor.class);
        assertThat(extractors[1]).isInstanceOf(FooExtractor.class);
        Map<String, ?> effectiveProperties = (Map<String, ?>) extractors[0].extractValue(rs, 42);
        assertThat(effectiveProperties.get("arg.a")).isEqualTo("a");
        assertThat(effectiveProperties.get("arg.b")).isEqualTo("b'");
        Map<String, ?> effectiveNestedMap = (Map<String, ?>) effectiveProperties.get("arg.c");
        assertThat(effectiveNestedMap.get("arg.d")).isEqualTo("d");
    }
}
