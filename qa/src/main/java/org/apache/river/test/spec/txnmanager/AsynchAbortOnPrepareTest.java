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
package org.apache.river.test.spec.txnmanager;

import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.server.ServerTransaction;
import net.jini.core.transaction.server.TransactionManager;
import org.apache.river.test.share.TestParticipant;
import org.apache.river.test.share.TestParticipantImpl;
import org.apache.river.test.share.TxnManagerTest;

import java.util.logging.Level;

// Test harness specific classes
// Shared classes

/**
 * Creates a transaction. Next commits the transaction and
 * waits for the transaction to switch from ACTIVE to VOTING, 
 * then proceeds to abort the transaction.
 *
 * Expected: no exception.
 */
public class AsynchAbortOnPrepareTest extends TxnManagerTest {

    public void run() throws Exception {
        TransactionManager mgr = null;
        Transaction.Created cr = null;
        ServerTransaction str = null;
        TestParticipant part = null;
        int state = 0;

        startTxnMgr();

        part = new TestParticipantImpl();

        mgr = manager();

        if (DEBUG) {
            logger.log(Level.INFO, "AsynchAbortOnPrepareTest: run: mgr = " + mgr);
        }
        cr = TransactionFactory.create(mgr, Lease.FOREVER);
        str = (ServerTransaction) cr.transaction;
        part.setBehavior(OP_JOIN);
        part.setBehavior(OP_TIMEOUT_PREPARE);
        part.setBehavior(OP_TIMEOUT_PREPARECOMMIT);
        part.setBehavior(OP_TIMEOUT_VERYLONG);
        part.setBehavior(OP_VOTE_PREPARED);
        part.behave(cr.transaction);
        Thread committer = new CommitThread(cr.transaction);
        committer.start();
        /*
         * Wait for the transaction to switch
         * from ACTIVE to VOTING, then proceed to
         * abort the transaction
         */
        while (true) {
            state = str.mgr.getState(str.id);
            if (state == VOTING) {
                break;
            }
            try { Thread.sleep(1000); } catch (InterruptedException ie) {}
        }
        /* REMIND: because this test only has 1 participant, the
         *   txnmanager calls prepareAndCommit on that participant.
         *   Thus, the following abort call doesn't complete
         *   until the participants prepareAndCommit call returns
         *   (ie, once it has slept OP_TIMEOUT_VERYLONG).
         *   This seems wrong.  Should this test have >1 participants
         *   so prepare is called and we can abort in the middle of
         *   that call?
         */
        cr.transaction.abort();

        // Expect no exception. Test passed.
        return;
    }
}
