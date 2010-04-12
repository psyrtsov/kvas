/*
 * Copyright 2008 Pavel Syrtsov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations
 * under the License.
 */


package com.kvas;

import com.kvas.queue.DataOp;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public abstract class AStore<K, V> {
    public static final String REPLICA_SYNC_CLOCK_KEY = "REPLICA_SYNC_CLOCK";
    public static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    /**
     * ths value should be set by Queue Processor ,
     * it has to be null in context of DBClient when data aplied before sending change to the queue
     */
    private static final ThreadLocal<DataOp> currentDataOp = new ThreadLocal<DataOp>();
    private final KVStore<String, StampedData<V>> tempStore;
    private final KVStore persistentStore;
    private final String prefix;
    private volatile long replicaSyncClock = 0;

    public AStore(KVStore<String, StampedData<V>> tempStore, KVStore<K, V> persistentStore, String prefix) {
        this.tempStore = tempStore;
        this.persistentStore = persistentStore;
        this.prefix = prefix;
        EXECUTOR.schedule(new ReplicaSyncClockTracker(), 1, TimeUnit.SECONDS);
    }

    public void put(Object ctx, K key, V value) {
        final DataOp dataOp = getQueueProcessingContext();
        if (dataOp == null) {
            // before queue call directed to temp storage
            tempStore.put(getScopedKey(ctx, key), new StampedData<V>(value));
        } else {
            //noinspection unchecked
            persistentStore.put(key, value);
            //noinspection unchecked
            persistentStore.put(REPLICA_SYNC_CLOCK_KEY, dataOp.getTs());
        }
    }

    public V get(Object ctx, K key) {
        final DataOp dataOp = getQueueProcessingContext();
        if (dataOp == null) {
            // this is before queue call => try temp storage first
            StampedData<V> stampedData = tempStore.get(getScopedKey(ctx, key));
            if (stampedData != null) {
                if (stampedData.getTs() > replicaSyncClock) {
                    return stampedData.getValue();
                } else {
                    tempStore.delete(getScopedKey(ctx, key));
                }
            }
        }
        //noinspection unchecked
        return (V) persistentStore.get(key);
    }

    public void delete(Object ctx, K key) {
        final DataOp dataOp = getQueueProcessingContext();
        if (dataOp == null) {
            // before queue call => store null in temp storage
            tempStore.put(getScopedKey(ctx, key), new StampedData<V>(null));
        } else {
            //noinspection unchecked
            persistentStore.delete(key);
        }
    }

    public static DataOp getQueueProcessingContext() {
        return currentDataOp.get();
    }

    public static void setQueueProcessingContext(DataOp op) {
        currentDataOp.set(op);
    }

    public static void removeQueueProcessingContext() {
        currentDataOp.remove();
    }

    public String getScopedKey(Object ctx, K key) {
        if (ctx == null) {
            return prefix + "." + key;
        }
        return prefix + "." + key + "." + ctx;
    }


    private class ReplicaSyncClockTracker implements Runnable {
        public void run() {
            @SuppressWarnings({"unchecked"})
            Object value = persistentStore.get(REPLICA_SYNC_CLOCK_KEY);
            if (value != null) {
                replicaSyncClock = ((Number) value).longValue();
            }
        }
    }
}
