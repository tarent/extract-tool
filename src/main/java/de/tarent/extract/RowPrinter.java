package de.tarent.extract;

import java.io.IOException;

public interface RowPrinter {

	void printRow(Iterable<?> values) throws IOException;

}
