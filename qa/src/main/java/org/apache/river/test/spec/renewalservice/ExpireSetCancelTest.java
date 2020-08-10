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

package org.apache.river.test.spec.renewalservice;

// java.rmi
import java.rmi.RemoteException;

// net.jini
import net.jini.core.lease.Lease;
import net.jini.core.lease.UnknownLeaseException;

/**
 * This class performs that same test as ExpireSetSleepTest except
 * that the lease is explicitly canceled instead of waiting for it to
 * expire.
 * 
 */
public class ExpireSetCancelTest extends ExpireSetSleepTest {
    

    // purposefully inherit javadoc from superclass
    protected boolean expireRenewalSetLease(Lease lease) 
           throws UnknownLeaseException, RemoteException {

	lease.cancel();
	return true;
    }


} // ExpireSetCancelTest
