/*
 * Copyright (c) 2019 Works Applications Co., Ltd.
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

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * Mmap functions
 *
 * <p>This class provides mmap() and munmap().
 */
public class MMap {

    /**
     * Maps a file directly into memory.
     *
     * @param filename the filename to open
     * @return the mapped byte buffer
     * @throws IOException if reading a file is failed
     */
    public static ByteBuffer map(String filename) throws IOException {
        try (FileInputStream istream = new FileInputStream(filename);
             FileChannel inputFile = istream.getChannel()) {
            ByteBuffer buffer = inputFile.map(FileChannel.MapMode.READ_ONLY,
                                              0,
                                              inputFile.size());
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            return buffer;
        }
    }

    /**
     * Unmaps the region of the buffer.
     *
     * If the buffer is not a mapped file, this method do nothing.
     * @param buffer the mapped byte buffer to ummap
     */
    public static void unmap(ByteBuffer buffer) {
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
                final MethodHandle cleanerMethod =
                    lookup.findVirtual(unsafeClass, "invokeCleaner",
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
                final Class<?> directBufferClass =
                    Class.forName("java.nio.DirectByteBuffer");
                final Method m = directBufferClass.getMethod("cleaner");
                m.setAccessible(true);
                final MethodHandle directBufferCleanerMethod = lookup.unreflect(m);
                final Class<?> cleanerClass
                    = directBufferCleanerMethod.type().returnType();

                final MethodHandle cleanMethod =
                    lookup.findVirtual(cleanerClass, "clean",
                                       MethodType.methodType(void.class));
                final MethodHandle nonNullTest =
                    lookup.findStatic(Object.class, "nonNull",
                                      MethodType.methodType(boolean.class, Object.class))
                    .asType(MethodType.methodType(boolean.class, cleanerClass));
                final MethodHandle noop = MethodHandles.dropArguments(MethodHandles.constant(Void.class, null).asType(MethodType.methodType(void.class)), 0, cleanerClass);
                unmapper =
                    MethodHandles.filterReturnValue(directBufferCleanerMethod,
                                                    MethodHandles.guardWithTest(nonNullTest, cleanMethod, noop))
                    .asType(MethodType.methodType(void.class, ByteBuffer.class));
                unmapper = directBufferCleanerMethod;
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
            } catch(Throwable e) {
                e.printStackTrace();
                throw new RuntimeException("can not destroy direct buffer " + buffer, e);
            }
        }
    }
}
