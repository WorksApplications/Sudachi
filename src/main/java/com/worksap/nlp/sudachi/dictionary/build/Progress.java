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

public class Progress {
    private final static long MS_100 = 100_000_000L; // 100ms in nanos
    private final int maxUpdates;
    private final Callback callback;
    private float currentProgress;
    private long lastUpdate;

    public Progress(int maxUpdates, Callback callback) {
        this.maxUpdates = maxUpdates;
        this.callback = callback;
    }

    public void startBlock(String name, long start, Kind kind) {
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

    public void endBlock(long size, long time) {
        callback.end(size, Duration.ofNanos(time));
    }

    public enum Kind {
        INPUT, OUTPUT
    }

    /**
     * Progress callback
     */
    public interface Callback {
        /**
         * This function will be called for each step at the beginning
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

        default void end(long size, Duration time) {
        }
    }
}
