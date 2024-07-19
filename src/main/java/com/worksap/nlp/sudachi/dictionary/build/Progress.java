/*
 * Copyright (c) 2021 Works Applications Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.worksap.nlp.sudachi.dictionary.build;

import java.time.Duration;

/**
 * Handles progress of each build process.
 */
public class Progress {
    // minimum time delta for callback.progress call
    private final static long MS_100 = 100_000_000L; // 100ms in nanos
    // resolution of progress step.
    private final int maxUpdates;
    private final Callback callback;
    private float currentProgress;
    // records the nano time of startBlock call
    private long startTime;
    // records the nano time of last callback.progress call
    private long lastUpdate;

    /** Progress with no-operation. */
    public static final Progress NOOP = new Progress(1, progress -> {
    });

    public Progress(int maxUpdates, Callback callback) {
        this.maxUpdates = maxUpdates;
        this.callback = callback;
    }

    /**
     * declare the start of progress block
     * 
     * @param name
     *            name of this block
     * @param start
     *            nano time when the process starts
     * @param kind
     *            what kind of data will be processed.
     */
    public void startBlock(String name, long start, Kind kind) {
        startTime = start;
        lastUpdate = start;
        callback.start(name, kind);
        currentProgress = step();
    }

    private float step() {
        return 1.0f / maxUpdates - 1e-6f;
    }

    /**
     * This function limits calls to the progress function
     *
     * @param cur
     *            current state
     * @param max
     *            maximum state
     */
    public void progress(long cur, long max) {
        double ratio = cur / (double) max;
        if (ratio > currentProgress) {
            if (ratio >= 1.0) {
                callback.progress(1.0f);
                currentProgress = Float.MAX_VALUE;
            }

            long curTime = System.nanoTime();
            if (curTime - lastUpdate > MS_100) {
                callback.progress((float) ratio);
                float step = step();
                double nsteps = ratio / step;
                currentProgress += Math.floor(nsteps) * step;
                assert ratio < currentProgress;
                lastUpdate = curTime;
            }
        }
    }

    /**
     * declare the end of progress block
     * 
     * @param size
     *            actual size of processed data.
     * @param time
     *            nano time when the process ends.
     */
    public void endBlock(long size, long time) {
        callback.end(size, Duration.ofNanos(time - startTime));
    }

    /**
     * What kind of data will be processed.
     */
    public enum Kind {
        BYTE, ENTRY
    }

    /**
     * Progress callback
     */
    public interface Callback {
        /**
         * This function will be called at the beginning of each block.
         * 
         * @param name
         *            step name
         */
        default void start(String name, Kind kind) {
        }

        /**
         * This function will be called as progress is happening
         * 
         * @param progress
         *            ratio of the progress
         */
        void progress(float progress);

        /**
         * This function will be called at the end of each block
         * 
         * @param size
         * @param time
         */
        default void end(long size, Duration time) {
        }
    }
}
