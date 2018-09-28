package de.tarent.extract;

import java.util.Map;
import java.util.Properties;

public interface HeaderProcessorFactory {

	HeaderProcessor create(Map<String, ColumnMapping> mappings, Properties properties);

}
