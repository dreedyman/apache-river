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
import org.apache.river.qa.harness.QAConfig;
import org.apache.river.qa.harness.TestException;

import java.util.Date;

import org.apache.river.constants.TimeConstants;
import org.apache.river.qa.harness.Test;

import net.jini.event.EventMailbox;
import net.jini.event.MailboxRegistration;
import net.jini.event.MailboxPullRegistration;
import net.jini.event.PullEventMailbox;
import net.jini.core.lease.Lease;
import net.jini.core.lease.UnknownLeaseException;


public class LeaseExpireRenewTest extends MailboxTestBase implements TimeConstants {

    private final long DURATION = 1*MINUTES;

    public void run() throws Exception {
	logger.log(Level.INFO, "Starting up " + this.getClass().toString()); 

	String mbType = 
            getConfig().getStringConfigVal(
		MAILBOX_PROPERTY_NAME,
		MAILBOX_IF_NAME);

	logger.log(Level.INFO, "Getting ref to " + mbType); 
	Lease lease = null;
        if (mbType.equals(MAILBOX_IF_NAME)) {
	    EventMailbox mb = getMailbox(); 
	    logger.log(Level.INFO, "Generating a registration");
	    MailboxRegistration mbr = getRegistration(mb, DURATION);
	    logger.log(Level.INFO, "Getting registration lease");
	    lease = getMailboxLease(mbr);
	} else if (mbType.equals(PULL_MAILBOX_IF_NAME)) {
	    PullEventMailbox mb = getPullMailbox(); 
	    logger.log(Level.INFO, "Generating a pull registration");
	    MailboxPullRegistration mbr = getPullRegistration(mb, DURATION);
	    logger.log(Level.INFO, "Getting registration lease");
	    lease = getPullMailboxLease(mbr);
	} else {
	    throw new TestException(
		"Unsupported mailbox type requested" + mbType);
	}
	

	logger.log(Level.INFO, "Checking lease");
	checkLease(lease, DURATION); 

	logger.log(Level.INFO, 
		   "Slept @: " + new Date(System.currentTimeMillis()));
	Thread.sleep(DURATION);
	logger.log(Level.INFO, 
		   "Awoken @: " + new Date(System.currentTimeMillis()));

	logger.log(Level.INFO, "Attempting to renew lease");
	try {
	    lease.renew(DURATION);
	    throw new TestException("Successfully renewed an expired lease");
	} catch (UnknownLeaseException ule) {
	    logger.log(Level.INFO, "Caught expected exception upon renewal");
	}
    }

    /**
     * Invoke parent's construct and parser
     */
    public Test construct(QAConfig sysConfig) throws Exception {
	super.construct(sysConfig);
	parse();
        return this;
    }
}
