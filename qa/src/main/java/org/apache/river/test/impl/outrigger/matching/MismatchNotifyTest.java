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
package org.apache.river.test.impl.outrigger.matching;

import java.util.logging.Level;

// Test harness specific classes
import org.apache.river.qa.harness.TestException;
import org.apache.river.qa.harness.QAConfig;

// All other imports
import org.apache.river.qa.harness.Test;
import net.jini.core.lease.Lease;
import net.jini.core.entry.Entry;


/**
 * JavaSpaces test that registers for a number of events each with a
 * template that will not match the one entry that will the
 * mismatching templates are picked from the entire UniqueEntry
 * hierarchy.
 */
public class MismatchNotifyTest extends SingletonMatchTestBase {
    private NotifyTestUtil testUtil;

    /**
     * Sets up the testing environment.
     *
     * @param config Arguments from the runner for construct.
     */
    public Test construct(QAConfig config) throws Exception {
        super.construct(config);
        this.parse();
        return this;
    }

    protected void parse() throws Exception {
        super.parse();
        testUtil = new NotifyTestUtil(getConfig(), this);
    }

    public void run() throws Exception {
        testUtil.init((JavaSpaceAuditor) space);
        final Entry toWrite = getOne();

        // Register for events
	TemplateGenerator gen = new MismatchTmplGen(toWrite, classList);
	int count = 0;
	Entry tmpl;
	
	while ((tmpl = gen.next()) != null) {
	    testUtil.registerForNotify(tmpl);
	    logger.log(Level.INFO, 
		       "Notify Registration " + ++count + " " + tmpl);
	}
        spaceSet();
	addOutriggerLease(space.write(toWrite, null, Lease.ANY), false);
        final String rslt = testUtil.waitAndCheck();

        if (rslt != null) {
            throw new TestException(rslt);
        }
    }
}
