package de.tarent.extract;

import org.evolvis.tartools.backgroundjobs.BackgroundJobMonitor;

public interface RowFetcher {

	void fetch(ExtractorQuery query, BackgroundJobMonitor monitor, RowPrinter printer, RowProcessor rowProcessor);

}
