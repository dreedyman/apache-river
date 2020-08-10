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
package org.apache.river.test.impl.mahalo;

import java.util.logging.Level;

// Test harness specific classes
import org.apache.river.qa.harness.QAConfig;
import org.apache.river.qa.harness.TestException;

import org.apache.river.constants.TimeConstants;
import org.apache.river.qa.harness.Test;

import net.jini.core.lease.Lease;
import net.jini.core.lease.UnknownLeaseException;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.core.transaction.server.TransactionManager.Created;


public class LeaseExpireCancelTest extends TxnMgrTestBase 
    implements TimeConstants 
{

    private final long DURATION = 1*MINUTES;

    public void run() throws Exception {
	logger.log(Level.INFO, "Starting up " + this.getClass().toString()); 

        TransactionManager mb = getTransactionManager();

	logger.log(Level.INFO, "Generating a transaction");
	Created txn = getCreated(mb, DURATION);

	logger.log(Level.INFO, "Checking lease");
	Lease lease = getTransactionManagerLease(txn); 
	checkLease(lease, DURATION); 

	logger.log(Level.INFO, "Sleeping past expiration");
	Thread.sleep(DURATION);

	logger.log(Level.INFO, "Attempting to cancel lease");
	try {
	    lease.cancel();
	    throw new TestException( "Successfully cancelled an expired lease");
	} catch (UnknownLeaseException ule) {
	    logger.log(Level.INFO, 
		       "Expected UnknownLeaseException thrown by cancel()");
	}
    }

    /**
     * Invoke parent's construct and parser
     * @exception QATestException will usually indicate an "unresolved"
     *  condition because at this point the test has not yet begun.
     */
    public Test construct(QAConfig sysConfig) throws Exception {
	super.construct(sysConfig);
	parse();
        return this;
    }
}
