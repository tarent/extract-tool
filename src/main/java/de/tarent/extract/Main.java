package de.tarent.extract;

import java.io.IOException;
import java.util.Properties;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import de.tarent.extract.utils.ExtractCliException;

public class Main {
    private Main() {
    }

    public static void main(final String[] args) throws IOException {

        final AnnotationConfigApplicationContext cx = new AnnotationConfigApplicationContext();
        try {
            final ExtractCli cli = new ExtractCli(args);

            addPropertySource(cx, cli.getProperties());

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
}
