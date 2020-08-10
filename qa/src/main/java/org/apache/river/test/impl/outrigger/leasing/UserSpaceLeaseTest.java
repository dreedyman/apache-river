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

// java classes

// jini classes
import net.jini.core.lease.Lease;
import net.jini.core.entry.Entry;
import net.jini.space.JavaSpace;

// Test harness specific classes
import org.apache.river.qa.harness.TestException;

// Shared classes
        import org.apache.river.test.share.UninterestingEntry;


/**
 * Tests binding between leases and entry in JavaSpaces.
 */
public class UserSpaceLeaseTest extends LeaseUsesTestBase {
    private volatile Entry resource;

    protected Lease acquireResource() throws TestException {
        specifyServices(new Class[] {
            JavaSpace.class});
        prep(0);
        Lease lease = null;

        try {
            resource = new UninterestingEntry();
            lease = ((JavaSpace) services[0]).write(resource, null,
                    durationRequest);
            resourceRequested();
        } catch (Exception e) {
            throw new TestException("writing entry", e);
        }
        return lease;
    }

    protected boolean isAvailable() throws TestException {
        Entry rslt = null;

        try {
            rslt = ((JavaSpace) services[0]).readIfExists(resource, null,
                    JavaSpace.NO_WAIT);
        } catch (Exception e) {
            throw new TestException("Testing for availability", e);
        }
        return !(rslt == null);
    }
}
