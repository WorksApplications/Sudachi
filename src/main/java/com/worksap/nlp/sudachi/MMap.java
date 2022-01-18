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

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Mmap functions
 *
 * <p>
 * This class provides mmap() and munmap().
 */
public class MMap {

    private MMap() {
    }

    /**
     * Maps a file directly into memory. Will fail if the file is larger than 2GB.
     *
     * @param filename
     *            the filename to open
     * @return the mapped byte buffer
     * @throws IOException
     *             if reading a file is failed
     */
    public static ByteBuffer map(String filename) throws IOException {
        return map(Paths.get(filename));
    }

    /**
     * Maps a file into the memory fully. Will fail if the file is larger than 2GB.
     * 
     * @param path
     *            {@link Path} to the file
     * @return mapped
     * @throws IOException
     *             when IO fails
     */
    public static ByteBuffer map(Path path) throws IOException {
        try (FileChannel fc = FileChannel.open(path)) {
            long size = fc.size();
            if (size > Integer.MAX_VALUE) {
                throw new IOException("impossible to map more than 2GB");
            }
            ByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, size);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            return buffer;
        }
    }

    /**
     * Unmaps the region of the buffer.
     *
     * If the buffer is not a mapped file, this method do nothing.
     * 
     * @param buffer
     *            the mapped byte buffer to ummap
     * @throws IOException
     *             if unmapping the buffer is failed
     */
    public static void unmap(ByteBuffer buffer) throws IOException {
        if (!buffer.isDirect()) {
            return;
        }

        MethodHandle unmapper = null;
        Class<?> unmappableBufferClass = null;

        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            try {
                // for JDK 9 or later
                final Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                final MethodHandle cleanerMethod = lookup.findVirtual(unsafeClass, "invokeCleaner",
                        MethodType.methodType(void.class, ByteBuffer.class));
                final Field f = unsafeClass.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                final Object theUnsafe = f.get(null);
                unmapper = cleanerMethod.bindTo(theUnsafe);
                unmappableBufferClass = ByteBuffer.class;
            } catch (SecurityException se) {
                throw se;
            } catch (ReflectiveOperationException | RuntimeException e) {
                // for JDK 8
                final Class<?> directBufferClass = Class.forName("java.nio.DirectByteBuffer");
                final Method m = directBufferClass.getMethod("cleaner");
                m.setAccessible(true);
                final MethodHandle directBufferCleanerMethod = lookup.unreflect(m);
                final Class<?> cleanerClass = directBufferCleanerMethod.type().returnType();

                final MethodHandle cleanMethod = lookup.findVirtual(cleanerClass, "clean",
                        MethodType.methodType(void.class));
                final MethodHandle nonNullTest = lookup
                        .findStatic(Objects.class, "nonNull", MethodType.methodType(boolean.class, Object.class))
                        .asType(MethodType.methodType(boolean.class, cleanerClass));
                final MethodHandle noop = MethodHandles.dropArguments(
                        MethodHandles.constant(Void.class, null).asType(MethodType.methodType(void.class)), 0,
                        cleanerClass);

                unmapper = MethodHandles
                        .filterReturnValue(directBufferCleanerMethod,
                                MethodHandles.guardWithTest(nonNullTest, cleanMethod, noop))
                        .asType(MethodType.methodType(void.class, ByteBuffer.class));
                unmappableBufferClass = directBufferClass;
            }
        } catch (SecurityException se) {
            throw se;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            unmapper = null;
        }

        if (unmapper != null && unmappableBufferClass.isInstance(buffer)) {
            try {
                unmapper.invokeExact(buffer);
            } catch (Throwable e) {
                throw new IOException("can not destroy direct buffer " + buffer, e);
            }
        }
    }
}
