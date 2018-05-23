package de.tarent.extract;

import org.evolvis.tartools.backgroundjobs.BackgroundJobMonitor;

public class DummyMonitor implements BackgroundJobMonitor {
    private final String jobId;

    public DummyMonitor(final String jobId) {
        this.jobId = jobId;
    }

    @Override
    public void log(final Severity severity, final Object message) {
    }

    @Override
    public boolean isAborting() {
        return false;
    }

    @Override
    public String getScheduledJobId() {
        return jobId;
    }

    @Override
    public void announceTotal(final int totalItems) {
    }

    @Override
    public void reportProgressIncrement(final int items) {
    }

    @Override
    public void reportProgressAbsolute(final int items) {
    }
}
