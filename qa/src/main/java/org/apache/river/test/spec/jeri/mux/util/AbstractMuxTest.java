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
package org.apache.river.test.spec.jeri.mux.util;
//harness imports
import org.apache.river.qa.harness.QAConfig;
import org.apache.river.qa.harness.LegacyTest;

//jeri imports
import org.apache.river.qa.harness.Test;
import net.jini.jeri.Endpoint;
import net.jini.jeri.ServerEndpoint;

//java.net
import java.net.InetAddress;
import java.net.UnknownHostException;

//java.util
import java.util.logging.Logger;

/**
 * Abstract utility class that serves as the base for all
 * Jini ERI Multiplexing Protocol tests.
 */
public abstract class AbstractMuxTest implements LegacyTest {

    protected static QAConfig sysConfig;
    protected static Logger log;

    private int port = 0;
    private long timeout = 0;
    private Endpoint endpoint = null;
    private ServerEndpoint se = null;
    private String host = null;

    //inherit javadoc
    public Test construct(QAConfig config) throws UnknownHostException {
        sysConfig = config;
        log = Logger.getLogger("org.apache.river.test.spec.jeri.mux");
        port = config.getIntConfigVal("org.apache.river.test.spec"
            + ".jeri.mux.listenPort",9090);
        timeout = config.getLongConfigVal("org.apache.river.test.spec.jeri"
            + ".mux.timeout",10000);
        host = InetAddress.getLocalHost().getHostAddress();
        endpoint = new TestEndpoint(port,host);
        se = new TestServerEndpoint(port);
        return this;
    }

    //inherit javadoc
    public void tearDown() {
    }

    /**
     * Returns a client endpoint that uses
     * <code>net.jini.jeri.connection.ConnectionManager</code> to manage its
     * connections.  This implies use of Jini ERI mux protocol.
     *
     * @return A client endpoint used in testing
     */
    public Endpoint getEndpoint(){
        return endpoint;
    }

    /**
     * Returns a server endpoint that uses
     * <code>net.jini.jeri.connection.ConnectionManager</code> to manage its
     * connections.  This implies use of the Jini ERI mux protocol.  Certain
     * tests expect the returned endpoint to be an instance of
     * <code>org.apache.river.test.spec.jeri.mux.util.TestServerEndoint</code>.
     *
     * @return A server endpoint to use in testing
     */
    public ServerEndpoint getServerEndpoint() {
        return se;
    }

    /**
     * Returns a port that a test can use when it needs to export an object.
     * The next sequential port should be available as well.  The port returned
     * is the value of the org.apache.river.test.spec.jeri.mux.listenPort property
     * or 9090 if this property is not set.
     *
     * @return A port on which the test can start a listen operation
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the host on which the test is being run.  All tests assume
     * client and server reside on the same host.
     *
     * @return The host on which the test is being run
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the timeout value that a test can use to wait for input to
     * be received from a connection.  The value returned is the value of
     * the org.apache.river.test.spec.jeri.mux.timeout property if the property
     * is set or 10000 is the property is not set.
     *
     * @return Timeout value to use in testing
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Returns a logger named org.apache.river.test.spec.jeri.mux that can be
     * used while executing a test.
     *
     * @return A logger for use in testing.
     */
    public static Logger getLogger(){
        return log;
    }
}
