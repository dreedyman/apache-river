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

package org.apache.river.api.security;

import java.net.MalformedURLException;
import java.security.CodeSource;
import java.net.URL;
import java.security.ProtectionDomain;
import java.net.URISyntaxException;
import java.security.cert.Certificate;
import java.security.Permission;
import java.security.Principal;
import java.net.URI;
import junit.framework.Assert;
import org.apache.river.api.net.Uri;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author peter
 */
public class URIGrantTest {
    URIGrant instance;
    ProtectionDomain pd;
    public URIGrantTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() throws URISyntaxException, MalformedURLException {
        String [] u = new String[2];
        u[0] = "file:/foo/*";
        u[1] = "file:/C:/FOO/*";
        instance = new URIGrant(u, new Certificate[0], new Principal[0], new Permission[0]);
        pd = new ProtectionDomain( new CodeSource(new URL("file:/foo/bar"), (Certificate []) null), null);
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testImpliesPD() throws URISyntaxException {
        System.out.println("Test implies ProtectionDomain");
        Assert.assertTrue(instance.implies(pd));
    }
}
