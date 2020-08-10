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
package org.apache.river.test.impl.norm;

import java.rmi.RemoteException;

import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.export.Exporter;
import net.jini.security.TrustVerifier;
import net.jini.security.proxytrust.ServerProxyTrust;

import org.apache.river.proxy.BasicProxyTrustVerifier;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.apache.river.qa.harness.QAConfig;
import java.rmi.server.ExportException;

/**
 * Remote listener impl to be sub-classed by a class which actually does work
 */
class RemoteListener
    implements RemoteEventListener, ServerProxyTrust, Serializable
{
    private Object proxy;
    private final Exporter exporter;

    RemoteListener() throws RemoteException {
        Exporter exporter = null;
	try {
	    exporter = QAConfig.getDefaultExporter();
	    Configuration c = QAConfig.getConfig().getConfiguration();
	    if (c instanceof org.apache.river.qa.harness.QAConfiguration) {
		exporter = (Exporter) c.getEntry("test",
						 "normListenerExporter", 
						 Exporter.class);
	    }
	    
	} catch (ConfigurationException e) {
	    throw new RemoteException("Configuration problem", e);
	}
        this.exporter = exporter;
    }
    
    public synchronized void export() throws ExportException{
        proxy = exporter.export(this); 
    }

    public Object writeReplace() throws ObjectStreamException {
	return proxy;
    }

    public TrustVerifier getProxyVerifier() {
	return new BasicProxyTrustVerifier(proxy);
    }

    public synchronized void notify(RemoteEvent theEvent)
	throws UnknownEventException
    {
    }
}
