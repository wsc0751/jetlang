package org.jetlang.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/// <summary>
/// Default implementation.

/// </summary>
public class RunnableExecutorImpl implements RunnableExecutor {
    private final Object _lock = new Object();
    private volatile boolean _running = true;

    // TODO the ArrayBlockingQueue is generally a tad faster, but its capacity-limited..
    private final BlockingQueue<Runnable> _commands = new LinkedBlockingQueue<Runnable>();
    private final List<Disposable> _onStop = new ArrayList<Disposable>();

    private final RunnableInvoker _commandRunner;

    public RunnableExecutorImpl() {
        _commandRunner = new RunnableInvokerImpl();
    }

    public RunnableExecutorImpl(RunnableInvoker executor) {
        _commandRunner = executor;
    }

    /// <summary>
    /// Queue command.
    /// </summary>
    /// <param name="command"></param>
    public void execute(Runnable command) {
        _commands.add(command);
    }

    /// <summary>
    /// Remove all commands.
    /// </summary>
    /// <returns></returns>
    private Collection<Runnable> DequeueAll() {
        List<Runnable> dequeued = new ArrayList<Runnable>();
        Runnable command;

        try {
            command = _commands.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        dequeued.add(command);
        _commands.drainTo(dequeued);

        return dequeued;
    }

    /// <summary>
    /// Remove all commands and execute.
    /// </summary>
    /// <returns></returns>
    private void ExecuteNextBatch() {
        _commandRunner.executeAll(DequeueAll());
    }

    /// <summary>
    /// Execute commands until stopped.
    /// </summary>
    public void run() {
        while (_running) {
            ExecuteNextBatch();
        }
    }

    /// <summary>
    /// Stop consuming events.
    /// </summary>
    public void dispose() {
        _running = false;

        execute(new Runnable() {
            public void run() {
                // so it wakes up and will notice that we've told it to stop
            }
        });

        synchronized (_lock) {
            for (Disposable r : _onStop.toArray(new Disposable[_onStop.size()])) {
                r.dispose();
            }
        }
    }

    public void addOnStop(Disposable r) {
        synchronized (_lock) {
            _onStop.add(r);
        }
    }

    public boolean removeOnStop(Disposable disposable) {
        synchronized (_lock) {
            return _onStop.remove(disposable);
        }
    }


    public int registeredDisposableSize() {
        synchronized (_lock) {
            return _onStop.size();
        }
    }
}