/*
 *  Copyright (c) 2017 Works Applications Co., Ltd.
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

package com.worksap.nlp.elasticsearch.sudachi.index;

import java.nio.file.Paths;

public class SudachiPathResolver {
    
    private final String esConfig;
    private final String filePath;

    public SudachiPathResolver(String esConfig, String filePath) {
        this.esConfig = esConfig;
        this.filePath = filePath;
    }
    
    public String resolvePathForFile(){
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        return resolvePath();
    }
    
    public String resolvePathForDirectory(){
        if (filePath == null) {
            return "";
        }
        if(filePath.isEmpty()){
            return Paths.get(esConfig).toString();
        }
        return resolvePath();
    }
    
    private String resolvePath(){
        if(Paths.get(filePath).isAbsolute()){
            return filePath;
        } else {
            return Paths.get(esConfig, filePath).normalize().toString();
        }
    }
}
