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

package com.kvas.processor;

import com.kvas.StoreRouter;
import com.kvas.queue.DataOp;
import com.kvas.queue.InMemOpQueue;
import com.kvas.queue.OpQueue;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Date: Mar 16, 2010
 * Time: 4:03:45 PM
 */
public class DataOpProcessor implements Runnable {
    public static final long TS_PADDING = 1000L;
    private final Queue<DataOp> queue;

    public DataOpProcessor(Queue<DataOp> queue) {
        this.queue = queue;
    }

    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            processOneItem();
        }
    }

    public void processOneItem() {
        final DataOp op = queue.poll();
        try {
            StoreRouter.setQueueProcessingContext(op);
            op.apply();
        } finally {
            StoreRouter.removeQueueProcessingContext();
        }
    }

    public static OpQueue start() {
        Queue<DataOp> queue = new ArrayBlockingQueue<DataOp>(1000);
        DataOpProcessor processor = new DataOpProcessor(queue);
        Thread thread = new Thread(processor, DataOpProcessor.class.getSimpleName());
        thread.setDaemon(true);
        thread.run();
        return new InMemOpQueue(queue);
    }
}
