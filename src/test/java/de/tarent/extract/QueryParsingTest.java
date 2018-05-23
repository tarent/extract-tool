package de.tarent.extract;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import de.tarent.extract.utils.ExtractorException;

public class QueryParsingTest {
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test() throws JsonParseException, JsonMappingException, IOException {
        InputStreamReader reader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("test-query.yaml"));
        ExtractorQuery descriptor = mapper().readValue(reader,    ExtractorQuery.class);
        Map<String, ?> properties = descriptor.getMappings().get("fnarz").getProperties();
        assertEquals(4, properties.size());
        assertEquals("bang",properties.get("bar"));
        assertEquals("42",properties.get("foo"));
        assertEquals(42,properties.get("fee"));
        assertTrue(properties.get("baz") instanceof Map);

    }
    private ObjectMapper mapper() {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(MapperFeature.AUTO_DETECT_CREATORS, true);
        mapper.configure(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS, true);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
        return mapper;
    }
}
