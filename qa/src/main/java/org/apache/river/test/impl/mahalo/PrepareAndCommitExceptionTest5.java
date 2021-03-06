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

import net.jini.core.lease.Lease;
import net.jini.core.transaction.CannotCommitException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.server.TransactionManager;
import org.apache.river.qa.harness.TestException;
import org.apache.river.test.share.TestParticipant;
import org.apache.river.test.share.TestParticipantImpl;
import org.apache.river.test.share.TxnManagerTest;

import java.util.logging.Level;

// Test harness specific classes
// Shared classes

/*
 * Test intended to exercise new prepareAndCommit semantics.
 * - Create single test participant that joins transaction
 * - then throws UnknownTransactionException on PAC call
 * - Test verifies that CannotCommitException is thrown from commit call 
 *   (without a timeout parameter)
 */
public class PrepareAndCommitExceptionTest5 extends TxnManagerTest {

     public void run() throws Exception {
        TransactionManager mgr = null;
        Transaction.Created cr = null;
        TestParticipant part = null;

        startTxnMgr();

        part = new TestParticipantImpl();

        mgr = manager();

        logger.log(Level.INFO, "PrepareAndCommitExceptionTest5: run: mgr = " + mgr);
        cr = TransactionFactory.create(mgr, Lease.FOREVER);
        logger.log(Level.INFO, "Created: cr = " + cr);        
        part.setBehavior(OP_JOIN);
        logger.log(Level.INFO, "Configured participant to join");        
        part.setBehavior(OP_EXCEPTION_ON_PREPARECOMMIT);
        logger.log(Level.INFO, "Configured participant to throw an exception");                
        part.setBehavior(EXCEPTION_TRANSACTION);
        logger.log(Level.INFO, "Configured participant to throw UTE");                                
        logger.log(Level.INFO, "Configuring participant to behave");
        part.behave(cr.transaction);

        logger.log(Level.INFO, "Committing transaction");
        try {
            cr.transaction.commit();
            throw new TestException("CannotCommitException not thrown");
        } catch (CannotCommitException cce) {
            logger.log(Level.INFO, "Caught expected exception: " + cce);
        }

    }
}
