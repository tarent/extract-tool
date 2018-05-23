package de.tarent.extract;

/*-
 * Extract-Tool is Copyright
 *  © 2015, 2016 Lukas Degener (l.degener@tarent.de)
 *  © 2018 mirabilos (t.glaser@tarent.de)
 *  © 2015 Jens Oberender (j.oberender@tarent.de)
 * Licensor is tarent solutions GmbH, http://www.tarent.de/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import de.tarent.extract.utils.ExtractCliException;
import org.apache.commons.cli.*;
import org.evolvis.tartools.backgroundjobs.BackgroundJobMonitor;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.Map;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

public class ExtractCli implements ExtractIo {
    private File inputFile;
    private File outputFile;
    private String outputEncoding = "utf-8";
    private String inputEncoding = "utf-8";
    private Properties properties = null;
    private ClassLoader jdbcLoader = this.getClass().getClassLoader();
    private final CommandLine cmd;

    public ExtractCli(final String... args) throws ExtractCliException {
        this(System.getProperties(), System.getenv(), args);
    }

    public ExtractCli(Properties sysProps, Map<String, String> env, final String... args) throws ExtractCliException {
        final CommandLineParser parser = new BasicParser();
        final Options options = getOptions();
        File home = home(sysProps, env);
        try {
            cmd = parser.parse(options, args);
        } catch (final ParseException e) {
            throw new ExtractCliException(options, e);
        }
        if (cmd.getArgs().length == 1) {
            inputFile = new File(cmd.getArgs()[0]);
            if (!inputFile.canRead()) {
                throw new ExtractCliException(options, "Cannot read from input file " + inputFile);
            }
        }
        if (cmd.getArgs().length > 1) {
            throw new ExtractCliException(options, "Cannot process more than one input file");
        }
        if (cmd.hasOption('o')) {
            outputFile = new File(cmd.getOptionValue('o'));
        }
        if (cmd.hasOption('I')) {
            inputEncoding = cmd.getOptionValue('I');
        }
        if (cmd.hasOption('O')) {
            outputEncoding = cmd.getOptionValue('O');
        }
        if (cmd.hasOption('J')) {
            final String[] jarNames = cmd.getOptionValue('J').split(":");
            URL[] jarPaths = new URL[jarNames.length];
            for (int i = 0; i < jarNames.length; ++i)
                try {
                    jarPaths[i] = new File(jarNames[i]).toURI().toURL();
                } catch (MalformedURLException e) {
                    throw new ExtractCliException(options, e);
                }
            jdbcLoader = new URLClassLoader(jarPaths, this.getClass().getClassLoader());
        }
        if (cmd.hasOption('c')) {
            properties = loadProperties(options, new File(cmd.getOptionValue('c')));
        } else if (home != null) {
            properties = loadProperties(options, new File(home, "extract.properties"));
        }
        if (properties == null) {
            throw new ExtractCliException(options, "You need to either set the environment variable 'EXTRACTTOOL_HOME' or system property 'extracttool.home'.\n"
                    + "It should point to a directory containing a file 'extract.properties'. \n"
                    + "Alternatively, you can use the -c option to provide a custom properties file.");
        }
    }

    private File home(Properties sysProps, Map<String, String> env) {
        String home = sysProps.getProperty("extracttool.home");
        if (home == null) {
            home = env.get("EXTRACTTOOL_HOME");
        }
        if (home == null) {
            return null;
        }
        return new File(home);
    }

    private Properties loadProperties(final Options options, final File file) throws ExtractCliException {
        if (!file.canRead()) {
            throw new ExtractCliException(options, "Cannot read properties from " + file);
        }
        try {
            Properties properties = new Properties();
            final Reader reader = new InputStreamReader(new FileInputStream(file), "utf-8");
            properties.load(reader);
            reader.close();
            return properties;
        } catch (IOException e) {
            // I think all interesting cases are already covered...
            throw new RuntimeException(e);
        }
    }

    private Options getOptions() {
        final Options options = new Options();
        options.addOption("c", "configuration", true, "Properties file overriding the default connection settings");
        options.addOption("I", "input-encoding", true, "input encoding to use (Default: UTF-8)");
        options.addOption("J", "jdbc-jar", true, "load JDBC driver from JAR file");
        options.addOption("O", "output-encoding", true, "output encoding to use (Default: UTF-8)");
        options.addOption("o", true, "write output to given file");
        options.addOption("q", false, "do not report progress");
        options.addOption("z", false, "gzip the output");
        return options;
    }

    public InputStream input() throws IOException {
        if (inputFile != null) {
            return new FileInputStream(inputFile);
        }
        return System.in;
    }

    public PrintStream output() throws IOException {
        if (cmd.hasOption('z')) {
            return new PrintStream(new GZIPOutputStream(actualOutput()));
        }
        return actualOutput();
    }

    public PrintStream actualOutput() throws IOException {
        if (outputFile != null) {
            return new PrintStream(outputFile, outputEncoding);
        }
        return System.out;
    }

    @Override
    public File getOutputFile() {
        return outputFile;
    }

    @Override
    public Reader reader() throws IOException {
        return new InputStreamReader(input(), inputEncoding);
    }

    @Override
    public Writer writer() throws IOException {
        return new OutputStreamWriter(output(), outputEncoding);
    }

    @Override
    public File getInputFile() {
        return inputFile;
    }

    public String getOutputEncoding() {
        return outputEncoding;
    }

    public String getInputEncoding() {
        return inputEncoding;
    }

    @Override
    public BackgroundJobMonitor getMonitor() {
        if (cmd.hasOption('q')) {
            return new DummyMonitor("egal");
        }
        return new ConsoleProgressMonitor(new PrintWriter(System.err), 50);
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    Class<? extends Driver> loadJdbc(String driverClassName) throws ClassNotFoundException {
        final Class clazz = Class.forName(driverClassName, true, jdbcLoader);
        if (clazz == null)
            throw new ClassNotFoundException("class " + driverClassName + " not found");
        if (!(Driver.class.isAssignableFrom(clazz)))
            throw new ClassNotFoundException("class " + driverClassName + " not a JDBC Driver");
        //noinspection unchecked
        return clazz;
    }
}
