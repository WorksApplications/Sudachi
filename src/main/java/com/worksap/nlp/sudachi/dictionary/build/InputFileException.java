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

public class InputFileException extends IllegalArgumentException {
    /** Exception with line number */
    public InputFileException(int line, Exception cause) {
        super(String.format("[line:%d]", line), cause);
    }

    /** Exception with file name and line number */
    public InputFileException(String file, int line, Exception cause) {
        super(String.format("[%s line:%d]", file, line), cause);
    }

    /** Exception with file name, line number and csv column name */
    public InputFileException(String file, int line, String column, Exception cause) {
        super(String.format("[%s line:%d, column: %s]", file, line, column), cause);
    }
}
