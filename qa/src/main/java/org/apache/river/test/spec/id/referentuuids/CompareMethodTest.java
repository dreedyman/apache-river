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
package org.apache.river.test.spec.id.referentuuids;

import org.apache.river.qa.harness.QATestEnvironment;
import org.apache.river.qa.harness.QAConfig;
import org.apache.river.qa.harness.Test;

import net.jini.id.ReferentUuids;
import net.jini.id.Uuid;
import net.jini.id.UuidFactory;

import org.apache.river.test.spec.id.util.FakeReferentUuid;

import java.util.logging.Level;

/**
 * <pre>
 * Purpose
 *   This test verifies the behavior of the ReferentUuids
 *   compare method.
 * 
 * Test Cases
 *   This test iterates over a set of 3-tuples.  Each 3-tuple denotes one
 *   test case and is defined by these variables:
 *      FakeReferentUuid referentUuid1
 *      FakeReferentUuid referentUuid2
 *      boolean          expectedResult
 * 
 * Infrastructure
 *   This test requires the following infrastructure:
 *     1) FakeReferentUuid
 *          -implements ReferentUuid
 *          -getReferentUuid returns Uuid passed to constructor
 * 
 * Actions
 *   For each test case the test performs the following steps:
 *     1) call compare, passing in referentUuid1 and referentUuid2
 *     2) assert expectedResult is returned
 * </pre>
 */
public class CompareMethodTest extends QATestEnvironment implements Test {

    Object[][] cases;

    public Test construct(QAConfig sysConfig) throws Exception {
        // expectedResult
        Boolean f = Boolean.FALSE;
        Boolean t = Boolean.TRUE;

        // uuids
        Uuid u1 = UuidFactory.create(1,1);
        Uuid u2 = UuidFactory.create(1,2);

        // test cases
        cases = new Object[][] {
            // referentUuid1, referentUuid2, expectedResult
            {null,                      null,                      t},
            {null,                      new FakeReferentUuid(u1),  f},
            {new FakeReferentUuid(u1),  null,                      f},
            {new FakeReferentUuid(u1),  new FakeReferentUuid(u1),  t},
            {new FakeReferentUuid(u2),  new FakeReferentUuid(u1),  f},
            {new Object(),              new FakeReferentUuid(u1),  f},
            {new FakeReferentUuid(u1),  new Object(),              f},
            {new Object(),              new Object(),              f}
        };
        return this;
    }

    public void run() throws Exception {
        for (int i = 0; i < cases.length; i++) {
            logger.log(Level.FINE,"=================================");
            Object referentUuid1 = cases[i][0];
            Object referentUuid2 = cases[i][1];
            boolean expectedResult = ((Boolean)cases[i][2]).booleanValue();
            logger.log(Level.FINE,"test case " + (i+1) + ": "
                + "referentUuid1:" + referentUuid1
                + ", referentUuid2:" + referentUuid2
                + ", expectedResult:" + expectedResult);
            logger.log(Level.FINE,"");

            // verify ReferentUuids.compare method
            assertion( ReferentUuids.compare(referentUuid1,referentUuid2) 
                == expectedResult);
        }

        return;
    }

    public void tearDown() {
    }

}

