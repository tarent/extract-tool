package de.tarent.extract;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

import org.evolvis.tartools.backgroundjobs.BackgroundJobMonitor;

public interface ExtractIo {
    /**
     * @return maybe null
     */
    public File getOutputFile();

    /**
     * @return maybe null
     */
    public File getInputFile();

    public Reader reader() throws IOException;

    public Writer writer() throws IOException;

    public BackgroundJobMonitor getMonitor();

    /**
     *
     * @return maybe null
     */
    public Properties getProperties();
}
