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

import de.tarent.extract.utils.ExtractCliException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import java.io.IOException;
import java.sql.Driver;
import java.util.Properties;

public class Main {
    private static Class<? extends Driver> jdbcDriverClass;

    private Main() {
    }

    public static void main(final String[] args) throws IOException, ClassNotFoundException {
        final AnnotationConfigApplicationContext cx = new AnnotationConfigApplicationContext();
        try {
            final ExtractCli cli = new ExtractCli(args);

            addPropertySource(cx, cli.getProperties());

            jdbcDriverClass = cli.loadJdbc(cx.getEnvironment().getProperty("jdbc.driver"));
            cx.register(SpringConfiguration.class);
            cx.refresh();
            cx.getBean(Extractor.class).run(cli);
        } catch (final ExtractCliException e) {
            System.err.println(e.getMessage());
            System.err.println(e.getUsage());
        } finally {
            cx.close();
        }
    }

    private static void addPropertySource(final AnnotationConfigApplicationContext cx, final Properties properties) {
        final PropertySource<?> ps = new PropertiesPropertySource("defaultProperties", properties);
        cx.getEnvironment().getPropertySources().addFirst(ps);
    }

    public static Class<? extends Driver> getJdbcDriverClass() {
        return jdbcDriverClass;
    }
}
