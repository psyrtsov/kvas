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

import java.io.Serializable;

/**
 *
 */
public class StampedData<V> implements Serializable {
    public static final long serialVersionID = -1L;
    private final long ts;
    private final V value;

    public StampedData(V value) {
        this.value = value;
        ts = System.currentTimeMillis();
    }

    public long getTs() {
        return ts;
    }

    public V getValue() {
        return value;
    }
}
