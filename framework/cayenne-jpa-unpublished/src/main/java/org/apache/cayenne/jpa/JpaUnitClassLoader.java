/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/

package org.apache.cayenne.jpa;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureClassLoader;

/**
 * A special class loader that uses parent loader to get resources, but does not load any
 * classes though the parent to avoid parent pollution with un-enhanced classes.
 * 
 */
class JpaUnitClassLoader extends SecureClassLoader {

    JpaUnitClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {

        if (name.startsWith("java.") || name.startsWith("javax.")) {
            return super.loadClass(name, resolve);
        }

        Class<?> c = findLoadedClass(name);

        if (c == null) {
            c = findClass(name);
        }

        if (resolve) {
            resolveClass(c);
        }

        return c;
    }

    /**
     * Loads a class bypassing parent class loader.
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String path = name.replace('.', '/') + ".class";

        InputStream in = getResourceAsStream(path);
        if (in == null) {
            throw new ClassNotFoundException(name);
        }

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];
            int read;

            while ((read = in.read(buffer, 0, 1024)) > 0) {
                out.write(buffer, 0, read);
            }

            out.close();
            byte[] classBytes = out.toByteArray();

            return defineClass(name, classBytes, 0, classBytes.length);
        }
        catch (SecurityException e) {
            // JVM doesn't allow us to define class here... Is there a way to detect this
            // upfront?
            return super.findClass(name);
        }
        catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
        finally {
            try {
                in.close();
            }
            catch (IOException e) {
                // ignore close exceptions...
            }
        }
    }
}
