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

import org.apache.river.constants.TimeConstants;
import org.apache.river.qa.harness.QAConfig;
import org.apache.river.qa.harness.Test;

import net.jini.core.transaction.server.NestableTransactionManager;
import net.jini.core.transaction.server.NestableServerTransaction;

public class NestableServerTransactionCreatedToStringTest 
    extends TxnMgrTestBase 
    implements TimeConstants 
{
    //
    // This should be long enough to sensibly run the test.
    // If the service doesn't grant long enough leases, then
    // we might have to resort to using something like the
    // LeaseRenewalManager to keep our leases current.
    //
    private final long DURATION = 5*MINUTES;
    
    static NestableTransactionManager noOpNestableTransactionManager = 
        new NoOpNestableTransactionManager();
    
    static NestableServerTransaction noOpNestableServerTransaction = 
        new NestableServerTransaction(
            noOpNestableTransactionManager, 0L, (NestableServerTransaction)null);
    
               
    public void run() throws Exception {
        /*
         * Mainly checking to see if the toString() method throws 
         * an exception (e.g. NullPointerException) with "bad" values.
         */
	logger.log(Level.INFO, "Starting up " + this.getClass().toString()); 
        NestableServerTransaction nullMgrIdParent = 
            new NestableServerTransaction(
		(NestableTransactionManager)null, 
		0,
		(NestableServerTransaction)null);
        logger.log(Level.INFO, "nullMgrIdParent: " + nullMgrIdParent);
        NestableServerTransaction nullIdParent = 
            new NestableServerTransaction(
		noOpNestableTransactionManager, 
		0,
		(NestableServerTransaction)null);
        logger.log(Level.INFO, "nullIdParent: " + nullIdParent);    
        NestableServerTransaction nullMgrId = 
            new NestableServerTransaction(
		(NestableTransactionManager)null, 
		0,
		noOpNestableServerTransaction);
        logger.log(Level.INFO, "nullMgrId: " + nullMgrId);
        NestableServerTransaction nonNullMgrIdParent = 
            new NestableServerTransaction(
		noOpNestableTransactionManager, 
		0,
		noOpNestableServerTransaction);
        logger.log(Level.INFO, "nonNullMgrIdParent: " + nonNullMgrIdParent);
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
