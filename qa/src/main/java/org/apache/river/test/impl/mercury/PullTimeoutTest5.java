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

import java.rmi.RemoteException;
import java.util.Date;
import java.util.logging.Logger;


import net.jini.event.InvalidIteratorException;
import net.jini.event.MailboxPullRegistration;
import net.jini.event.PullEventMailbox;
import net.jini.event.RemoteEventIterator;
import net.jini.core.lease.Lease;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;

import org.apache.river.constants.TimeConstants;

import org.apache.river.test.impl.mercury.EMSTestBase;

import org.apache.river.qa.harness.QAConfig;
import org.apache.river.qa.harness.Test;
import org.apache.river.qa.harness.TestException;

public class PullTimeoutTest5
    extends EMSTestBase implements TimeConstants 
{

    //
    // This should be long enough to sensibly run the test.
    // If the service doesn't grant long enough leases, then
    // we might have to resort to using something like the
    // LeaseRenewalManager to keep our leases current.
    //
    private final long REG_LEASE = 60 * 1000;
    private final long REG_LEASE_WAIT = REG_LEASE * 2;
    private final long ENABLE_REG_WAIT = REG_LEASE / 2;    
    
    private final int NUM_EVENTS = 5;

    private final long EVENT_ID = 1234;
    private final long EVENT_ID2 = 5678;

    class Enabler implements Runnable {
        final MailboxPullRegistration mr;
        final RemoteEventListener rel;
        final long delay;
        final Logger logger;
        Enabler(MailboxPullRegistration mr, RemoteEventListener rel, 
                long delay, Logger logger) 
        {
            this.mr = mr;
            this.rel = rel;
            this.delay = delay;
            this.logger = logger;        
        }
        public void run() {
            try {
                logger.log(Level.FINEST, 
                    "Enabler sleeping @ {0} for {1} ms", 
                    new Object[] { new Date(), new Long(delay)});
                Thread.sleep(delay);
                logger.log(Level.FINEST, 
                    "Enabler awoken @ {0}", new Date());
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                // ignore
                logger.log(Level.FINEST, 
                    "Sleep interrupted", ie);
            }
            try {
                mr.enableDelivery(rel);                    
                logger.log(Level.FINEST, "Enabled delivery @ {0}", new Date());
            } catch (RemoteException re) {
               logger.log(Level.FINEST, 
                   "Ignoring RemoteException from enabling delivery",
                       re);
            } 
        }
    }
    
    public void run() throws Exception {
	logger.log(Level.INFO, "Starting up " + this.getClass().toString()); 
        PullEventMailbox[] mbs = getPullMailboxes(2);

	PullEventMailbox mb = mbs[0];
        PullEventMailbox mb2 = mbs[1];
	int i = 0;

	// Register and check lease
        Date gotLease = new Date();
	MailboxPullRegistration mr = getPullRegistration(mb, REG_LEASE);
	Lease mrl = getPullMailboxLease(mr);
	checkLease(mrl, REG_LEASE); 

	MailboxPullRegistration mr2 = getPullRegistration(mb2, REG_LEASE);
//	Lease mrl2 = getPullMailboxLease(mr2);
//	checkLease(mrl2, REG_LEASE); 
        RemoteEventListener mbRel2 = getMailboxListener(mr2);
        
        
        // Start canceler thread with a delay of REG_LEASE_CANCEL_WAIT
        Thread t = 
            new Thread(
                new Enabler(mr, mbRel2, ENABLE_REG_WAIT, logger));
        t.start();
        
        // Get events and verify
	logger.log(Level.INFO, "Getting events from empty mailbox.");
        RemoteEventIterator rei = mr.getRemoteEvents();
        RemoteEvent rei_event;
        Date before = new Date();
	logger.log(Level.INFO, "Calling next() on empty set @ {0}", before);        
        try {
            rei_event = rei.next(REG_LEASE_WAIT);
            throw new TestException("Successfully called next on invalid reg.");
        } catch (InvalidIteratorException iie) {
            logger.log(Level.INFO, "Received expected exception", iie);
        }
        Date after = new Date();
        //Verify that we returned in time
        long delta = after.getTime() - before.getTime();        
	logger.log(Level.INFO, "Returned from next() @ {0}, delta = {1}", 
            new Object[] {after, new Long(delta)});   
        if (delta >= REG_LEASE_WAIT) {
             throw new TestException("Returned from next() after expected: " 
                     + delta);
        } else if (after.getTime() <= (gotLease.getTime() + ENABLE_REG_WAIT)) {
             throw new TestException("Returned from next() before expected: "
                     + after.getTime() + " ms versus " 
                     + (gotLease.getTime() + REG_LEASE) + " ms.");            
        }

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
