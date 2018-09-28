package de.tarent.extract;

public interface RowProcessorFactory {

	RowProcessor create(String sql, RowPrinter printer, HeaderProcessor headerProcessor);

}
