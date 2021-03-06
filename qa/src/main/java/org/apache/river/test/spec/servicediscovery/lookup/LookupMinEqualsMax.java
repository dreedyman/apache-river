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

package org.apache.river.test.spec.servicediscovery.lookup;

import java.util.logging.Level;

import org.apache.river.test.spec.servicediscovery.AbstractBaseTest;
import org.apache.river.test.share.DiscoveryServiceUtil;

import net.jini.lookup.ServiceItemFilter;
import net.jini.core.lookup.ServiceItem;

import java.util.ArrayList;
import org.apache.river.qa.harness.QAConfig;
import org.apache.river.qa.harness.TestException;
import org.apache.river.qa.harness.Test;

import java.util.List;

/**
 * With respect to the <code>lookup</code> method defined by the 
 * <code>ServiceDiscoveryManager</code> utility class, this class verifies
 * that the blocking version that returns an array of instances of
 * <code>ServiceItem</code> operates as specified when invoked under
 * the following condition:
 * <p><ul>
 *    <li> template matching performed by the service discovery manager is
 *         based on service type only
 *    <li> the service discovery manager applies no filtering to the results
 *         of the template matching
 *    <li> the minimum number of desired services is equal to the maximum
 *         number of desired services
 * </ul><p>
 *
 * <pre>
 *    ServiceItem[] lookup(ServiceTemplate tmpl,
 *                         int minMatches,
 *                         int maxMatches
 *                         ServiceItemFilter filter,
 *                         long waitDur);
 * </pre>
 */
public class LookupMinEqualsMax extends AbstractBaseTest {

    protected volatile long waitDur = 30*1000;
    protected volatile int  minMatches = 0;
    protected volatile int  maxMatches = 0;

    /** Performs actions necessary to prepare for execution of the 
     *  current test.
     *
     *  1. Starts N lookup services 
     *  2. Registers M test services with the lookup services started above
     *  3. Creates a service discovery manager that discovers the lookup
     *     services started above
     *  4. Creates a template that will match the test services based on
     *     service type only
     */
    public Test construct(QAConfig config) throws Exception {
        super.construct(config);
        testDesc = ": multiple service lookup employing -- template, "
                   +"blocking, minMatches = maxMatches";
        registerServices(getnServices(), getnAttributes());
        maxMatches = getnServices()+getnAddServices()-1;
        minMatches = maxMatches;
        return this;
    }//end construct

    /** Cleans up all state. */
    public void tearDown() {
        /* Because service registration occurs in a separate thread, 
         * some tests can complete before all of the service(s) are 
         * registered with all of the lookup service(s). In that case,
         * a lookup service may be destroyed in the middle of one of
         * registration requests, causing a RemoteException. To avoid
         * this, an arbitrary delay is executed to allow all previous
         * registrations to complete.
         */
        logger.log(Level.FINE, ""
                     +": waiting "+(regCompletionDelay/1000)+" seconds before "
                     +"tear down to allow all registrations to complete ... ");
        DiscoveryServiceUtil.delayMS(regCompletionDelay);
        super.tearDown();
    }//end tearDown

    /** Defines the actual steps of this particular test.
     *  
     *  1. Invokes the desired version of the <code>lookup</code> method
     *     on the service discovery manager - applying NO filtering
     *     (<code>null</code> filter parameter) - to query the discovered
     *     lookup services for the desired service. 
     *  2. Verifies that the services returned are the services expected,
     *     and the <code>lookup</code> method blocks for the expected
     *     amount of time
     */
    protected void applyTestDef() throws Exception {
        /* Verify blocking mechanism for less than min registered services */
        verifyBlocking(waitDur);
        /* Register enough services to exceed the maximum, verify that the
         * call to lookup actually blocks until the desired services are
         * registered, and verify that no more than the maximum number are
         * returned.
         */
        waitDur = 1*60*1000; //reset the amount of time to block
        verifyBlocking(getnServices(), getnAddServices(),waitDur);
    }//end applyTestDef

    /** Tests that the blocking mechanism of the lookup() method will block
     *  for the expected amount of time based on the given parameter
     *  values.
     *
     *  If no services are to be registered (nSrvcs == 0), or if
     *  the template and filter combination given to lookup() match
     *  none of the registered services, this method verifies that 
     *  lookup() not only blocks the full amount of time, but also 
     *  returns null.
     *
     *  If services are to be registered (nSrvcs > 0), this method 
     *  verifies that lookup() blocks until the expected matching
     *  service is registered with with at least one of the lookup
     *  services used in the test.
     *  
     *  This method will return <code>null</code> if there are no problems.
     *  If the <code>String</code> returned by this method is 
     *  non-<code>null</code>, then the test should declare failure and
     *  display the value returned by this method.
     */
    protected void verifyBlocking(int startVal,int nSrvcs,long waitDur) 
	throws Exception
    {
        String testServiceClassname 
	    = "org.apache.river.test.spec.servicediscovery.AbstractBaseTest$TestService";
        long waitDurSecs = waitDur/1000; //for debug output
        if(nSrvcs > 0) {
            logger.log(Level.FINE, ""+": look up at least "
		       +minMatches+" service(s), but no more than "
		       +maxMatches+" service(s) -- blocking "
		       +waitDurSecs+" second(s)");
            /* Register all services after waiting less than the block time */
            logger.log(Level.FINE, ""+": "+getExpectedServiceList().size()
		       +" service(s) "
		       +"registered, registering "+nSrvcs
		       +" more service(s) ...");
            (new RegisterThread(startVal,nSrvcs,0,waitDur)).start();
        } else {//(nSrvcs<=0)
            /* Will register no more services */
            logger.log(Level.FINE, ""+": "+getExpectedServiceList().size()
		       +" service(s) "
		       +"registered, look up at least "
		       +minMatches+" service(s), but no more than "
		       +maxMatches+" service(s) -- blocking "
		       +waitDurSecs+" second(s)");
        }//endif(nSrvcs>0)

        /* Try to lookup the services, block until the services appear */
	long startTime = System.currentTimeMillis();
	ServiceItem[] srvcItems = srvcDiscoveryMgr.lookup(template,
							  minMatches,
							  maxMatches,
							  firstStageFilter,
							  waitDur);
	long endTime = System.currentTimeMillis();
	long actualBlockTime = endTime-startTime;
         /* 7th May 2013 - Peter Firmstone.
          * According to section SD4.1.3 The blocking feature of lookup
          * waitDur is measured in milliseconds.
          * 
          * Quote:
              * the method will wait a finite period of time until either an 
              * acceptable minimum number of service references are discovered 
              * or the specified time period has passed. 
          * 
          * waitError is specified incorrectly here, since it is converted to
          * seconds and hence requires that the lookup method wait no longer than one second less
          * of waitDur if the acceptable minimum number of services are 
          * discovered, this is appears incorrect.
          * 
          * long waitError = (actualBlockTime-waitDur)/1000
          * 
          * } else { //(nExpectedSrvcs>=minMatches)
	  *  // Blocking time should be less than the full amount
	  *  if(waitError >= 0) {
          * 	throw new TestException(" -- blocked longer than expected "
          *			  +"-- requested block = "
          *			  + waitDurSecs +" second(s), actual "
          * 			  +"block = "+(actualBlockTime/1000)
          *			  +" second(s)");
	  *     }
          * }//endif(nExpectedSrvcs<maxMatches)
          * 
          * In addition it was previously considered an error if the wait period
          * was exactly equal to waitDur after being rounded to seconds,
          * this is in conflict with the spec that specifies UNTIL either an 
          * acceptable minimum number of service references are discovered 
          * or the specified time period has passed. 
          * 
          * The test failed to allow for a corner case where an acceptable minimum
          * number of service references are discovered concurrently to the 
          * specified time period elapsing.
          * 
          * Additionally however, Object.wait(waitDur) may not wake up at the
          * correct wait time if another object happens to be holding the lock.
          * 
          * The spec doesn't specify limits on the wait period, except that it
          * has passed.  Originally this test allowed 30 seconds to elapse after
          * waitDur
          * 
          * I have changed waitDur to milliseconds to comply with the spec
          * on the 7th of May 2013
          * 
          * The limits were changed to allow 300 milliseconds to elapse after the 
          * wait period completes.
          *
          * The limits were changed to allow 500 milliseconds to elapse after the
          * wait period completes. - 23rd February 2014 P. Firmstone.  After
          * some tests on Solaris failed because they were 9ms overdue.
          * 
          * In addtion Item number 3 below states:
          * 
             * 3. while lookup() is blocking, if enough new services are
             *    registered so that the acceptable minimum is achieved,
             *    lookup() will return immediately; that is, even if there
             *    is more time left on the wait period, lookup() will not
             *    wait for more services beyond the minimum. 
             *
             *    For example, if 3 services are initially registered and
             *    lookup is called with min = 4 and max = 7, then lookup()
             *    will find the 3 services and then wait for more services to
             *    be registered. Suppose that while lookup() is blocking
             *    another 5 services are registered, bringing the total number
             *    of services to 8. In this case, lookup() will stopping
             *    waiting and return 4 services (the minimum), not the
             *    maximum 7.
          * 
          * The problem with this statement is if additonal threads are waiting
          * for the object monitor, there's no guarantee the waiting thread can
          * obtain the monitor first and return the minimum number.  In fact
          * the jvm is more likely to prefer a thread that's still
          * in cpu cache rather than a thread that's been waiting for a long 
          * time and isn't loaded in the cpu cache.
          * 
          * The specification doesn't make this distinction, it only limits
          * the result to maxMatches.  So we need to check that maxMatches isn't
          * exceeded.
          */
	long waitError = (actualBlockTime-waitDur);
	/* Delay to allow all of the services to finish registering */
	DiscoveryServiceUtil.delayMS(regCompletionDelay);
	/* populate the expected info after lookup to prevent delay */
	List expectedSrvcs 
	    = new ArrayList(getExpectedServiceList());
	/* Modify the list based on whether or not a filter exists */
	if(    (firstStageFilter != null)
	       && (firstStageFilter instanceof ServiceItemFilter) )
            {
                for(int i=0,indx=0,len=expectedSrvcs.size();i<len;i++) {
                    if(srvcValOdd((TestService)expectedSrvcs.get(indx))) {
                        expectedSrvcs.remove(indx);
                    } else {
                        indx++;
                    }//endif
                }//end loop
            }//endif
	/* According to section SD.4.1.3 of the spec, with respect to
	 * ServiceItem[] lookup(tmpl,min,max,filter,waitDur):
	 * 1. lookup() will query all lookups before blocking, and if
	 *    at least the acceptable minimum number of services are
	 *    found, will return without blocking
	 * 2. if the number of services found after querying all lookups
	 *    first is less than the acceptable minimum, then lookup()
	 *    will wait for the desired services to be registered with
	 *    the lookups
	 * 3. DELETED 7th May 2013 see comments
	 * 4. if the minimum number of services have not been registered
	 *    during the wait period, lookup() will return what it has
	 *    found.
         * 5. ADDED 7th May 2013 The maximum number of services returned 
         *    never exceeds maxMatches.
	 *
	 * Below, determine the number of services to expect based on
	 * the specified behavior described above.    
	 */
	int nPreReg  = countSrvcsByVal(getnServices());
	int nPostReg = expectedSrvcs.size();
	int nExpectedSrvcs = nPreReg;

	if(nPreReg < minMatches) {//will block after first lookup
	    logger.log(Level.FINE, ""+":   lookup() will block");
	    if(nPostReg > nPreReg) {//will register more services
		if(nPostReg >= minMatches) {
		    nExpectedSrvcs = minMatches;
		} else {
		    nExpectedSrvcs = nPostReg;
		}
	    } else {//will not register more services
		nExpectedSrvcs = nPreReg;
	    }
	} else {//(nPreReg >= minMatches) ==> won't block
	    logger.log(Level.FINE, ""
		       +":   lookup() will NOT block");
            /* 7th May 2013: This requirement complicates 
             * thread synchronization and is superflous to the specification.
             */
//	    if(nPreReg == minMatches) {//return min immediately
//		nExpectedSrvcs = minMatches;
//	    } else {//(nPreReg > minMatches)
		if(nPreReg < maxMatches) {
		    nExpectedSrvcs = nPreReg;
		} else {//(nPreReg >= maxMatches)
		    nExpectedSrvcs = maxMatches;
		}//endif
//	    }//endif
	}//endif            
	logger.log(Level.FINE, ""
		   +":   minMatches       = "+minMatches);
	logger.log(Level.FINE, ""
		   +":   maxMatches       = "+maxMatches);
	logger.log(Level.FINE, ""
		   +":   nPreReg          = "+nPreReg);
	logger.log(Level.FINE, ""
		   +":   nPostReg         = "+nPostReg);
	logger.log(Level.FINE, ""
		   +":   nExpectedSrvcs   = "+nExpectedSrvcs);
	logger.log(Level.FINE, ""
		   +":   srvcItems.length = "+srvcItems.length);
	if(nExpectedSrvcs < minMatches) {//block full amount
	    /* Blocking time should be within epsilon of full amount */
	    if(waitError<-3) {
		throw new TestException(" -- failed to block requested "
				  +"time -- requested block = "
				  +waitDur+" millisecond(s), actual "
				  +"block = "+(actualBlockTime)
				  +" millisecond(s)");
	    } else if(waitError>500) {
		throw new TestException(" -- exceeded requested block "
				  +"time -- requested block = "
				  +waitDur+" millisecond(s), actual "
				  +"block = "+(actualBlockTime)
				  +" millisecond(s)");
	    }//endif
	} else { //(nExpectedSrvcs>=minMatches)
	    /* Blocking time will likely be less than the full wait period */
	    if(waitError > 500) {
		throw new TestException(" -- blocked longer than expected "
				  +"-- requested block = "
				  + waitDur +" millisecond(s), actual "
				  +"block = "+(actualBlockTime)
				  +" millisecond(s) wait error = " + waitError);
	    }
	}//endif(nExpectedSrvcs<maxMatches)
	verifyServiceItems(srvcItems,
			   expectedSrvcs,
			   nExpectedSrvcs,
			   waitDur,
			   actualBlockTime);
    }//end verifyBlocking

    protected void verifyBlocking(long waitDur) throws Exception {
        verifyBlocking(0,0,waitDur);
    }//end verifyBlocking

    protected void verifyBlocking(int nSrvcs,long waitDur) throws Exception {
        verifyBlocking(0,nSrvcs,waitDur);
    }//end verifyBlocking

    private void verifyServiceItems(ServiceItem[] srvcItems,
				    List     expectedSrvcs,
				    int           nExpectedSrvcs,
				    long          waitDur,
				    long          actualBlockTime)
	throws Exception
    {
        if(srvcItems == null) {
            throw new TestException(" -- unexpected null service item array "
                              +"returned");
        } else if(srvcItems.length != nExpectedSrvcs) {
            logger.log(Level.FINE, ""+": lookup failed -- "
                              +"returned unexpected number of "
                              +"service items (expected = "
                              +nExpectedSrvcs+", but "
                              +"returned = "+srvcItems.length+")");
            throw new TestException(" -- lookup returned unexpected "
                              +"number of service items "
                              +"(expected = "
                              +nExpectedSrvcs+", but "
                              +"returned = "+srvcItems.length+")");
        } else {/* Compare returned array to expected services set */
	    label_i:
            for(int i=0;i<srvcItems.length;i++) {
                logger.log(Level.FINE, ""+": comparing sevice item "+i);
                if( srvcItems[i] == null ) {
                    throw new TestException(" -- returned service item "+i
                                      +" is null");
                } else if(srvcItems[i].service == null) {
                    throw new TestException(" -- service component of "
                                      +"returned service item "+i
                                      +" is null");
                } else {
                    for(int j=0;j<expectedSrvcs.size();j++) {
                        if( (srvcItems[i].service).equals
                                                     (expectedSrvcs.get(j)) )
                        {
                            continue label_i; // next srvcItems[i]
                        }//endif
                    }//end loop (j)
                    throw new TestException(" -- returned service item "+i
                                      +" is not contained in the "
                                      +" expected set of services");
                }//endif
            }//end loop (i)
            logger.log(Level.FINE, ""+": all expected "
                              +"services found -- requested "
                              +"block = "+(waitDur/1000)
                              +" second(s), actual block = "
                              +(actualBlockTime/1000)
                              +" second(s)");
            return;//passed
        }//endif(srvcItems==null)
    }//end verifyServiceItems

}//end class LookupMinEqualsMax


