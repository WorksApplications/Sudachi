/*
 * Copyright (c) 2022 Works Applications Co., Ltd.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;
import java.util.function.Supplier;

public class DicBuilder2 {
    private DicBuilder2() {
        // no instances
    }

    public static class Base<T extends Base<T>> {
        protected final POSTable pos = new POSTable();
        protected final ConnectionMatrix connection = new ConnectionMatrix();
        protected final Index index = new Index();
        protected Progress progress = Progress.NOOP;

        @SuppressWarnings("unchecked")
        private T self() {
            return (T) this;
        }

        public T lexicon(String name, Supplier<InputStream> input, long size) throws IOException {

            return self();
        }
    }

}
