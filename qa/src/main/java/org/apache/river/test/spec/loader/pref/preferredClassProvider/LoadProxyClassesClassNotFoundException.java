/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.river.test.spec.loader.pref.preferredClassProvider;

// org.apache.river.qa.harness
import org.apache.river.qa.harness.TestException;

// org.apache.river.qa

// java.io

// java.net
        import java.net.MalformedURLException;

// java.rmi.server

// java.lang.reflect

// java.util.logging
        import java.util.logging.Level;

// davis packages

// instrumented preferred class loader
        import org.apache.river.test.spec.loader.util.Util;
import org.apache.river.test.spec.loader.util.QATestPreferredClassProvider;

// test base class
import org.apache.river.test.spec.loader.pref.AbstractTestBase;
import net.jini.loader.ClassLoading;


/**
 * <b>Purpose</b><br><br>
 *
 * This test verifies the behavior of the<br>
 * <code>public Class loadProxyClass(String codebase, String[] interfaces,
 *                                   ClassLoader defaultLoader)
 *    throws MalformedURLException, ClassNotFoundException</code>
 *    method of
 * method of the<br>
 * <code>net.jini.loader.pref.PreferredClassProvider</code>
 * class:
 *
 * <br><blockquote>
 * Define and return a dynamic proxy class in a class loader
 * with URLs supplied in the given location.  The proxy class
 * will implement interface classes named by the given array
 * of interface names.
 * <br><br>
 * </blockquote>
 *  <ul><lh>Parameters:</lh>
 *    <li>codebase - the list of URLs (space-separated) to load
 *                   classes from, or <code>null</code></li>
 *    <li>interfaces - the names of the interfaces for the proxy class
 *                     to implement</li>
 *    <li>defaultLoader - additional contextual class loader to use,
 *                         or <code>null</code></li>
 *  </ul>
 *  <ul><lh>throws:</lh>
 *    <li>ClassNotFoundException - if a definition for one of
 *         the named interfaces could not be found at the specified location,
 *         or if creation of the dynamic proxy class failed (such as if
 *         {@link java.lang.reflect.Proxy#getProxyClass Proxy.getProxyClass}
 *         would throw an <code>IllegalArgumentException</code> for the given
 *         interface list)</li>
 *  </ul>
 *
 * <b>Test Description</b><br><br>
 *
 *  This test uses {@link QATestPreferredClassProvider} that is created
 *  passing various parameters to parent PreferredClassProvider constructor.
 *  <br><br>
 *
 *  {@link QATestPreferredClassProvider} should be configured as
 *  java.rmi.server.RMIClassLoaderSpi, so that RMIClassLoader.loadProxyClass
 *  calls QATestPreferredClassProvider.loadProxyClass method.
 *  <br><br>
 *
 *  This test should be run with appropriate parameters.
 *  <br>
 *  All parameters should be set via config (properties) file.
 *  <ul><lh>Possible parameters are:</lh>
 *  <li>boolean isHttp: if <code>true</code> then will download classes via
 *                      http, otherwise will download classes via file based
 *                      url</li>
 *  <li>int httpPort: port do download classes via http</li>
 *  <li>boolean requireDlPerm: <code>false</code>, <code>true</code></li>
 *  </ul>
 *
 *  If requireDlPerm is equal to true then this test should be run
 *  with policy.loader policy file, otherwise with policy.loaderNoDlPerm file.
 *  <br>
 *  policy.loader policy file grants needed download permissions.
 *  <br>
 *  policy.loaderNoDlPerm file does not grant needed download permissions.
 *  <br><br>
 *
 *  This test iterates over a set of interfaces.
 *  The set of interfaces are placed in the qa1-loader-pref.jar file and
 *  can be downloaded using http or file based url.
 *  <br><br>
 *  Class {@link Util} has a statically defined lists of all resources
 *  placed in the qa1-loader-pref.jar file. {@link Util#listInterfaces},
 *  define names of these interfaces.
 *  <br><br>
 *
 *  Ten times this test will call {@link Util#getRandomInterface} and try to
 *  execute RMIClassLoader.loadProxyClass passing:
 *  <ul>
 *   <li>codebase - string representation of url to qa1-loader-pref.jar
 *                  file</li>
 *   <li>interfaces - string array of non-existent interface name (so that
 *                    this array has one element)</li>
 *   <li>defaultLoader - ClassLoader.getSystemClassLoader()</li>
 *  </ul>
 *  <br><br>
 *
 *  and verify that ClassNotFoundException is thrown.
 *  <br><br>
 *
 * <b>Infrastructure</b><br><br>
 *
 * <ol><lh>This test requires the following infrastructure:</lh>
 *  <li> {@link QATestPreferredClassProvider} is an instrumented
 *       PreferredClassProvider using for davis.loader's and davis.loader.pref's
 *       testing.</li>
 *  <li> A set of interfaces should be placed in the qa1-loader-pref.jar,
 *       so these interfaces can be downloaded.</li>
 * </ol>
 *
 * <br>
 *
 * <b>Actions</b><br><br>
 *
 * <ol>
 *  <li>
 *   run test passing {@link QATestPreferredClassProvider} as
 *   java.rmi.server.RMIClassLoaderSpi
 *  </li>
 *  <li>
 *   ten times do the following:
 *   <ol>
 *    <li>get name of non-existent interface via
 *        {@link Util#getRandomInterface}</li>
 *    <li>invoke RMIClassLoader.loadProxyClass method passing:</li>
 *     <ul>
 *      <li>codebase - string representation of url to qa1-loader-pref.jar</li>
 *      <li>interfaces - string array of inon-existent interface name (so that
 *                       this array has one element)</li>
 *      <li>defaultLoader - ClassLoader.getSystemClassLoader()</li>
 *     </ul>
 *    <li>verify that ClassNotFoundException is thrown</li>
 *   </ol>
 *  </li>
 * </ol>
 *
 */
public class LoadProxyClassesClassNotFoundException extends AbstractTestBase {

    /**
     *
     * run test passing {@link QATestPreferredClassProvider} as
     *     java.rmi.server.RMIClassLoaderSpi
     */
    public void run() throws Exception {
        String message = "";
        ClassLoader parent = Util.systemClassLoader();
        String cb = null;

        /*
         * Get codebase to qa1-loader-pref.jar.
         */
        cb = Util.getUrlAddr(isHttp, config, port) + Util.PREFERREDJarFile;

        /*
         * 2) ten times do the following:
         * - get name of non-existent interface via
         *   Util.getRandomInterface()
         * - invoke ClassLoading.loadProxyClass method passing:
         *   codebase - string representation of url to qa1-loader-pref.jar
         *              file
         *   interfaces - string array of non-existent interface name (so
         *                that this array has one element)
         *   defaultLoader - ClassLoader.getSystemClassLoader()
         * - verify that ClassNotFoundException is thrown
         */
        for (int item = 0; item < 10; item++) {
            String name = Util.getRandomInterface();
            Class classLoaded = null;
            String[] in = { name };

            try {
                classLoaded = ClassLoading.loadProxyClass(cb, in, parent, false, null);
            } catch (ClassNotFoundException e) {
                // Expect ClassNotFoundException
                String msg = ""
                           + "\nClassLoading.loadProxyClass(" + cb + ", "
                           + in[0] + ", defaultLoader, false, null)\n"
                           + "  throws " + e.toString() + " as expected";
                logger.log(Level.FINE, msg);
                continue;
            } catch (MalformedURLException me) {
                // Do not expect MalformedURLException.
                // Tests case with expected MalformedURLException
                // is LoadProxyClassesMalformedURLException
                message += "\nClassLoading.loadProxyClass(" + cb + ", "
                         + in[0] + ", defaultLoader, false, null)\n"
                         + "  throws: " + me.toString() + "\n"
                         + "  expected: ClassNotFoundException";
                break;
            }
            message += "\nClassLoader.loadProxyClass(" + cb + ", "
                     + in[0] + ", defaultLoader, false, null)\n"
                     + "  returns: " + classLoaded.toString() + "\n"
                     + "  expected: ClassNotFoundException";
            break;
        }

        if (message.length() > 0) {
            throw new TestException(message);
        }
    }
}
