package de.tarent.extract;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.csv.CSVFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@ComponentScan("de.tarent.extract")
public class SpringConfiguration {
    @Value("${jdbc.url}")
    private String jdbcUrl;
    @Value("${jdbc.driver}")
    private String jdbcDriver;
    @Value("${jdbc.username}")
    private String jdbcUsername;
    @Value("${jdbc.password}")
    private String jdbcPassword;

    @Autowired
    Environment env;

    @Bean
    public Properties jdbcProperties() {
        final Properties properties = new Properties();
        // FIXME: this sucks big time. Any better way to do it?
        // The idea is to get all properties from the environment that start
        // with 'jdbc.' and put them into a Properties object that
        // I can pass along as connection properties to the jdbc driver.
        for (final org.springframework.core.env.PropertySource<?> ps : ((AbstractEnvironment) env)
                .getPropertySources()) {
            if (ps instanceof MapPropertySource) {
                for (final String name : ((MapPropertySource) ps)
                        .getPropertyNames()) {
                    final String prefix = "jdbc.";
                    if (name.startsWith(prefix)) {
                        properties.setProperty(name.substring(prefix.length()),
                                env.getProperty(name));
                    }
                }
            }
        }

        return properties;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public DataSource dataSource() throws SQLException {
        final DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setUrl(jdbcUrl);
        ds.setDriverClassName(jdbcDriver);
        ds.setUsername(jdbcUsername);
        ds.setPassword(jdbcPassword);

        ds.setConnectionProperties(jdbcProperties());
        return ds;
    }

    @Bean
    JdbcTemplate jdbcTemplate(final DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean
    CSVFormat csvFormat() {
        return CSVFormat.DEFAULT;
    }

}
