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


package org.elasticsearch.bootstrap;

import org.elasticsearch.common.io.PathUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * jar hell checker killer for Elasticsearch 5.5.0
 *
 * Unfortunately, there seems no proper way to disable jar hell check excepting this hack.
 */
public class JarHell {
    public static void main(String[] args) throws Exception {
    }

    public static void checkJarHell() throws IOException, URISyntaxException {
    }

    public static void checkJarHell(Set<URL> urls) throws URISyntaxException, IOException {
    }

    public static void checkVersionFormat(String targetVersion) {
    }

    public static void checkJavaVersion(String resource, String targetVersion) {
    }

    public static Set<URL> parseClassPath() {
        return parseClassPath(System.getProperty("java.class.path"));
    }

    static Set<URL> parseClassPath(String classPath) {
        String pathSeparator = System.getProperty("path.separator");
        String fileSeparator = System.getProperty("file.separator");
        String[] elements = classPath.split(pathSeparator);
        Set<URL> urlElements = new LinkedHashSet<URL>();
        String[] var5 = elements;
        int var6 = elements.length;

        for (int var7 = 0; var7 < var6; ++var7) {
            String element = var5[var7];
            if (element.isEmpty()) {
                throw new IllegalStateException("Classpath should not contain empty elements! (outdated shell script from a previous version?) classpath='" + classPath + "'");
            }

            if (element.startsWith("/") && "\\".equals(fileSeparator)) {
                element = element.replace("/", "\\");
                if (element.length() >= 3 && element.charAt(2) == 58) {
                    element = element.substring(1);
                }
            }

            try {
                URL url = PathUtils.get(element, new String[0]).toUri().toURL();
                if (!urlElements.add(url)) {
                    // throw new IllegalStateException("jar hell!" + System.lineSeparator() + "duplicate jar [" + element + "] on classpath: " + classPath);
                }
            } catch (MalformedURLException var10) {
                throw new RuntimeException(var10);
            }
        }

        return Collections.unmodifiableSet(urlElements);
    }
}
