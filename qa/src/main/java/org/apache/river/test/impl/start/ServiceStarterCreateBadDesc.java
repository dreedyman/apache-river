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

import java.util.logging.Level;

import org.apache.river.qa.harness.TestException;
import org.apache.river.start.ServiceStarter;
import org.apache.river.start.ServiceDescriptor;
import net.jini.config.Configuration;

import java.util.Arrays;

public class ServiceStarterCreateBadDesc extends ServiceStarterCreateBaseTest {

    public static class MyBogusServiceDescriptor 
	implements ServiceDescriptor
    {
         public Object create(Configuration config) throws Exception {
	     return null;
	 }
    }

    public static class MyBogusServiceDescriptor2 
	implements ServiceDescriptor 
    {
         public Object create(Configuration config) throws Exception {
	     throw new Exception("MyBogusServiceDescriptor2:create()");
	 }
    }

    public void run() throws Exception {
	String[] keys = new String[] {"service.creation.null",
            "service.creation.unknown"};

	String[] args = new String[] { 
		getConfig().getStringConfigVal("test.bogusConfig", null) 
	};
	if ( args[0] == null)  {
            throw new TestException("Configuration file entry not found: "
                    + args[0]);
	}
	logger.log(Level.INFO, "bogusConfig => " + args[0]);
	ServiceStarter.main(args);
	if (!checkReport(Arrays.asList(keys), handler.getKeys())) {
            throw new TestException("Required keys not generated.");
        }
        return;
    }
}

