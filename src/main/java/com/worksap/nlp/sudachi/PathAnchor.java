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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.logging.Logger;

/**
 * Resolves paths of {@link Settings}, when converting them to {@link Config}s.
 * When creating {@link com.worksap.nlp.sudachi.Config.Resource} objects, paths
 * will be resolved relative to an anchor, which is bound to a Settings object.
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
 * It is also possible to chain anchors using {@link #andThen(PathAnchor)}
 * method, which will resolve the first existing path.
 */
public abstract class PathAnchor {
    private static final Logger logger = Logger.getLogger(PathAnchor.class.getName());

    /**
     * Create an anchor for the root of classpath, using the classloader of the
     * Config class.
     * 
     * @return classpath anchor
     */
    public static PathAnchor classpath() {
        return classpath(Config.class.getClassLoader());
    }

    /**
     * Create an anchor for the root of the classpath, using provided classloader
     * 
     * @param loader
     *            provided classloader
     * @return classpath anchor
     */
    public static PathAnchor classpath(ClassLoader loader) {
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
    public static PathAnchor classpath(String prefix, ClassLoader loader) {
        return new Classpath(Paths.get(prefix), loader);
    }

    /**
     * Create an anchor relative to the provided class
     * 
     * @param clz
     *            provided class
     * @return classpath anchor
     */
    public static PathAnchor classpath(Class<?> clz) {
        String name = clz.getName();
        String path = name.replace(".", "/");
        return new Classpath(Paths.get(path).getParent(), clz.getClassLoader());
    }

    /**
     * Create a filesystem anchor relative to the path
     * 
     * @param path
     *            base path to resolve other paths upon
     * @return filesystem anchor
     */
    public static PathAnchor filesystem(Path path) {
        if (path == null) {
            throw new NullPointerException("passed path was null");
        }
        return new Filesystem(path);
    }

    /**
     * Create a filesystem anchor relative to the path
     * 
     * @param path
     *            base path to resolve other paths upon
     * @return filesystem anchor
     */
    public static PathAnchor filesystem(String path) {
        if (path == null) {
            throw new NullPointerException("passed path was null");
        }
        return filesystem(Paths.get(path));
    }

    /**
     * Create a filesystem anchor relative to the current directory
     * 
     * @return filesystem anchor
     */
    public static PathAnchor none() {
        return None.INSTANCE;
    }

    /**
     * Resolve a path relative to the anchor
     * 
     * @param part
     *            path suffix
     * @return full path. It may not be usable for classpath.
     */
    public Path resolve(String part) {
        return Paths.get(part);
    }

    /**
     * Check whether the path exists.
     * 
     * @param path
     *            fully resolved path
     * @return true if the path points to an existing file
     */
    public boolean exists(Path path) {
        try {
            return Files.exists(path);
        } catch (SecurityException e) {
            return false;
        }
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
    public <T> Config.Resource<T> toResource(Path path) {
        if (this.exists(path)) {
            return new Config.Resource.Filesystem<>(path);
        }
        return new Config.Resource.NotFound<>(path, this);
    }

    /**
     * Create a resource for passed string path
     * 
     * @param path
     *            path to the resource
     * @return resource, encapsulating the path
     * @param <T>
     *            type of resource
     */
    public <T> Config.Resource<T> resource(String path) {
        return toResource(resolve(path));
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
    public PathAnchor andThen(PathAnchor other) {
        if (this.equals(other)) {
            return this;
        }
        return new Chain(this, other);
    }

    static class Filesystem extends PathAnchor {
        private final Path base;

        public Filesystem(Path base) {
            this.base = base;
        }

        @Override
        public Path resolve(String part) {
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

    static class Classpath extends PathAnchor {
        private final Path prefix;
        private final ClassLoader loader;

        public Classpath(Path prefix, ClassLoader loader) {
            this.prefix = prefix;
            this.loader = loader;
        }

        @Override
        public Path resolve(String part) {
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
        public boolean exists(Path path) {
            String name = resourceName(path);
            return loader.getResource(name) != null;
        }

        @Override
        public <T> Config.Resource<T> toResource(Path path) {
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
            return "Classpath{prefix=" + prefix + '}';
        }
    }

    static class Chain extends PathAnchor {
        private final List<PathAnchor> children = new ArrayList<>();

        Chain(PathAnchor... items) {
            for (PathAnchor item : items) {
                if (item instanceof Chain) {
                    Chain c = (Chain) item;
                    c.children.forEach(this::add);
                } else {
                    add(item);
                }
            }
        }

        private void add(PathAnchor item) {
            if (!children.contains(item)) {
                children.add(item);
            }
        }

        @Override
        public Path resolve(String part) {
            Path path = null;
            for (PathAnchor p : children) {
                path = p.resolve(part);
                if (p.exists(path)) {
                    return path;
                }
                logger.fine(() -> String.format("%s: %s does not exist, skipping", p, part));
            }
            return path;
        }

        @Override
        public boolean exists(Path path) {
            return children.stream().anyMatch(p -> p.exists(path));
        }

        @Override
        public <T> Config.Resource<T> toResource(Path path) {
            for (PathAnchor child : children) {
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
            for (PathAnchor anchor : children) {
                joiner.add(anchor.toString());
            }
            return joiner.toString();
        }
    }

    private static class None extends PathAnchor {
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
