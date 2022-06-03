/*
 * Copyright (c) 2017-2022 Works Applications Co., Ltd.
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

package com.worksap.nlp.sudachi;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

/**
 * Resolves paths of {@link Settings}, when converting them to {@link Config}s.
 * When creating {@link com.worksap.nlp.sudachi.Config.Resource} objects, paths
 * will be resolved relative to an anchor, which is bound to Settings.
 *
 * <p>
 * There are three types of anchors:
 * <ul>
 * <li>{@link None} which will resolve paths as filesystem, relative to the
 * CWD</li>
 * <li>{@link Filesystem} which will resolve paths relative to a provided
 * directory</li>
 * <li>{@link Classpath} which will resolve classpath resources</li>
 * </ul>
 * Use static methods for their creation.
 *
 * <p>
 * It is also possible to chain anchors using {@link #andThen(SettingsAnchor)}
 * method, which will resolve the first existing path.
 */
public abstract class SettingsAnchor {
    private static final Logger logger = Logger.getLogger(SettingsAnchor.class.getName());

    /**
     * Create an anchor for the root of classpath, using Settings classloader.
     * 
     * @return classpath anchor
     */
    public static SettingsAnchor classpath() {
        return classpath(Settings.class.getClassLoader());
    }

    /**
     * Create an anchor for the root of the classpath, using provided classloader
     * 
     * @param loader
     *            provided classloader
     * @return classpath anchor
     */
    public static SettingsAnchor classpath(ClassLoader loader) {
        return classpath("", loader);
    }

    /**
     * Create an anchor for the provided prefix
     * 
     * @param prefix
     *            in the classpath, should be a path
     * @param loader
     *            provided classloader
     * @return classpath anchor
     */
    public static SettingsAnchor classpath(String prefix, ClassLoader loader) {
        return new Classpath(Paths.get(prefix), loader);
    }

    /**
     * Create an anchor relative to the provided class
     * 
     * @param clz
     *            provided class
     * @return classpath anchor
     */
    public static SettingsAnchor classpath(Class<?> clz) {
        String name = clz.getName();
        String path = name.replace(".", "/");
        return new Classpath(Paths.get(path).getParent(), clz.getClassLoader());
    }

    /**
     * Create a filesystem anchor relative to the path
     * 
     * @param path
     *            path
     * @return filesystem anchor
     */
    public static SettingsAnchor filesystem(Path path) {
        if (path == null) {
            throw new NullPointerException("passed path was null");
        }
        return new Filesystem(path);
    }

    /**
     * Create a filesystem anchor relative to the current directory
     * 
     * @return filesystem anchor
     */
    public static SettingsAnchor none() {
        return None.INSTANCE;
    }

    /**
     * Resolve a path relative to the anchor
     * 
     * @param part
     *            path suffix
     * @return full path. It may not be usable for classpath.
     */
    Path resolve(String part) {
        return Paths.get(part);
    }

    /**
     * Check whether the path exists.
     * 
     * @param path
     *            fully resolved path
     * @return true if the path points to an existing file
     */
    boolean exists(Path path) {
        return Files.exists(path);
    }

    /**
     * Create a resource for the fully resolved path
     * 
     * @param path
     *            fully resolved path
     * @param <T>
     *            type of the resource
     * @return resource, encapsulating path, works both for filesystem and classpath
     */
    <T> Config.Resource<T> toResource(Path path) {
        if (Files.exists(path)) {
            return new Config.Resource.Filesystem<>(path);
        }
        return new Config.Resource.NotFound<>(path, this);
    }

    /**
     * Chain another anchor after the current one. Path will be resolved for the
     * first anchor, if the pointed file does not exist, path will be resolved by
     * the second anchor.
     * 
     * @param other
     *            another anchor
     * @return chained anchor
     */
    public SettingsAnchor andThen(SettingsAnchor other) {
        if (this.equals(other)) {
            return this;
        }
        return new Chain(this, other);
    }

    static class Filesystem extends SettingsAnchor {
        private final Path base;

        public Filesystem(Path base) {
            this.base = base;
        }

        @Override
        Path resolve(String part) {
            Path resolved = base.resolve(part);
            logger.fine(() -> String.format("%s resolved %s to %s", this, part, resolved));
            return resolved;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Filesystem) {
                Filesystem o = (Filesystem) obj;
                return base.equals(o.base);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(base);
        }

        @Override
        public String toString() {
            return "Filesystem{" + "base=" + base + '}';
        }
    }

    static class Classpath extends SettingsAnchor {
        private final Path prefix;
        private final ClassLoader loader;

        public Classpath(Path prefix, ClassLoader loader) {
            this.prefix = prefix;
            this.loader = loader;
        }

        @Override
        Path resolve(String part) {
            Path resolved = prefix.resolve(part);
            logger.fine(() -> String.format("%s resolved %s to %s", this, part, resolved));
            return resolved;
        }

        private static String resourceName(Path path) {
            String strPath = path.toString();
            // Windows hack. Can override Filesystem, but that will be much more code
            if ("\\".equals(path.getFileSystem().getSeparator())) {
                strPath = strPath.replace("\\", "/");
            }
            return strPath;
        }

        @Override
        boolean exists(Path path) {
            String name = resourceName(path);
            return loader.getResource(name) != null;
        }

        @Override
        <T> Config.Resource<T> toResource(Path path) {
            URL resource = loader.getResource(resourceName(path));
            if (resource == null) {
                return new Config.Resource.NotFound<>(path, this);
            }
            return new Config.Resource.Classpath<>(resource);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Classpath) {
                Classpath c = (Classpath) obj;
                return prefix.equals(c.prefix) && loader.equals(c.loader);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(prefix, loader);
        }

        @Override
        public String toString() {
            return "Classpath{" + "prefix=" + prefix + '}';
        }
    }

    static class Chain extends SettingsAnchor {
        private final List<SettingsAnchor> children = new ArrayList<>();

        Chain(SettingsAnchor... items) {
            for (SettingsAnchor item : items) {
                if (item instanceof Chain) {
                    Chain c = (Chain) item;
                    c.children.forEach(this::add);
                } else {
                    add(item);
                }
            }
        }

        private void add(SettingsAnchor item) {
            if (!children.contains(item)) {
                children.add(item);
            }
        }

        @Override
        Path resolve(String part) {
            Path path = null;
            for (SettingsAnchor p : children) {
                path = p.resolve(part);
                if (p.exists(path)) {
                    return path;
                }
                logger.fine(() -> String.format("%s: %s does not exist, skipping", p, part));
            }
            return path;
        }

        @Override
        boolean exists(Path path) {
            return children.stream().anyMatch(p -> p.exists(path));
        }

        @Override
        <T> Config.Resource<T> toResource(Path path) {
            for (SettingsAnchor child : children) {
                if (child.exists(path)) {
                    return child.toResource(path);
                }
            }

            return new Config.Resource.NotFound<>(path, this);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Chain) {
                Chain c = (Chain) obj;
                return children.equals(c.children);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(children);
        }

        int count() {
            return children.size();
        }

        @Override
        public String toString() {
            StringJoiner joiner = new StringJoiner(", ", "[", "]");
            for (SettingsAnchor anchor : children) {
                joiner.add(anchor.toString());
            }
            return joiner.toString();
        }
    }

    private static class None extends SettingsAnchor {
        private None() {
        }

        private static final None INSTANCE = new None();

        @Override
        public String toString() {
            return "None{}";
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof None;
        }

        @Override
        public int hashCode() {
            return 1;
        }
    }
}
