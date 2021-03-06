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
package org.apache.river.test.spec.constraint.util;

// java
import java.util.Collection;

// net.jini
import net.jini.security.TrustVerifier;


/**
 * Base class for all TrustVerifier.Context impl classes.
 */
public abstract class BaseTrustVerifierContext
        implements TrustVerifier.Context {

    /**
     * Method from TrustVerifier.Context interface. Does nothing.
     *
     * @return null
     */
    public ClassLoader getClassLoader() {
        return null;
    }

    /**
     * Method from TrustVerifier.Context interface. Does nothing.
     *
     * @return null
     */
    public Collection getCallerContext() {
        return null;
    }
}
