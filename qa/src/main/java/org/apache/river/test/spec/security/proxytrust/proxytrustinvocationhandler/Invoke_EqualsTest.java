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
package org.apache.river.test.spec.security.proxytrust.proxytrustinvocationhandler;

// java.lang
import java.lang.reflect.Method;

// net.jini
import net.jini.core.constraint.RemoteMethodControl;
import net.jini.security.proxytrust.ProxyTrust;
import net.jini.security.proxytrust.ProxyTrustInvocationHandler;

// org.apache.river
import org.apache.river.qa.harness.TestException;
import org.apache.river.test.spec.security.proxytrust.util.AbstractTestBase;
import org.apache.river.test.spec.security.proxytrust.util.ProxyTrustUtil;
import org.apache.river.test.spec.security.proxytrust.util.Interface1Impl;
import org.apache.river.test.spec.security.proxytrust.util.Interface2Impl;
import org.apache.river.test.spec.security.proxytrust.util.Interface12Impl;
import org.apache.river.test.spec.security.proxytrust.util.Interface13Impl;
import org.apache.river.test.spec.security.proxytrust.util.Interface21Impl;
import org.apache.river.test.spec.security.proxytrust.util.TestClassLoader;

/**
 * <pre>
 * Purpose
 *   This test verifies the following:
 *     If the specified method is Object.equals, invoke method of
 *     ProxyTrustInvocationHandler returns true if the argument (args[0]) is an
 *     instance of a dynamic proxy class (that is, a class generated by Proxy)
 *     that implements the same interfaces as the specified proxy and this
 *     invocation
 *     handler is equal to the invocation handler of that argument, and returns
 *     false otherwise.
 *
 * Infrastructure
 *   This test requires the following infrastructure:
 *     Interface1Proxy - normal dynamic proxy class implementing TestInterface1
 *     Interface2Proxy - normal dynamic proxy class implementing TestInterface2
 *     Interface12Proxy - normal dynamic proxy class implementing TestInterface1
 *             and TestInterface2 interfaces
 *     Interface13Proxy - normal dynamic proxy class implementing TestInterface1
 *             and TestInterface3 interfaces
 *     Interface21Proxy - normal dynamic proxy class implementing TestInterface2
 *             and TestInterface1 interfaces (in this order)
 *     ValidMainProxy - normal main proxy
 *     ValidBootProxy - normal boot proxy
 *     InvHandler - test InvocationHandler
 *     TestClassLoader - normal class loader
 *
 * Action
 *   The test performs the following steps:
 *     1) construct ProxyTrustInvocationHandler with ValidMainProxy and
 *        ValidBootProxy as parameters
 *     2) construct Interface1Proxy instance with constructed
 *        ProxyTrustInvocationHandler
 *     3) invoke 'invoke' method of constructed ProxyTrustInvocationHandler with
 *        Interface1Proxy, 'equals' method and args[0] = the same
 *        Interface1Proxy
 *     4) assert that true will be returned
 *     5) construct Interface1Proxy instance with equal invocation handler and
 *        TestClassLoader (Interface1Proxy1)
 *     6) invoke 'invoke' method of constructed ProxyTrustInvocationHandler with
 *        Interface1Proxy, 'equals' method and args[0] = Interface1Proxy1
 *     7) assert that true will be returned
 *     8) construct Interface1Proxy instance with InvHandler invocation handler
 *        (Interface1Proxy2)
 *     9) invoke 'invoke' method of constructed ProxyTrustInvocationHandler with
 *        Interface1Proxy, 'equals' method and args[0] = Interface1Proxy2
 *     10) assert that false will be returned
 *     11) invoke 'invoke' method of constructed ProxyTrustInvocationHandler
 *         with Interface1Proxy, 'equals' method and args[0] = Interface2Proxy
 *         with the equal invocation handler
 *     12) assert that false will be returned
 *     13) construct Interface12Proxy instance with constructed
 *         ProxyTrustInvocationHandler
 *     14) invoke 'invoke' method of constructed ProxyTrustInvocationHandler
 *         with Interface1Proxy, 'equals' method and args[0] = Interface12Proxy
 *     15) assert that false will be returned
 *     16) invoke 'invoke' method of constructed ProxyTrustInvocationHandler
 *         with Interface12Proxy, 'equals' method and args[0] = the same
 *         Interface12Proxy
 *     17) assert that true will be returned
 *     18) construct Interface12Proxy instance with the equal invocation handler
 *         and TestClassLoader (Interface12Proxy1)
 *     19) invoke 'invoke' method of constructed ProxyTrustInvocationHandler
 *         with Interface12Proxy, 'equals' method and args[0] =
 *         Interface12Proxy1
 *     20) assert that true will be returned
 *     21) construct Interface12Proxy instance with InvHandler invocation
 *         handler (Interface12Proxy2)
 *     22) invoke 'invoke' method of constructed ProxyTrustInvocationHandler
 *         with Interface12Proxy, 'equals' method and args[0] =
 *         Interface12Proxy2
 *     23) assert that false will be returned
 *     24) invoke 'invoke' method of constructed ProxyTrustInvocationHandler
 *         with Interface12Proxy, 'equals' method and args[0] = Interface13Proxy
 *         with the equal invocation handler
 *     25) assert that false will be returned
 *     26) invoke 'invoke' method of constructed ProxyTrustInvocationHandler
 *         with Interface12Proxy, 'equals' method and args[0] = Interface21Proxy
 *         with the equal invocation handler
 *     27) assert that false will be returned
 * </pre>
 */
public class Invoke_EqualsTest extends AbstractTestBase {

    /**
     * This method performs all actions mentioned in class description.
     *
     */
    public void run() throws Exception {
        RemoteMethodControl main = createValidMainProxy();
        ProxyTrust boot = createValidBootProxy();
        ProxyTrustInvocationHandler ptih = createPTIH(main, boot);
        Object proxy = ProxyTrustUtil.newProxyInstance(
                new Interface1Impl(), ptih);
        Method m = Object.class.getDeclaredMethod("equals",
                new Class[] { Object.class });
        TestClassLoader cl = new TestClassLoader();
        Object[] args = new Object[] {
            proxy,
            ProxyTrustUtil.newProxyInstance(new Interface1Impl(), ptih, cl),
            ProxyTrustUtil.newProxyInstance(new Interface1Impl()),
            ProxyTrustUtil.newProxyInstance(new Interface2Impl(), ptih),
            ProxyTrustUtil.newProxyInstance(new Interface12Impl(), ptih)};
        boolean[] expRes = new boolean[] { true, true, false, false, false };
        Object res;

        for (int i = 0; i < args.length; ++i) {
            res = ptihInvoke(ptih, proxy, m, new Object[] { args[i] });

            if (!isOk(res, expRes[i])) {
                // FAIL
                throw new TestException(
                        "'invoke' method of constructed "
                        + "ProxyTrustInvocationHandler returned " + res
                        + ", while Boolean(" + expRes[i]
                        + ") was expected.");
            }
        }
        proxy = ProxyTrustUtil.newProxyInstance(new Interface12Impl(), ptih);
        args = new Object[] {
            proxy,
            ProxyTrustUtil.newProxyInstance(new Interface12Impl(), ptih, cl),
            ProxyTrustUtil.newProxyInstance(new Interface12Impl()),
            ProxyTrustUtil.newProxyInstance(new Interface13Impl(), ptih),
            ProxyTrustUtil.newProxyInstance(new Interface21Impl(), ptih) };
        expRes = new boolean[] { true, true, false, false, false };

        for (int i = 0; i < args.length; ++i) {
            res = ptihInvoke(ptih, proxy, m, new Object[] { args[i] });

            if (!isOk(res, expRes[i])) {
                // FAIL
                throw new TestException(
                        "'invoke' method of constructed "
                        + "ProxyTrustInvocationHandler returned " + res
                        + ", while Boolean(" + expRes[i]
                        + ") was expected.");
            }
        }
    }
}
