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
package org.apache.river.mercury.proxy;

import java.io.Serializable;
/**
 * Trivial class (struct) that simply holds the current read count
 * and the associated (next unread) read position. U?sed as the client-side
 * cookie for PersistentEventLog.
 * @since 2.1
 */
public class RemoteEventDataCursor implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long readCount;
    private final long readPosition;

    public RemoteEventDataCursor(long count, long cursor) {
        readCount = count;
        readPosition = cursor;
    }

    public long getReadCount() { return readCount; }
    public long getReadPosition() { return readPosition; }
}
