package de.tarent.extract;

/*-
 * Extract-Tool is Copyright
 *  © 2015, 2016, 2018 Lukas Degener (l.degener@tarent.de)
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

import org.evolvis.tartools.backgroundjobs.BackgroundJobMonitor;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ConsoleProgressMonitor implements BackgroundJobMonitor {
    private final PrintWriter printWriter;
    private int total;
    private int done;
    private final int barWidth;
    private int lastLength = 0;
    private boolean hasEnded = false;

    public ConsoleProgressMonitor(final PrintWriter printWriter, final int barWidth) {
        this.printWriter = printWriter;
        this.barWidth = barWidth;
    }

    @Override
    public void log(final Severity severity, final Object message) {
        printWriter.println(severity + ": " + message);
    }

    @Override
    public boolean isAborting() {
        return false;
    }

    @Override
    public String getScheduledJobId() {
        return "cli-job";
    }

    @Override
    public void announceTotal(final int totalItems) {
        total = totalItems;
        /* printProgress(); */ // strictly speaking, this is needed
    }

    @Override
    public void reportProgressIncrement(final int items) {
        done += items;
        printProgress();
    }

    private void printProgress() {
        // "Progress: [======..............] 204099 of 300000"
        final StringWriter sb = new StringWriter();
        sb.write("Progress: [");
        for (int i = 0; i < barWidth; i++) {
            final double doneRatio = ((double) done) / ((double) total);
            if ((i) < doneRatio * (barWidth)) {
                sb.write("=");
            } else {
                sb.write(".");
            }
        }
        sb.write("] ");
        sb.write("" + done);
        sb.write(" of ");
        sb.write("" + total);
        final int currentLength = sb.toString().length();
        for (int i = sb.toString().length(); i < lastLength; i++) {
            sb.append(' ');
        }

        lastLength = currentLength;
        printWriter.print(sb.toString());
        printWriter.print('\r');
        if (done < total) {
            hasEnded = false;
        } else if (!hasEnded) {
            printWriter.print('\n');
            hasEnded = true;
        }
        printWriter.flush();
    }

    @Override
    public void reportProgressAbsolute(final int items) {
        done = items;
        printProgress();
    }
}
