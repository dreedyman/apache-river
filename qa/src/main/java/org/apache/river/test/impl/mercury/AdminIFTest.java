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
package org.apache.river.test.impl.mercury;

import java.util.logging.Level;

// Test harness specific classes

import org.apache.river.qa.harness.TestException;
import org.apache.river.qa.harness.QAConfig;

import org.apache.river.admin.DestroyAdmin;
import org.apache.river.qa.harness.Test;

import net.jini.admin.JoinAdmin;
import net.jini.core.entry.Entry;
import net.jini.core.discovery.LookupLocator;
import net.jini.event.EventMailbox;
import net.jini.lookup.entry.Name;


public class AdminIFTest extends AdminIFBase {

    public void run() throws Exception {
	logger.log(Level.INFO, "Starting up " + this.getClass().toString()); 

	EventMailbox mb = getConfiguredMailbox();        
	Object admin = getMailboxAdmin(mb);

	//////////////////////
	// JoinAdmin Methods
	//////////////////////

	//
	// Attributes
	//

	// Uncomment the following in order to delay test progress.
	// This allows you to obtain the mailbox's PID, for example
	// before it gets destroyed at the end of this test.

	//waitOnInput(); // wait

	logger.log(Level.INFO, "\tCalling JoinAdmin methods");
	JoinAdmin ja = (JoinAdmin)admin;
	Entry[] attrs = ja.getLookupAttributes();
	logger.log(Level.INFO, "Calling JoinAdmin::getLookupAttributes: got " + 
	    attrs.length + " items");
	String name = "Spanguini";
	Entry[] newAttrs = { new Name(name) };
	logger.log(Level.INFO, "Calling JoinAdmin::addLookupAttributes()");
	ja.addLookupAttributes(newAttrs);
	logger.log(Level.INFO, "Checking addLookupAttributes call via get call");
	attrs = ja.getLookupAttributes();
	if (!assertContainsName(attrs, name)) {
	    throw new TestException(
		"Did not receive proper attribute setting for Name " +
		"after adding");
	}
	name = name + "2";
	Entry[] templates = { new Name() };
	newAttrs[0] = new Name(name);
	logger.log(Level.INFO, "Calling JoinAdmin::modifyLookupAttributes()");
	ja.modifyLookupAttributes(templates, newAttrs);
	attrs = ja.getLookupAttributes();
	if (!assertContainsName(attrs, name)) {
	    throw new TestException("Did not receive proper attribute setting for Name after modifying");
	}
	// double check assert mechanism
	if (assertContainsName(attrs, name + "2")) {
	    throw new TestException("assertContainsName returned true for a bogus value");
	}
	//
	// LookupGroups
	//
	String[] luGroups = null;
	/*
	 * TestBase utilities automatically create and join the service(s)
	 * under test to at one (unique) group. So, this section of of code 
	 * is no longer valid.
	luGroups = ja.getLookupGroups(); 
	logger.log(Level.INFO, "Calling JoinAdmin::getLookupGroups got " + 
	    luGroups.length + " items");
	dumpGroups(luGroups);
	if (!assertLookupGroups(luGroups, LookupDiscovery.NO_GROUPS))
	    throw new TestException(
		"getLookupGroups did not return NO_GROUPS upon startup");
	 */

	String[] groups = { "group1", "group2", "group3" };
	logger.log(Level.INFO, "Calling JoinAdmin::addLookupGroups()");
	dumpGroups(groups);
	ja.addLookupGroups(groups);
	logger.log(Level.INFO, "Verifying group set: ");
	luGroups = ja.getLookupGroups(); 
	dumpGroups(luGroups);
	if (luGroups.length < groups.length) 
	    throw new TestException(
		"Invalid length for returned group set after add");
	if (!assertLookupGroups(luGroups, groups))
	    throw new TestException(
		"getLookupGroups did not contain added group set");

	logger.log(Level.INFO, "Calling JoinAdmin::removeLookupGroups()");
	dumpGroups(groups);
	ja.removeLookupGroups(groups);
	logger.log(Level.INFO, "Verifying returned groups: ");
	luGroups = ja.getLookupGroups(); 
	dumpGroups(luGroups);
	if (assertLookupGroups(luGroups, groups))
	    throw new TestException("getLookupGroups contained removed group ");

	logger.log(Level.INFO, "Calling JoinAdmin::setLookupGroups()");
	dumpGroups(groups);
	ja.setLookupGroups(groups);
	logger.log(Level.INFO, "Verifying group set: ");
	luGroups = ja.getLookupGroups(); 
	dumpGroups(luGroups);
	if (groups.length != luGroups.length) 
	    throw new TestException("Invalid length for returned group set after set");
	if (!assertLookupGroups(luGroups, groups))
	    throw new TestException("getLookupGroups did not contain required group set");
	// double check assert mechanism
	String[] bogus = { groups[0], "bogusGroup", groups[1] };
	if (assertLookupGroups(luGroups, bogus))
	    throw new TestException("assertLookupGroups returned true for bogus data");
	/*
	 * The following test causes cross-talk between concurrently
	 * running test suite -- resulting in spurrious failures.
	 * Setting grops=ALL causes the service to register with 
	 * other LUS w/i multicast radius. Other tests can potentially
	 * get references to the service, which goes away shortly
	 * thereafter.
	logger.log(Level.INFO, "Calling JoinAdmin::setLookupGroups(ALL_GROUPS) ");
	ja.setLookupGroups(LookupDiscovery.ALL_GROUPS);
	logger.log(Level.INFO, "Verifying group set: ");
	luGroups = ja.getLookupGroups(); 
	dumpGroups(luGroups);
	if (!assertLookupGroups(luGroups, LookupDiscovery.ALL_GROUPS))
	    throw new TestException("getLookupGroups did not contain ALL_GROUPS after set");
	 */

	//
	// Locators
	//
	logger.log(Level.INFO, "Calling JoinAdmin::getLookupLocators ");
	if (!assertLocators(ja.getLookupLocators(), new LookupLocator[0]))
	    throw new TestException("Did not receive empty set of locators upon startup");
	logger.log(Level.INFO, "Calling JoinAdmin::addLookupLocators()");
	LookupLocator[] locators = {
	    QAConfig.getConstrainedLocator("jini://resendes:8080/"),
	    QAConfig.getConstrainedLocator("jini://resendes:8081/"),
	};
	ja.addLookupLocators(locators);
	logger.log(Level.INFO, "Verifying JoinAdmin::addLookupLocators()");
	if (!assertLocators(ja.getLookupLocators(), locators))
	    throw new TestException("Did not receive expected set of locators after add");
	logger.log(Level.INFO, "Calling JoinAdmin::removeLookupLocators()");
	ja.removeLookupLocators(locators);
	if (!assertLocators(ja.getLookupLocators(), new LookupLocator[0]))
	    throw new TestException("Did not receive empty set of locators after remove");
	logger.log(Level.INFO, "Calling JoinAdmin::setLookupLocators()");
	ja.setLookupLocators(locators);
	if (!assertLocators(ja.getLookupLocators(), locators))
	    throw new TestException("Did not receive expected set of locators after set");
	// double check assert mechanism
	LookupLocator[] bogusLoc = {
	    locators[1],
	    QAConfig.getConstrainedLocator("jini://bogus:8080/"),
	    locators[0],
	};
	if (assertLocators(ja.getLookupLocators(), bogusLoc))
	    throw new TestException("assertLocators returned true for bogus values");


	/////////////////////////
	// DestroyAdmin Methods
	/////////////////////////
	logger.log(Level.INFO, "\tCalling DestroyAdmin methods");
	DestroyAdmin da = (DestroyAdmin)admin;
	logger.log(Level.INFO, "Calling DestroyAdmin::destroy()");
	da.destroy();

	/* Delay for a bit before returning.  The destroy call
	 * starts a "destroy" thread on the mailbox process. 
	 * One part of this clean up process
	 * is to cancel any registration leases with the lookup service.
	 * Since the lookup service is killed upon returning
	 * from this function, we can sometimes get nasty 
	 * activation error messages like "NoSuchObject".
	 */
	Thread.sleep(10000);
    }

    /**
     * Invoke parent's construct and parser
     * @exception TestException will usually indicate an "unresolved"
     *  condition because at this point the test has not yet begun.
     */
    public Test construct(QAConfig sysConfig) throws Exception {
	super.construct(sysConfig);
	parse();
        return this;
    }
}
