package org.jetlang.core;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// A synchronous execute typically used for testing.

/// </summary>
public class SynchronousCommandQueue implements RunnableQueue, RunnableExecutor {
    private boolean _running = true;
    private List<Runnable> _onStop = new ArrayList<Runnable>();

    /// <summary>
    /// Queue command
    /// </summary>
    /// <param name="command"></param>
    public void execute(Runnable command) {
        if (_running) {
            command.run();
        }
    }

    public void onStop(Runnable runOnStop) {
        _onStop.add(runOnStop);
    }

    /// <summary>
    /// start Consuming events.
    /// </summary>
    public void run() {
        _running = true;
    }

    /// <summary>
    /// Stop consuming events.
    /// </summary>
    public void stop() {
        _running = false;
        for (Runnable run : _onStop) {
            run.run();
        }
    }
}