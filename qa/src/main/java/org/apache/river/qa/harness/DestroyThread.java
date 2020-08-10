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

package org.apache.river.qa.harness;

import net.jini.export.Exporter;

/**
 * A thread which unexports and destroys the group after a 2 second delay
 */
class DestroyThread extends Thread {
    final Exporter exporter;

    /** Create a non-daemon thread */
    public DestroyThread(Exporter exporter) {
        super("destroy");
        this.exporter = exporter;
    }

    public void run() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            this.interrupt();
        } finally {
            exporter.unexport(true);
            System.exit(0);
        }
    }
    
}
