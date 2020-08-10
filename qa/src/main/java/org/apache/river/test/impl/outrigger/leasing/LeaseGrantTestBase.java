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
import net.jini.admin.Administrable;

// Test harness specific classes
import org.apache.river.qa.harness.TestException;
import org.apache.river.qa.harness.QAConfig;

// Shared classes
import org.apache.river.qa.harness.Test;
import org.apache.river.test.share.TestBase;


/**
 * Base class for tests which grab a lease and make sure the returned lease
 * meets give constraints.
 */
public abstract class LeaseGrantTestBase extends TestBase implements Test {

    /**
     * If true then the tests expects leases to granted
     * exactly.  If false the grant can be for less than the request
     */
    private volatile boolean exact = false;

    /**
     * The length of time the lease should be asked for
     */
    protected volatile long durationRequest;

    /**
     * The expiration time that would be given for the duration request
     * on the most recent use.  Not used if <code>durationRequest</code> is
     * <code>Lease.ANY</code>.
     * @see #resourceRequested
     */
    private volatile long expirationRequest;

    /**
     * The local time just after the request.  <code>expirationRequest</code>
     * is <code>requestStart + durationRequest</code>.
     * @see #resourceRequested
     */
    private volatile long requestStart;

    /**
     * The length of time that leases get cliped to if they are too
     * long. A negative value indicates no cliping is expected
     */
    private volatile long clip = -1;

    /*
     * Acceptable slop interval between when we think lease will
     * expire and when it acctually does.
     */
    protected volatile long slop = 2000;

    /**
     * Test the passed lease to see if it has been granted for an
     * acceptable length of time
     */
    protected final synchronized boolean isAcceptable(Lease underTest) {

        // if we asked for ANY lease, then any duration is cool
        if (durationRequest == Lease.ANY) {
            return true;
        }
        final long duration = underTest.getExpiration() - requestStart;

        // if we're within slop of the original request, cool
        if (withinSlop(duration, durationRequest)) {
            return true;
        }

        /*
         * if the previous test failed but cliping is allowed and the
         * duration is within slop of acceptable clip, cool
         */
        if (clip >= 0 && withinSlop(duration, clip)) {
            return true;
        }

        /*
         * if we're not exact and we're less that the original request, cool
         * substract slop from duration to avoid overflow problems
         */
        if (!exact && duration - slop <= durationRequest) {
            return true;
        }

        /*
         * Ok there is one posablity left, if we asked for forever, and got
         * an expiration at the end of time, then it is a pass even if
         * the test is "exact"
         */
        if (underTest.getExpiration() == Long.MAX_VALUE
                && durationRequest == Long.MAX_VALUE) {
            return true;
        }

        // uncool
        return false;
    }

    /**
     * Return <code>true</code> if the <code>duration</code> is within
     * the allowed slop range relative to the given <code>value</code>.
     */
    protected final synchronized boolean withinSlop(long duration, long value) {
        return (duration > value - slop && duration < value + slop);
    }

    /**
     * Log a requested lease and its result, for the given type of lease.
     */
    protected void logRequest(String type, Lease lease) {
        logger.log(Level.INFO, "Lease {0}: {1}", new Object[]{type, lease});
        logger.log(Level.INFO, "\treq:{0}", expirationRequest);
        logger.log(Level.INFO, "\tgot:{0}", lease.getExpiration());
        logger.log(Level.INFO, "\taprox duration:{0}", (lease.getExpiration() -
                  requestStart));
        logger.log(Level.INFO, "\tdrift:{0}", (lease.getExpiration() - expirationRequest));
    }

    /**
     * Should be called immediately after a leased resource is
     * requested or renewed.  Sets up relevant state, namely
     * <code>requestStart</code> and <code>expirationRequest</code>.
     * @see #requestStart
     * @see #expirationRequest
     */
    protected final synchronized void resourceRequested() {
        requestStart = System.currentTimeMillis();
        expirationRequest = requestStart + durationRequest;

        /*
         * If we get a negative result then we must have had an
         * overflow, set expirationRequest to the the end of time
         */
        if (expirationRequest < 0) {
            expirationRequest = Long.MAX_VALUE;
        }
    }

    public Test construct(QAConfig config) throws Exception {
        super.construct(config);
        this.parse();
        return this;
    }

    /**
     * Parse the test options
     *
     * <code>argv[]</code> is parsed to control various options
     *
     * <DL>
     * <DT>-exact<DD> Sets the test to fail if exact leases are not
     * granted, defaults to <code>false</code>. If this option is not
     * set tests will pass if the returned lease is less than or equal
     * to what was expected.
     *
     * <DT>-duration<DD> Length of time to request lease
     * for. Defaults to 20 seconds. The string "forever" is
     * interpreted as <code>Lease.FOREVER</code>.  "anylength" is
     * interpreted as <code>Lease.ANY</code>.
     *
     * <DT>-clip<DD> If set to a non-negative value and -duration is
     * larger than -clip a lease of duration of -clip will not be
     * considered an error. Defaults to -1.
     *
     * <DT>-slop <var>milliseconds</var> <DD> Set how tolerant test is
     * of clock drift and network delays.  Defaults to 1000ms.
     *
     * </DL>
     */
    protected void parse() throws Exception {
        super.parse();
        synchronized (this){
            QAConfig config = getConfig();
            exact = config.getBooleanConfigVal("org.apache.river.test.share.exact", false);
            clip  = config.getLongConfigVal("org.apache.river.test.share.clip", -1);
            slop  = config.getLongConfigVal("org.apache.river.test.share.slop", 10000);
            final String durStr = config.getStringConfigVal("org.apache.river.test.share.duration", null);

            if (durStr == null) {
                durationRequest = 1000 * 60;
            } else if (durStr.equals("forever")) {
                durationRequest = Lease.FOREVER;
            } else if (durStr.equals("anylength")) {
                durationRequest = Lease.ANY;
            } else {
                try {
                    durationRequest = Long.parseLong(durStr);
                } catch (NumberFormatException e) {
                    throw new Exception("Malformed argument for -duration property");
                }
            }

            // Log out test options.
            logger.log(Level.INFO, "exact = {0}", exact);
            logger.log(Level.INFO, "clip = {0}", clip);
            logger.log(Level.INFO, "slop = {0}", slop);
            logger.log(Level.INFO, "durationRequest = {0}", durationRequest);
        }
    }

    /**
     * Makes sure the designated service is activated by getting its admin
     */
    protected void prep(int serviceIndex) throws TestException {
        try {
            ((Administrable) services[serviceIndex]).getAdmin();
        } catch (Throwable e) {
            setupFailure("Could not pre-activate service under test", e);
        }
    }
}
