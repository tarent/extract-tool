package de.tarent.extract;

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
