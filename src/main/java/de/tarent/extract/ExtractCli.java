package de.tarent.extract;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.evolvis.tartools.backgroundjobs.BackgroundJobMonitor;
import de.tarent.extract.utils.ExtractCliException;

public class ExtractCli implements ExtractIo {
    private File inputFile;
    private File outputFile;
    private String outputEncoding = "utf-8";
    private String inputEncoding = "utf-8";
    private Properties properties = null;
    private final CommandLine cmd;

    public ExtractCli(final String... args) throws ExtractCliException {
        this(System.getProperties(),System.getenv(), args);
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
        if (cmd.hasOption('c')) {
            properties = loadProperties(options, new File(cmd.getOptionValue('c')));
        } else if(home!=null){
            properties = loadProperties(options, new File(home,"extract.properties"));
        }
        if (properties==null) {
            throw new ExtractCliException(options,"You need to either set the environment variable 'EXTRACTTOOL_HOME' or system property 'extracttool.home'.\n"
                    + "It should point to a directory containing a file 'extract.properties'. \n"
                    + "Alternatively, you can use the -c option to provide a custom properties file.");
        }
    }

    private File home(Properties sysProps, Map<String, String> env) {
        String home = sysProps.getProperty("extracttool.home");
        if(home==null){
            home = env.get("EXTRACTTOOL_HOME");
        }
        if(home==null){
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
}
