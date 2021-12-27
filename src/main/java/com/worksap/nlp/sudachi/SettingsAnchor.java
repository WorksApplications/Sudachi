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

package com.worksap.nlp.sudachi;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public abstract class SettingsAnchor {
    private static final Logger logger = Logger.getLogger(SettingsAnchor.class.getName());

    public static SettingsAnchor classpath() {
        return new Classpath(Paths.get(""), Settings.class.getClassLoader());
    }

    public static SettingsAnchor classpath(Class<?> clz) {
        String name = clz.getName();
        String path = name.replace(".", "/");
        return new Classpath(Paths.get(path).getParent(), clz.getClassLoader());
    }

    public static SettingsAnchor filesystem(Path p) {
        return new Filesystem(p);
    }

    public static SettingsAnchor none() {
        return None.INSTANCE;
    }

    Path resolve(String part) {
        return Paths.get(part);
    }

    boolean exists(Path path) {
        return Files.exists(path);
    }

    <T> Config.Resource<T> toResource(Path path) {
        return new Config.Resource.Filesystem<>(path);
    }

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
            return new Config.Resource.Classpath<>(loader.getResource(resourceName(path)));
        }

        @Override
        public SettingsAnchor andThen(SettingsAnchor other) {
            if (other instanceof Classpath) {
                Classpath o = (Classpath) other;
                if (Objects.equals(o.prefix, prefix)) {
                    return this;
                }
            }
            return super.andThen(other);
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
                    children.forEach(this::add);
                    children.addAll(c.children);
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
            Optional<SettingsAnchor> first = children.stream().filter(p -> p.exists(path)).findFirst();
            return first.<Config.Resource<T>>map(resolver -> resolver.toResource(path)).orElse(null);
        }

        @Override
        public SettingsAnchor andThen(SettingsAnchor other) {
            if (equals(other)) {
                return this;
            }
            children.forEach(this::add);
            return this;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Chain) {
                Chain c = (Chain) obj;
                return children.equals(c.children);
            }
            return false;
        }

        int count() {
            return children.size();
        }
    }

    private static class None extends SettingsAnchor {
        private None() {
        }

        @Override
        public SettingsAnchor andThen(SettingsAnchor other) {
            if (other instanceof None) {
                return this;
            }
            return super.andThen(other);
        }

        static final None INSTANCE = new None();

        @Override
        public String toString() {
            return "None{}";
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof None;
        }
    }
}
