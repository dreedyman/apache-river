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
package org.apache.river.test.spec.javaspace.conformance.snapshot;

// net.jini
import net.jini.core.entry.Entry;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.server.ServerTransaction;
import net.jini.core.transaction.server.TransactionParticipant;

// org.apache.river
import org.apache.river.qa.harness.TestException;

// org.apache.river.qa
        import org.apache.river.test.spec.javaspace.conformance.SimpleEntry;
import org.apache.river.test.spec.javaspace.conformance.ParticipantImpl;
import org.apache.river.test.spec.javaspace.conformance.Committer;
import org.apache.river.test.spec.javaspace.conformance.TransactionTest;


/**
 * SnapshotTransactionCommitTakeTest asserts that a take is considered
 * to be successful only if all enclosing transactions commit successfully.
 *
 * It tests this statement for snapshots.
 *
 * @author Mikhail A. Markov
 */
public class SnapshotTransactionCommitTakeTest
        extends TransactionTest {

   /**
     * This method asserts that a take is considered to be successful
     * only if all enclosing transactions commit successfully.
     *
     * It tests this statement for snapshots.
     *
     * <P>Notes:<BR>For more information see the JavaSpaces specification
     * sections 2.5, 2.6.</P>
     */
    public void run() throws Exception {
        SimpleEntry sampleEntry = new SimpleEntry("TestEntry #1", 1);
        Entry snapshot;
        SimpleEntry result;
        Transaction txn;
        long leaseFor = timeout2;

        // first check that space is empty
        if (!checkSpace(space)) {
            throw new TestException(
                    "Space is not empty in the beginning.");
        }

        // create snapshot
        snapshot = space.snapshot(sampleEntry);

        // write snapshot of sample entry to the space
        space.write(snapshot, null, leaseForeverTime);

        // create the non null transaction
        txn = getTransaction();

        /*
         * take written entry from the space using it's snapshot
         * within the transaction
         */
        space.take(snapshot, txn, checkTime);

        // abort the transaction
        txnAbort(txn);

        // check that taken entry is still available in the space
        result = (SimpleEntry) space.read(sampleEntry, null, checkTime);

        if (result == null) {
            throw new TestException(
                    "Take operation within the transaction has removed"
                    + " taken entry from the space after"
                    + " transaction's aborting.");
        }
        logDebugText("abort works as expected.");

        // create the non null transaction with finite lease time
        txn = getTransaction(leaseFor);

        /*
         * take written entry from the space using it's snapshot
         * within the transaction
         */
        space.take(snapshot, txn, checkTime);

        // sleep to let the transaction expire
        Thread.sleep(leaseFor + 1000);

        // check that taken entry is still available in the space
        result = (SimpleEntry) space.read(sampleEntry, null, checkTime);

        if (result == null) {
            throw new TestException(
                    "Take operation within the transaction has removed"
                    + " taken entry from the space after"
                    + " transaction's expiration.");
        }
        logDebugText("transaction's expiration works as expected.");

        // create another non null transaction.
        txn = getTransaction();

        /*
         * take written entry from the space using it's snapshot
         * within the transaction
         */
        space.take(snapshot, txn, checkTime);

        /*
         * create fake TransactionParticipant which
         * will prevent normal commit completion
         */
        TransactionParticipant tp = new ParticipantImpl();
        ((ParticipantImpl)tp).export();
        ((ServerTransaction) txn).join(tp, System.currentTimeMillis());

        // run thread which will prevent normal commit completion
        Committer committer = new Committer(tp, (ServerTransaction) txn,
                mgr);
        committer.start();

        // try to commit the operation
        try {
            txnCommit(txn);
            throw new TestException(
                    "Commit completes with no exceptions.");
        } catch (Exception ex) {
            logDebugText("commit produces"
                    + " the following exception, as expected: " + ex);
        }

        result = (SimpleEntry) space.read(sampleEntry, null, checkTime);
        if (result == null) {
            throw new TestException(
                    "Take operation within the transaction has removed"
                    + " taken entry from the space after"
                    + " unsuccessfull transaction's committing.");
        }
    }
}
