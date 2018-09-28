package de.tarent.extract;

import java.util.Map;
import java.util.Properties;

public class DefaultHeaderProcessorFactory implements HeaderProcessorFactory {
	@Override
	public HeaderProcessor create(Map<String, ColumnMapping> mappings, Properties properties) {
		return new HeaderProcessor(mappings, properties);
	}
}
