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
package org.apache.river.test.spec.lookupservice.test_set00;
import org.apache.river.qa.harness.QAConfig;
import org.apache.river.qa.harness.Test;

import java.util.logging.Level;

import org.apache.river.test.spec.lookupservice.QATestRegistrar;
import org.apache.river.test.spec.lookupservice.QATestUtils;
import org.apache.river.test.spec.lookupservice.RemoteEventComparator;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceEvent;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceRegistration;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;

import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

/** This class is used to verify that after using templates containing only 
 *  a service ID to request notification of MATCH_MATCH|MATCH_NOMATCH events,
 *  upon the expiration of each service item's lease, the expected set of 
 *  events will be generated by the lookup service.
 *
 *  @see org.apache.river.qa.harness.TestEnvironment
 *  @see org.apache.river.test.spec.lookupservice.QATestRegistrar
 *  @see org.apache.river.test.spec.lookupservice.QATestUtils
 */
public class NotifyOnSrvcLeaseExpiration extends QATestRegistrar {

    /** Class which handles all events sent by the lookup service */
    private class Listener extends BasicListener implements RemoteEventListener
    {
        public Listener() throws RemoteException {
            super();
        }

        /** Method called remotely by lookup to handle the generated event. */
        public void notify(RemoteEvent ev) {
            ServiceEvent srvcEvnt = (ServiceEvent)ev;
            synchronized (NotifyOnSrvcLeaseExpiration.this){
                evntVec.add(srvcEvnt);
            }
        }
    }

    protected List<ServiceEvent> evntVec = new ArrayList<ServiceEvent>(50);

    private long srvcLeaseDurMS;
    private ServiceItem[] srvcItems;
    private ServiceRegistration[] srvcRegs;
    private ServiceTemplate[] srvcIDTmpl;
    private ServiceRegistrar proxy;
    private int nInstances = 0;
    private int nInstancesPerClass = 0;

    /* The event handler for the services registered by this class */
    private static RemoteEventListener listener;

    /* The transition expected to be returned for all services */
    private static int EXPECTED_TRANSITION
                                   = ServiceRegistrar.TRANSITION_MATCH_NOMATCH;

    /** Performs actions necessary to prepare for execution of the 
     *  current QA test.
     *
     *  Creates the lookup service. Creates a single event handler to 
     *  handle all events generated by any of the registered service items. 
     *  Loads and instantiates all service classes. Registers each service 
     *  class instance with a lease duration specified in the configuration
     *  properties file. Retrieves the proxy to the lookup Registrar. Creates 
     *  an array of ServiceTemplates in which each element contains the 
     *  service ID of one of the registered service items. For each registered
     *  service, registers an event notification request, with the maximum 
     *  lease duration, based on the contents of the corresponding template 
     *  and the appropriate transition mask; along with a callback containing 
     *  the service ID.
     */
    public synchronized Test construct(QAConfig sysConfig) throws Exception {
        int i;
        ServiceID curSrvcID;
	EventRegistration[] evntRegs;
        int regTransitions =   ServiceRegistrar.TRANSITION_MATCH_MATCH
                             | ServiceRegistrar.TRANSITION_MATCH_NOMATCH;
	super.construct(sysConfig);

	logger.log(Level.FINE, "in setup() method.");

	listener = new Listener();
        ((BasicListener) listener).export();
        nInstances = super.getNInstances();
        nInstancesPerClass = super.getNInstancesPerClass();
        srvcLeaseDurMS = 2*nInstances*QATestUtils.N_MS_PER_SEC;
	srvcItems = super.createServiceItems(TEST_SRVC_CLASSES);
	srvcRegs = super.registerAll(srvcLeaseDurMS);
	proxy = super.getProxy();
	srvcIDTmpl = new ServiceTemplate[srvcRegs.length];
	evntRegs = new EventRegistration[srvcRegs.length];
        for (i=0;i<srvcRegs.length;i++) {
            curSrvcID = srvcRegs[i].getServiceID();
            srvcIDTmpl[i] = new ServiceTemplate(curSrvcID,null,null);
	    EventRegistration er;
	    er = proxy.notify(srvcIDTmpl[i], regTransitions, listener,
			      new MarshalledObject(curSrvcID),
			      Long.MAX_VALUE);
	    evntRegs[i] = prepareEventRegistration(er);
	}
        return this;
    }

    /** Executes the current QA test.
     *
     *  Waits a configured amount of time designed to guarantee that all 
     *  of the service leases have expired; and that allows all of the 
     *  events to be generated and collected. Determines if all of the 
     *  expected -- as well as no un-expected -- events have arrived. 
     *  This test depends on the semantics of event-notification. That is, 
     *  it uses the fact that if the events were generated for each service 
     *  item in sequence (which they were), then the events will arrive 
     *  in that same sequence. This means one can expect, when examining 
     *  the event corresponding to index i, that the service ID returned 
     *  in the ServiceEvent should correspond to the i_th service item 
     *  registered. If it does not, then failure is declared. Thus, this 
     *  test does the following:  1. Verifies that the number of expected 
     *  events equals the number of events that have arrived. 2. Verifies 
     *  that the transition returned in event[i] corresponds to the expected 
     *  transition (MATCH_NOMATCH). 3. Verifies that the service ID returned 
     *  in event[i] equals the service ID of the i_th service registered. 
     *  4. Verifies that the handback returned in the i_th event object 
     *  equals the service ID of the i_th service.
     */
    public synchronized void run() throws Exception {
	logger.log(Level.FINE, "in run() method.");

	/* wait for the service leases to expire */
	try {
	    long waitTime = srvcLeaseDurMS + super.deltaTEvntNotify; 
	    logger.log(Level.FINE, "Sleeping " + waitTime + " milliseconds " +
			      "for service leases to expire.");
            QATestUtils.waitDeltaT(waitTime, this);
	    //Thread.sleep(waitTime);
	} catch (InterruptedException e) {
	}

	logger.log(Level.FINE, "Checking for the expect set of events.");
        Collections.sort(evntVec, new RemoteEventComparator());
	QATestUtils.verifyEventVector(evntVec,srvcRegs.length,
				      EXPECTED_TRANSITION,srvcRegs);
    }

    /** Performs cleanup actions necessary to achieve a graceful exit of 
     *  the current QA test.
     *
     *  Unexports the listener and then performs any remaining standard
     *  cleanup duties.
     */
    public synchronized void tearDown() {
	logger.log(Level.FINE, "in tearDown() method.");
	try {
	    unexportListener(listener, true);
	} finally {
	    super.tearDown();
	}
    }
}
