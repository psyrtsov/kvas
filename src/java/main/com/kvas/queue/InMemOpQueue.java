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

package com.kvas.queue;

import java.util.Queue;

/**
 * Date: Mar 16, 2010
 * Time: 4:00:02 PM
 */
public class InMemOpQueue extends OpQueue {
    private Queue<DataOp> queue;

    public InMemOpQueue(Queue<DataOp> queue) {
        this.queue = queue;
    }

    protected void enqueue(DataOp op) {
        queue.add(op);
    }
}
