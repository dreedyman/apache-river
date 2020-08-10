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
package org.apache.river.test.impl.start;

import org.apache.river.qa.harness.Test;
import org.apache.river.start.ServiceStarter;
import org.apache.river.start.ServiceDescriptor;

import net.jini.config.Configuration;

import java.lang.reflect.Method;
import javax.security.auth.login.LoginContext;

public abstract class ServiceStarterCreateBaseTest extends StarterBase implements Test {
    private static final Class[] createArgs = 
	new Class[] { ServiceDescriptor[].class, Configuration.class };
    private static Method create;
    static {
	try {
	    create = 
		ServiceStarter.class.getDeclaredMethod("create", createArgs);
	    create.setAccessible(true);
	} catch (Exception e) {
	    throw new RuntimeException("Exception getting create method", e);
	}
    }
    private static final Class[] createWithLoginArgs = 
	new Class[] { ServiceDescriptor[].class, Configuration.class,
		      LoginContext.class};
    private static Method createWithLogin;
    static {
	try {
	    createWithLogin = 
		ServiceStarter.class.getDeclaredMethod(
		    "createWithLogin", createWithLoginArgs);
	    createWithLogin.setAccessible(true);
	} catch (Exception e) {
	    throw new RuntimeException("Exception getting createWithLogin method", e);
	}
    }
    protected ServiceStarterCreateBaseTest() { }

    static protected Method getCreateMethod() { return create; }
    static protected Method getCreateWithLoginMethod() { 
	return createWithLogin; 
    }
}

