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
package org.apache.river.test.spec.security.authenticationpermission;

// java
import java.security.PermissionCollection;

// net.jini
import net.jini.security.AuthenticationPermission;

// org.apache.river
import org.apache.river.qa.harness.TestException;
import org.apache.river.qa.harness.QATestEnvironment;
import org.apache.river.qa.harness.Test;


/**
 * <pre>
 * Purpose
 *   This test verifies the following:
 *     'newPermissionCollection' method of AuthenticationPermission returns an
 *     empty PermissionCollection for storing AuthenticationPermission
 *     instances.
 *
 * Action
 *   The test performs the following steps:
 *     1) construct AuthenticationPermission
 *     2) call 'newPermissionCollection' method of constructed
 *        AuthenticationPermission
 *     3) assert that returned PermissionCollection is empty
 * </pre>
 */
public class NewPermissionCollectionTest extends QATestEnvironment implements Test {

    /**
     * This method performs all actions mentioned in class description.
     *
     */
    public void run() throws Exception {
        AuthenticationPermission ap = new AuthenticationPermission(
                "abc \"abc\"", "listen");
        logger.fine("Calling 'newPermissionCollection' of " + ap + ".");
        PermissionCollection pc = ap.newPermissionCollection();

        if (pc.elements().hasMoreElements()) {
            // FAIL
            throw new TestException(
                    "Created PermissionCollection is not empty: " + pc);
        } else {
            // PASS
            logger.fine("Created PermissionCollection is empty "
                    + "as expected.");
        }
    }
}
