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
package org.apache.river.test.spec.lookupservice.test_set02;

import org.apache.river.qa.harness.TestException;

import org.apache.river.test.spec.lookupservice.QATestRegistrar;
import net.jini.core.lookup.ServiceTemplate;

/**
 * This class is used to verify that doing a getEntryClasses with a template
 * that matches nothing results in a null return value.
 */
public class GetEntryClassesNoMatch extends QATestRegistrar {

    public void run() throws Exception {
	Class[] types = {GetEntryClassesNoMatch.class};
	Class[] vals = 
	    getProxy().getEntryClasses(new ServiceTemplate(null, types, null));
	if (vals != null) {
	    throw new TestException("getEntryClasses did not return null");
	}
    }
}
