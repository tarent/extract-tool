package de.tarent.extract.utils;

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

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExtractCliException extends Exception {
    private static final long serialVersionUID = 3654373558875728264L;

    private final String usage;

    public ExtractCliException(final Options options, final Exception e) {
        super(e);
        usage = usage(options);
    }

    public ExtractCliException(final Options options, final String message) {
        super(message);
        usage = usage(options);
    }

    public String getUsage() {
        return usage;
    }

    private static String usage(final Options options) {
        final StringWriter out = new StringWriter();
        new HelpFormatter().printHelp(new PrintWriter(out), 80, "extract", null, options, 2, 2, null, true);
        return out.toString().trim();
    }
}
