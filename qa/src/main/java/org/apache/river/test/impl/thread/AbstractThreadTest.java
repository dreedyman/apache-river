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
package org.apache.river.test.impl.thread;
//harness imports
import org.apache.river.qa.harness.QAConfig;
import org.apache.river.qa.harness.LegacyTest;
import org.apache.river.qa.harness.Test;

/**
 * Abstract utility class that serves as the base for all
 * thread tests
 */
public abstract class AbstractThreadTest implements LegacyTest {

    protected static QAConfig sysConfig;

    //inherit javadoc
    public Test construct(QAConfig config) {
        sysConfig = config;
        return this;
    }

    //inherit javadoc
    public void tearDown() {
    }
}
