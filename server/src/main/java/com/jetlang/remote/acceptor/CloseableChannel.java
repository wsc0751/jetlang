package com.jetlang.remote.acceptor;

import org.jetlang.channels.Channel;
import org.jetlang.channels.Subscribable;
import org.jetlang.core.Callback;
import org.jetlang.core.Disposable;
import org.jetlang.core.DisposingExecutor;

import java.util.ArrayList;
import java.util.List;

public class CloseableChannel<T> implements Channel<T> {

    private final Object lock = new Object();
    private boolean closed = false;
    private final List<Disposable> subscriptions = new ArrayList<Disposable>();

    private final Channel<T> target;

    public CloseableChannel(Channel<T> target) {
        this.target = target;
    }

    public void publish(T t) {
        target.publish(t);
    }

    public Disposable subscribe(DisposingExecutor disposingExecutor, Callback<T> tCallback) {
        synchronized (lock) {
            if (closed) {
                throw new RuntimeException("Channel is closed");
            }
            return wrap(target.subscribe(disposingExecutor, tCallback));
        }
    }

    public Disposable subscribe(Subscribable<T> tSubscribable) {
        synchronized (lock) {
            if (closed) {
                throw new RuntimeException("Channel is closed");
            }
            return wrap(target.subscribe(tSubscribable));
        }
    }

    private Disposable wrap(final Disposable subscribe) {
        Disposable remove = new Disposable() {
            public void dispose() {
                subscribe.dispose();
                synchronized (lock) {
                    subscriptions.remove(subscribe);
                }
            }
        };
        subscriptions.add(subscribe);
        return remove;
    }

    public void close() {
        synchronized (lock) {
            for (Disposable subscription : subscriptions) {
                subscription.dispose();
            }
            closed = true;
            subscriptions.clear();
        }
    }

    public static <T> CloseableChannel<T> wrap(Channel<T> channel) {
        return new CloseableChannel<T>(channel);
    }

    public static class Group {
        private final List<CloseableChannel<?>> allChannels = new ArrayList<CloseableChannel<?>>();

        public <T> CloseableChannel<T> add(Channel<T> c) {
            CloseableChannel<T> closeable = CloseableChannel.wrap(c);
            allChannels.add(closeable);
            return closeable;
        }

        public void closeAndClear() {
            for (CloseableChannel<?> allChannel : allChannels) {
                allChannel.close();
            }
            allChannels.clear();
        }

    }
}
