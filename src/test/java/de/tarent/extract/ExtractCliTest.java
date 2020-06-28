package de.tarent.extract;

/*-
 * Extract-Tool is Copyright
 *  © 2015, 2016, 2018 Lukas Degener (l.degener@tarent.de)
 *  © 2018, 2019, 2020 mirabilos (t.glaser@tarent.de)
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class ExtractCliTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();
    private File inputFile;
    private File outputFile;
    private File home;
    private Map<String, String> env;
    private File customPropertiesFile;
    @SuppressWarnings("FieldCanBeLocal")
    private File defaultPropertiesFile;
    private Properties sysProps;

    @Before
    public void setup() throws IOException {
        env = new HashMap<>();
        sysProps = new Properties();
        inputFile = tmp.newFile();
        outputFile = tmp.newFile();
        home = tmp.newFolder();
        env.put("EXTRACTTOOL_HOME", home.getAbsolutePath());
        defaultPropertiesFile = new File(home, "extract.properties");
        customPropertiesFile = tmp.newFile();
        FileUtils.write(defaultPropertiesFile, "jdbc.foo=bang\n", StandardCharsets.UTF_8);
        FileUtils.write(customPropertiesFile, "jdbc.foo=bar\n", StandardCharsets.UTF_8);
        FileUtils.write(inputFile, "expected input", StandardCharsets.UTF_8);
    }

    @Test
    public void defaultsToStdIO() throws ExtractCliException, IOException {
        final ExtractCli cli = new ExtractCli(sysProps, env);
        assertThat(cli.input()).isSameAs(System.in);
        assertThat(cli.output()).isSameAs(System.out);
        assertThat(cli.getOutputFile()).isNull();
    }

    @Test
    public void takesFirstNonOptionArgAsInputFile() throws IOException,
      ExtractCliException {
        final ExtractCli cli = new ExtractCli(sysProps, env, inputFile.getPath());
        assertThat(cli.input()).isNotSameAs(System.in);
        assertThat(cli.output()).isSameAs(System.out);
        assertThat(cli.getOutputFile()).isNull();
        assertThat(IOUtils.toString(cli.input(), StandardCharsets.UTF_8)).isEqualTo("expected input");
    }

    @Test
    public void supportsWritingToAFile() throws IOException,
      ExtractCliException {
        final ExtractCli cli = new ExtractCli(sysProps, env, "-o", outputFile.getPath());
        assertThat(cli.input()).isSameAs(System.in);
        final PrintStream output = cli.output();
        assertThat(output).isNotSameAs(System.out);
        IOUtils.write("expected output", output, StandardCharsets.UTF_8);
        assertThat(cli.getOutputFile()).isEqualTo(outputFile);
        assertThat(FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8)).isEqualTo(
          "expected output");
    }

    @Test
    public void supportsReadingWithCustomCharEncoding()
      throws ExtractCliException, IOException {
        FileUtils.writeByteArrayToFile(inputFile, new byte[] {
          (byte) 0x80,
          (byte) 0xA4
        });
        assertThat(readEncoded("cp1252")).isEqualTo("€¤");
        FileUtils.writeByteArrayToFile(inputFile, new byte[] {
          (byte) 0xA4
        });
        assertThat(readEncoded("ISO-8859-15")).isEqualTo("€");
    }

    @Test
    public void supportsWritingWithCustomCharEncoding()
      throws ExtractCliException, IOException {
        final byte[] bs = writeEncoded("cp1252", "€¤");
        assertThat(bs[0]).isEqualTo((byte) 0x80);
        assertThat(bs[1]).isEqualTo((byte) 0xA4);
        final byte[] cs = writeEncoded("ISO-8859-15", "€");
        assertThat(cs[0]).isEqualTo((byte) 0xA4);
    }

    @Test
    public void doesNotRelyOnPlatformDefaultCharEncoding() throws ExtractCliException {
        final String actualCharset = new ExtractCli(sysProps, env).getOutputEncoding();
        assertThat(actualCharset).isEqualTo("utf-8");
        assertThat(actualCharset).isNotEqualTo(Charset.defaultCharset().name());
        assertThat(new ExtractCli(sysProps, env).getInputEncoding()).isEqualTo(actualCharset);
    }

    @Test
    public void disablesProgressReportWhenInQuietMode()
      throws ExtractCliException {
        final ExtractIo cli = new ExtractCli(sysProps, env, "-q");
        assertThat(cli.getMonitor()).isInstanceOf(DummyMonitor.class);
    }

    @Test
    public void canGZipTheOutput() throws ExtractCliException, IOException {
        final ExtractIo cli = new ExtractCli(sysProps, env, "-z", "-o", outputFile.getPath());
        final Writer writer = cli.writer();
        writer.write("hallo gezippte welt");
        writer.close();
        assertThat(FileUtils.readFileToByteArray(outputFile)).isEqualTo(
          bytes(0x1f, 0x8b, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0xcb, 0x48, 0xcc, 0xc9, 0xc9, 0x57, 0x48, 0x4f,
            0xad, 0xca, 0x2c, 0x28, 0x28, 0x49, 0x55, 0x28, 0x4f,
            0xcd, 0x29, 0x01, 0x00, 0xf2, 0x5a, 0x06, 0x1a, 0x13,
            0x00, 0x00, 0x00));
    }

    @Test
    public void canLoadPropertiesFromDifferentLocation() throws ExtractCliException {
        final ExtractIo cli = new ExtractCli(sysProps, env, "-c", customPropertiesFile.getPath());
        assertThat(cli.getProperties()).isInstanceOf(Properties.class);
        assertThat(cli.getProperties().get("jdbc.foo")).isEqualTo("bar");
    }

    @Test
    public void defaultsToReadingPropertiesFromToolHome() throws ExtractCliException {
        final ExtractIo cli = new ExtractCli(sysProps, env);
        assertThat(cli.getProperties()).isInstanceOf(Properties.class);
        assertThat(cli.getProperties().get("jdbc.foo")).isEqualTo("bang");
    }

    @Test
    public void settingToolHomeViaSystemPropertyOverridesSettingViaEnvironment() throws ExtractCliException {
        env.put("EXTRACTTOOL_HOME", "/mich/gibts/nicht");
        sysProps.setProperty("extracttool.home", home.getAbsolutePath());
        final ExtractIo cli = new ExtractCli(sysProps, env);
        assertThat(cli.getProperties()).isInstanceOf(Properties.class);
        assertThat(cli.getProperties().get("jdbc.foo")).isEqualTo("bang");
    }

    @Test
    public void complainsWhenNeitherToolHomeNorCustomPropertiesAreSet() {
        env.remove("EXTRACTTOOL_HOME");
        try {
            new ExtractCli(sysProps, env);
        } catch (ExtractCliException e) {
            assertThat(e.getMessage()).contains("'EXTRACTTOOL_HOME'");
            assertThat(e.getMessage()).contains("'extracttool.home'");
            assertThat(e.getMessage()).contains("-c");
        }
    }

    private byte[] bytes(final int... literals) {
        final byte[] bs = new byte[literals.length];
        for (int i = 0; i < bs.length; i++) {
            bs[i] = (byte) literals[i];
        }
        return bs;
    }

    private byte[] writeEncoded(final String enc, final String str)
      throws ExtractCliException, IOException {
        final ExtractIo cli = new ExtractCli(sysProps, env, "-o", outputFile.getPath(), "-O",
          enc, inputFile.getPath());
        final Writer writer = cli.writer();
        writer.write(str);
        writer.close();
        return FileUtils.readFileToByteArray(outputFile);
    }

    private String readEncoded(final String enc) throws IOException,
      ExtractCliException {
        final Reader reader = new ExtractCli(sysProps, env, "--input-encoding", enc,
          inputFile.getPath()).reader();
        final String string = IOUtils.toString(reader);
        reader.close();
        return string;
    }
}
