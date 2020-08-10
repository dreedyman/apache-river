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
package org.apache.river.test.impl.outrigger.leasing;

import java.util.logging.Level;

// java classes

// jini classes
import net.jini.core.lease.Lease;
import net.jini.core.entry.Entry;
import net.jini.space.JavaSpace;
import net.jini.core.event.EventRegistration;

// Test harness specific classes
import org.apache.river.qa.harness.TestException;

// Shared classes
import org.apache.river.test.share.UninterestingEntry;

/**
 * Tests binding between leases and notifications in JavaSpaces
 */
public class UseNotifyLeaseTest extends LeaseUsesTestBase {
    final private Entry aEntry = new UninterestingEntry();
    private LeasedSpaceListener listener;
    private JavaSpace space;
    private long callbackWait;
    private boolean verbose;

    /**
     * Parse our args
     * <DL>
     *
     * <DT>-callbackWait <var>int</var><DD> Number of milliseconds we
     * will wait for the event to fire before giving up.  Defaults to 2000
     * 
     * The original test defaulted to 2 seconds waiting for an event notification
     * before giving up, under some circumstances this time period is
     * insufficient and tests assume the object isn't there.  Most lease durations
     * are 60 seconds, this increase in wait interval doesn't 
     * appear to be problematic, nor is there anything in the standards that suggest
     * such a short wait period is required.  The additional time will ensure
     * that network latency and gc events don't cause the test to fail incorrectly
     * assuming the object is no longer available.
     *
     * <DT>-verbose<DD> If set test will print a message before writing
     * an entry into the space
     *
     *</DL>
     */
    protected void parse() throws Exception {
        super.parse();
        synchronized (this){
            // Get values from property file for this test.
            callbackWait = getConfig().getLongConfigVal("org.apache.river.test.share.callbackWait", 10000);
            verbose = getConfig().getBooleanConfigVal("org.apache.river.test.share.verbose", false);

            // Log out test options.
            logger.log(Level.INFO, "callbackWait = " + callbackWait);
            logger.log(Level.INFO, "verbose = " + verbose);
        }
    }

    protected Lease acquireResource() throws TestException {
        specifyServices(new Class[] {JavaSpace.class});
        prep(0);
        Lease lease = null;
        try {
            synchronized (this){
                listener = new LeasedSpaceListener(getConfig().getConfiguration());
                space = (JavaSpace) services[0];
                EventRegistration reg = space.notify(aEntry, null, listener,
                        durationRequest, null);
                reg = (EventRegistration)
                      getConfig().prepare("test.outriggerEventRegistrationPreparer",
                                          reg);
                resourceRequested();
                lease = reg.getLease();
                lease = (Lease)
                        getConfig().prepare("test.outriggerLeasePreparer",
                                            lease);
            }
        } catch (Exception e) {
            throw new TestException("registering for event", e);
        }
        return lease;
    }
    private int count = 0;

    protected boolean isAvailable() throws TestException {
        try {
            synchronized (this){
                logger.log(Level.FINEST, "Writing entry {0}", ++count);
                synchronized (listener) {
                   assert listener.isReceived() == false;

                    /*
                     * Important to have the write inside the
                     * synchronized, otherwise we my miss
                     * listener.received transtion from false->true
                     */
                    addOutriggerLease(space.write(aEntry, null, Lease.ANY), false);
                    /* Check for spurious wakeup */
                    long startTime = System.currentTimeMillis();
                    long finishTime = startTime + callbackWait;
                    while (System.currentTimeMillis() < finishTime) {
                        logger.log(Level.FINEST, "Waiting for listener to be called at {0}", (new java.util.Date()));
                        listener.wait(callbackWait);
                        if (listener.isReceived()){
                            logger.log(Level.FINEST, "Wait done at {0}, received = {1}", new Object[]{new java.util.Date(), listener.isReceived()});
                            // Reset listener state.
                            listener.setReceived(false);
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new TestException("Testing for availability", e);
        }
        return false;
    }
}
