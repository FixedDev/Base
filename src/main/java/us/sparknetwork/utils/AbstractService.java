package us.sparknetwork.utils;

import us.sparknetwork.base.Service;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractService implements Service {

    private AtomicBoolean started;

    public AbstractService() {
        this.started = new AtomicBoolean();
    }

    @Override
    public void start() throws IllegalStateException {
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("The service is already started");
        }

        doStart();
    }

    @Override
    public void stop() {
        if (!started.compareAndSet(true, false)) {
            throw new IllegalStateException("The service isn't already started");
        }

        doStop();
    }

    protected abstract void doStart();
    protected abstract void doStop();

    @Override
    public boolean isStarted() {
        return started.get();
    }
}
