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
import org.apache.river.test.share.TxnManagerTest;
import org.apache.river.test.share.TxnTestUtils;

import java.util.logging.Level;

// Test harness specific classes
// Shared classes


public class ManyParticipantsTest extends TxnManagerTest {
    private static final int NUM_PARTICIPANTS = 1000;

    public void run() throws Exception {
        TransactionManager mgr = null;
        Transaction.Created cr = null;
        ServerTransaction str = null;
        TestParticipant[] parts = null;
        int state = 0;

        startTxnMgr();

        logger.log(Level.INFO, 
		   "ManyParticipantsTest: creating " + NUM_PARTICIPANTS
                + " participants");
        parts = TxnTestUtils.createParticipants(NUM_PARTICIPANTS);

        /*
         * set the behavior for the specified
         * number of participants.
         */
        mgr = manager();
        logger.log(Level.INFO, "ManyParticipantsTest: run: mgr = " + mgr);
        cr = TransactionFactory.create(mgr, Lease.FOREVER);
        str = (ServerTransaction) cr.transaction;
        state = str.mgr.getState(str.id);
        logger.log(Level.INFO, "ManyParticipantsTest: setting behavior for "
                + NUM_PARTICIPANTS + " participants");
        TxnTestUtils.setBulkBehavior(OP_JOIN, parts);
        TxnTestUtils.setBulkBehavior(OP_VOTE_PREPARED, parts);
        TxnTestUtils.doBulkBehavior(cr.transaction, parts);
        logger.log(Level.INFO, "ManyParticipantsTest: commiting the txn");
        cr.transaction.commit();

        return;
    }
}
