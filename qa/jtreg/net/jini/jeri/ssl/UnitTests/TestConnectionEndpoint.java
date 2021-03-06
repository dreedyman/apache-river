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

/* @test -- Disabled for now.  -tjb[28.Apr.2003]
 * @ignore
 * @summary Tests the SecureConnectionEndpoint methods provided by the Endpoint
 *	    class.
 * @author Tim Blackman
 * @library ../../../../../unittestlib
 * @build UnitTestUtilities BasicTest Test
 * @build TestEndpoint TestEndpointUtilities TestUtilities 
 * @run main/othervm/policy=policy/timeout=250 TestConnectionEndpoint
 */

import org.apache.river.temp.davis.core.security.*;
import org.apache.river.temp.davis.io.UnsupportedSecurityException;
import org.apache.river.temp.davis.securejeri.*;
import org.apache.river.temp.davis.securejeri.connection.*;
import org.apache.river.temp.davis.security.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import javax.security.auth.Subject;

public class TestConnectionEndpoint extends TestEndpoint {

    /** All tests */
    public static final Collection tests = new ArrayList();

    /** Runs all tests */
    public static void main(String[] args) {
	test(tests);
    }

    /* -- Provide support for managing connections -- */

    static abstract class LocalConnectionTest extends LocalTest {
	final SecurityConstraints clientConstraints;
	final Subject clientSubject;
	private SecureCallContext context;
	private SecureConnectionEndpoint connectionEndpoint;

	LocalConnectionTest(String name,
			    boolean clientAuth,
			    Subject serverSubject,
			    SecurityConstraints clientConstraints,
			    Subject clientSubject)
	{
	    super(name +
		  "\n  clientConstraints=" + clientConstraints +
		  "\n  clientSubject=" + subjectString(clientSubject),
		  clientAuth, serverSubject);
	    this.clientConstraints = clientConstraints;
	    this.clientSubject = clientSubject;
	}

	SecureCallContext getCallContext() throws IOException {
	    if (context == null) {
		context = getEndpoint().getCallContext(
		    clientConstraints, clientSubject);
	    }
	    return context;
	}

	SecureConnectionEndpoint getConnectionEndpoint() throws IOException {
	    if (connectionEndpoint == null) {
		connectionEndpoint = TestUtilities.getConnectionEndpoint(
		    getEndpoint());
	    }
	    return connectionEndpoint;
	}

	SecureConnection getConnection() throws IOException {
	    SecureOutboundRequest req =
		getEndpoint().newRequest(getCallContext());
	    SecureConnection result = getOutboundRequestConnection(req);
	    req.getRequestOutputStream().close();
	    req.getResponseInputStream().close();
	    return result;
	}
    }

    /* -- Test openConnection() -- */

    static {
	tests.add(TestOpenConnection.localTests);
    }

    static class TestOpenConnection extends LocalConnectionTest {
	private static final boolean THROWS = false;
	private static final boolean RETURNS = true;

	static Test[] localTests = {
	    new TestOpenConnection(
		"Wrong server principal",
		NO_CLIENT_AUTH,
		serverRSASubject,
		requirements(
		    ServerAuthentication.YES,
		    serverPrincipals(x500("CN=Wrong"))),
		null,
		THROWS),
	    new TestOpenConnection(
		"Right server principal",
		CLIENT_AUTH,
		serverRSASubject,
		requirements(ServerAuthentication.YES,
			     serverPrincipals(x500(serverRSA))),
		TestUtilities.clientSubject,
		RETURNS),
	    new TestOpenConnection(
		"Server should be anonymous",
		NO_CLIENT_AUTH,
		serverRSASubject,
		requirements(ServerAuthentication.NO),
		TestUtilities.clientSubject,
		RETURNS),
	    new TestOpenConnection(
		"DSA client principal with RSA server principal",
		CLIENT_AUTH,
		serverRSASubject,
		requirements(ClientAuthentication.YES,
			     minPrincipals(x500(clientDSA)),
			     ServerAuthentication.YES,
			     serverPrincipals(x500(serverRSA))),
		new WithSubject() { {
		    addX500Principal("clientDSA", subject);
		} }.subject(),
		RETURNS),
	    new TestOpenConnection(
		"Wrong server private key",
		NO_CLIENT_AUTH,
		new WithSubject() { {
		    addX500Principal("clientRSA1", subject, false);
		    addPrivateKey("clientRSA2", "clientRSA1", subject);
		} }.subject(),
		requirements(ServerAuthentication.YES),
		null,
		THROWS),
	    new TestOpenConnection(
		"Wrong client private key",
		CLIENT_AUTH,
		serverRSASubject,
		requirements(ClientAuthentication.YES),
		new WithSubject() { {
		    addX500Principal("clientRSA1", subject, false);
		    addPrivateKey("clientRSA2", "clientRSA1", subject);
		} }.subject(),
		THROWS),
	    new TestOpenConnection(
		"No authentication permission for " +
		"client and server principals",
		CLIENT_AUTH,
		new WithSubject() { {
		    addX500Principal("noPerm", subject);
		} }.subject(),
		requirements(ClientAuthentication.YES,
			     ServerAuthentication.YES),
		TestUtilities.clientSubject,
		THROWS),
	    new TestOpenConnection(
		"Destroyed server credentials",
		CLIENT_AUTH,
		new WithSubject() { {
		    addX500Principal("serverRSA", subject);
		} }.subject(),
		requirements(ClientAuthentication.YES,
			     ServerAuthentication.YES),
		TestUtilities.clientSubject,
		THROWS)
	    {
		SecureConnection openConnection(
		    SecureConnectionEndpoint connectionEndpoint,
		    SecureCallContext context)
		    throws IOException
		{
		    destroyPrivateCredentials(serverSubject);
		    return super.openConnection(
			connectionEndpoint, context);
		}
	    },
	    new TestOpenConnection(
		"Destroyed client credentials",
		CLIENT_AUTH,
		serverRSASubject,
		requirements(ClientAuthentication.YES,
			     ServerAuthentication.YES),
		new WithSubject() { {
		    addX500Principal("clientRSA1", subject);
		} }.subject(false),
		THROWS)
	    {
		SecureConnection openConnection(
		    SecureConnectionEndpoint connectionEndpoint,
		    SecureCallContext context)
		    throws IOException
		{
		    destroyPrivateCredentials(clientSubject);
		    return super.openConnection(
			connectionEndpoint, context);
		}
	    },
	    new TestOpenConnection(
		"Renegotiate because of missing credential",
		CLIENT_AUTH,
		TestUtilities.serverSubject,
		constraints(
		    array(ClientAuthentication.YES, ServerAuthentication.YES),
		    array(Confidentiality.YES)),
		new WithSubject() { {
		    addX500Principal("clientRSA1", subject);
		} }.subject(false),
		RETURNS)
	    {
		SecureConnection openConnection(
		    SecureConnectionEndpoint connectionEndpoint,
		    SecureCallContext context)
		    throws IOException
		{
		    SecureConnection first = super.openConnection(
			connectionEndpoint, context);
		    first.close();
		    clientSubject.getPrincipals().remove(x500(clientRSA1));
		    addX500Principal("clientRSA2", clientSubject);
		    return super.openConnection(
			connectionEndpoint, context);
		}
		public void check(Object result) throws Exception {
		    if (result instanceof SecureConnection) {
			SecureConnection sc = (SecureConnection) result;
			/*
			 * Throws an exception if the socket was closed, which
			 * it shouldn't be.
			 */
			sc.getInputStream();
		    }
		    super.check(result);
		}
	    },
	    new TestOpenConnection(
		"Expired client credentials",
		CLIENT_AUTH,
		TestUtilities.serverSubject,
		requirements(ClientAuthentication.YES),
		new WithSubject() { {
		    addX500Principal("clientDSA2expired", subject);
		} }.subject(),
		THROWS),
	    new TestOpenConnection(
		"Expired server credentials",
		NO_CLIENT_AUTH,
		new WithSubject() { {
		    addX500Principal("serverRSA2expired", subject);
		} }.subject(),
		requirements(ClientAuthentication.NO,ServerAuthentication.YES),
		null /* clientSubject */,
		THROWS),
	    new TestOpenConnection(
		"No authentication permission for existing, non-encrypting " +
		"session",
		CLIENT_AUTH,
		TestUtilities.serverSubject,
		requirements(ClientAuthentication.YES, Confidentiality.NO),
		TestUtilities.clientSubject,
		RETURNS)
	    {
		SecureConnection openConnection(
		    SecureConnectionEndpoint connectionEndpoint,
		    SecureCallContext context)
		    throws IOException
		{
		    /*
		     * Create a non-encrypting session using the RSA client
		     * principal.
		     */
		    SecureConnection first = openConnectionWithAuth(
			connectionEndpoint, context, x500(clientRSA1));
		    /*
		     * Close the connection so that the SSL context for that
		     * session goes into the pool of free contexts.
		     */
		    first.close();
		    /*
		     * Create another connection without access to the RSA
		     * client principal.  JSSE will reuse the non-encrypting
		     * session and the provider will invalidate it.  Make sure
		     * the provider does not attempt to switch constraints from
		     * the non-encrypting session, which causes an error in
		     * JSSE.
		     */
		    return openConnectionWithAuth(
			connectionEndpoint, context, x500(clientDSA));
		}
		SecureConnection openConnectionWithAuth(
		    final SecureConnectionEndpoint connectionEndpoint,
		    final SecureCallContext context,
		    final Principal clientPrincipal)
		    throws IOException
		{
		    return (SecureConnection) AccessController.doPrivileged(
			new PrivilegedAction() {
			    public Object run() {
				try {
				    return connectionEndpoint.openConnection(
					context);
				} catch (IOException e) {
				    throw unexpectedException(e);
				}
			    }
			},
			withAuthenticationPermissions(
			    new AuthenticationPermission[] {
				new AuthenticationPermission(
				    Collections.singleton(clientPrincipal),
				    null, "connect") }));
		}
	    }    
	};

	public static void main(String[] args) {
	    test(localTests);
	}

	private TestOpenConnection(String name,
				   boolean clientAuth,
				   Subject serverSubject,
				   SecurityConstraints clientConstraints,
				   Subject clientSubject,
				   boolean shouldReturn)
	{
	    super(name, clientAuth, serverSubject, clientConstraints,
		  clientSubject);
	    setCompareTo(new Boolean(shouldReturn));
	}

	public Object run() throws Exception {
	    SecureEndpoint endpoint = getEndpoint();
	    SecureCallContext context = getCallContext();
	    SecureConnectionEndpoint connectionEndpoint =
		TestUtilities.getConnectionEndpoint(endpoint);
	    SecureConnection connection;
	    try {
		connection = openConnection(connectionEndpoint, context);
	    } catch (UnsupportedSecurityException e) {
		return e;
	    } catch (IOException e) {
		/*
		 * Note that JSSE throws SocketException if
		 * Socket.getOutputStream is called on an SSL socket whose
		 * handshake has failed, say because a client certificate was
		 * expired.  -tjb[8.Oct.2001]
		 */
		return e;
	    }
	    return connection;
	}

	SecureConnection openConnection(
	    SecureConnectionEndpoint connectionEndpoint,
	    SecureCallContext context)
	    throws IOException
	{
	    return connectionEndpoint.openConnection(context);
	}

	public void check(Object result) throws Exception {
	    boolean shouldReturn = getCompareTo().equals(Boolean.TRUE);
	    cleanup();
	    if (shouldReturn != (result instanceof SecureConnection)) {
		throw new FailedException(
		    "Should " + (shouldReturn ? "not " : "") +
		    "have thrown an exception" +
		    (testLevel >= 5 && result instanceof Exception
		     ? ":\n" + getStackTrace((Exception) result)
		     : ""));
	    }
	}
    }

    /* -- Test chooseConnection() -- */

    static {
	tests.add(TestChooseConnection.localTests);
    }

    static class TestChooseConnection extends LocalConnectionTest {
	static final boolean RETURNS_CONNECTION = true;
	static final boolean RETURNS_NULL = false;

	static final Test[] localTests = {
	    new TestChooseConnection(
		"Same constraints",
		CLIENT_AUTH,
		TestUtilities.serverSubject,
		requirements(ClientAuthentication.YES,
			     minPrincipals(x500(clientRSA1)),
			     Confidentiality.YES,
			     Integrity.YES),
		TestUtilities.clientSubject,
		RETURNS_CONNECTION),
	    new TestChooseConnection(
		"Same constraints, null subjects",
		NO_CLIENT_AUTH,
		TestUtilities.serverSubject,
		requirements(ClientAuthentication.NO,
			     Confidentiality.YES,
			     Integrity.YES),
		null,
		RETURNS_CONNECTION),
	    new TestChooseConnection(
		"Unimplied preference",
		CLIENT_AUTH,
		TestUtilities.serverSubject,
		requirements(ClientAuthentication.YES,
			     Confidentiality.NO),
		TestUtilities.clientSubject,
		RETURNS_NULL)
	    {
		SecureCallContext getChooseCallContext() throws IOException {
		    return getEndpoint().getCallContext(
			constraints(ClientAuthentication.YES,
				    Confidentiality.YES),
			clientSubject);
		}
	    },
	    new TestChooseConnection(
		"Different preference",
		NO_CLIENT_AUTH,
		TestUtilities.serverSubject,
		constraints(Confidentiality.YES,
			    ServerAuthentication.YES),
		TestUtilities.clientSubject,
		RETURNS_NULL)
	    {
		SecureCallContext getChooseCallContext() throws IOException {
		    return getEndpoint().getCallContext(
			constraints(Confidentiality.YES,
				    ServerAuthentication.NO),
			clientSubject);
		}
	    },
	    new TestChooseConnection(
		"Wrong private key",
		CLIENT_AUTH,
		TestUtilities.serverSubject,
		requirements(ClientAuthentication.YES,
			     minPrincipals(x500(clientRSA1)),
			     Confidentiality.YES,
			     Integrity.YES),
		new WithSubject() { {
		    addX500Principal("clientRSA1", subject);
		} }.subject(false),
		RETURNS_NULL)
	    {
		SecureCallContext getChooseCallContext() throws IOException {
		    /*
		     * Replace the private credential with one that has the
		     * wrong private key.
		     */
		    clientSubject.getPrivateCredentials().clear();
		    addPrivateKey("clientRSA2", "clientRSA1", clientSubject);
		    return super.getChooseCallContext();
		}
	    },
	    new TestChooseConnection(
		"Destroyed client credentials",
		CLIENT_AUTH,
		TestUtilities.serverSubject,
		requirements(ClientAuthentication.YES),
		new WithSubject() { {
		    addX500Principal("clientRSA1", subject);
		} }.subject(false),
		RETURNS_NULL)
	    {
		SecureConnection chooseConnection(
		    Collection connections, SecureCallContext context)
		    throws IOException
		{
		    destroyPrivateCredentials(clientSubject);
		    return super.chooseConnection(connections, context);
		}
	    },
	    new TestChooseConnection(
		"After adding principal with different key type",
		CLIENT_AUTH,
		TestUtilities.serverSubject,
		requirements(ClientAuthentication.YES),
		new WithSubject() { {
		    addX500Principal("clientRSA1", subject);
		} }.subject(false),
		RETURNS_NULL)
	    {
		SecureCallContext getChooseCallContext() throws IOException {
		    addX500Principal("clientDSA", clientSubject);
		    return super.getChooseCallContext();
		}
	    },
	    new TestChooseConnection(
		"After adding principal that is a preference",
		CLIENT_AUTH,
		TestUtilities.serverSubject,
		constraints(ClientAuthentication.YES,
			    minPrincipals(x500(clientRSA2))),
		new WithSubject() { {
		    addX500Principal("clientRSA1", subject);
		} }.subject(false),
		RETURNS_NULL)
	    {
		SecureCallContext getChooseCallContext() throws IOException {
		    addX500Principal("clientRSA2", clientSubject);
		    return super.getChooseCallContext();
		}
	    },
	    new TestChooseConnection(
		"After adding principal outside requirements",
		CLIENT_AUTH,
		TestUtilities.serverSubject,
		requirements(ClientAuthentication.YES,
			     minPrincipals(x500(clientRSA1))),
		new WithSubject() { {
		    addX500Principal("clientRSA1", subject);
		} }.subject(false),
		RETURNS_CONNECTION)
	    {
		SecureCallContext getChooseCallContext() throws IOException {
		    addX500Principal("clientRSA2", clientSubject);
		    return super.getChooseCallContext();
		}
	    },
	    new TestChooseConnection(
		"No authentication permission",
		CLIENT_AUTH,
		TestUtilities.serverSubject,
		requirements(ClientAuthentication.YES),
		TestUtilities.clientSubject,
		RETURNS_NULL)
	    {
		SecureConnection chooseConnection(
		    final Collection connections,
		    final SecureCallContext context)
		{
		    return (SecureConnection) AccessController.doPrivileged(
			new PrivilegedAction() {
			    public Object run() {
				try {
				    return superChooseConnection(
					connections, context);
				} catch (IOException e) {
				    throw unexpectedException(e);
				}
			    }
			},
			withAuthenticationPermissions(null));
		}
		SecureConnection superChooseConnection(
		    Collection connections,
		    SecureCallContext context)
		    throws IOException
		{
		    return super.chooseConnection(connections, context);
		}
	    },
	    new TestChooseConnection(
		"After switching",
		CLIENT_AUTH,
		TestUtilities.serverSubject,
		constraints(array(ClientAuthentication.YES, Integrity.YES),
			    array(Confidentiality.YES)),
		TestUtilities.clientSubject,
		RETURNS_CONNECTION)
	    {
		SecurityConstraints preferNotConfidential =
		    constraints(
			array(ClientAuthentication.YES, Integrity.YES),
			array(Confidentiality.NO));
		SecureConnection chooseConnection(
		    Collection connections, SecureCallContext context)
		    throws IOException
		{
		    SecureCallContext newContext =
			getEndpoint().getCallContext(
			    preferNotConfidential, clientSubject);
		    ((SecureReusableConnection) connection)
			.switchConstraints(newContext);
		    return super.chooseConnection(connections, newContext);
		}
	    },
	    new TestChooseConnection(
		"After client-side session timeout",
		NO_CLIENT_AUTH,
		TestUtilities.serverSubject,
		requirements(ServerAuthentication.YES),
		null,
		RETURNS_NULL)
	    {
		SecureConnection chooseConnection(
		    Collection connections, SecureCallContext context)
		    throws IOException
		{
		    long old = setMaxClientSessionDuration(0);
		    try {
			return super.chooseConnection(connections, context);
		    } finally {
			setMaxClientSessionDuration(old);
		    }
		}
	    },
	    new TestChooseConnection(
		"Expired client credentials",
		CLIENT_AUTH,
		TestUtilities.serverSubject,
		requirements(ServerAuthentication.YES,
			     ClientAuthentication.YES),
		new WithSubject() { {
		    addX500Principal("clientDSA2", subject);
		} }.subject(false /* readOnly */),
		RETURNS_NULL)
	    {
		SecureConnection chooseConnection(
		    Collection connections, SecureCallContext context)
		    throws IOException
		{
		    clientSubject.getPublicCredentials().clear();
		    addX500Principal("clientDSA2expired", clientSubject);
		    return super.chooseConnection(connections, context);
		}
	    },
	    new TestChooseConnection(
		"Different required client and server principals -- " +
		"checks authentication for existing sessions",
		CLIENT_AUTH,
		serverRSASubject,
		requirements(ClientAuthentication.YES,
			     minPrincipals(x500(clientRSA1)),
			     Confidentiality.NO,
			     ServerAuthentication.YES,
			     serverPrincipals(x500(serverRSA))),
		TestUtilities.clientSubject,
		RETURNS_CONNECTION)
	};

	private final boolean returnsWhat;
	SecureConnection connection;

	public static void main(String[] args) {
	    test(localTests);
	}

	private TestChooseConnection(String name,
				     boolean clientAuth,
				     Subject serverSubject,
				     SecurityConstraints clientConstraints,
				     Subject clientSubject,
				     boolean returnsWhat)
	{
	    super(name, clientAuth, serverSubject, clientConstraints,
		  clientSubject);
	    this.returnsWhat = returnsWhat;
	}

	public Object run() throws IOException {
	    connection = getConnection();
	    setCompareTo(
		returnsWhat == RETURNS_CONNECTION ? connection : null);
	    SecureConnection result = chooseConnection(
		Collections.singletonList(connection),
		getChooseCallContext());
	    cleanup();
	    return result;
	}

	SecureCallContext getChooseCallContext() throws IOException {
	    return getEndpoint().getCallContext(
		clientConstraints, clientSubject);
	}

	SecureConnection chooseConnection(
	    Collection connections, SecureCallContext context)
	    throws IOException
	{
	    return getConnectionEndpoint().chooseConnection(
		connections, context);
	}
    }

    /* -- Test canSwitchConstraints() and switchConstraints() -- */

    static {
	tests.add(TestSwitchConstraints.localTests);
    }

    static class TestSwitchConstraints extends LocalConnectionTest {
	static private final boolean THROWS = false;
	static private final boolean RETURNS = true;

	static Test[] localTests = {
	    new TestSwitchConstraints(
		"Same constraints",
		requirements(Confidentiality.YES,
			     ClientAuthentication.YES,
			     Integrity.YES),
		requirements(Confidentiality.YES,
			     ClientAuthentication.YES,
			     Integrity.YES),
		RETURNS),
	    new TestSwitchConstraints(
		"Confidentiality to no confidentiality",
		requirements(Confidentiality.YES,
			     ClientAuthentication.YES,
			     Integrity.YES),
		requirements(Confidentiality.NO,
			     ClientAuthentication.YES,
			     Integrity.YES),
		RETURNS),
	    new TestSwitchConstraints(
		"No confidentiality to confidentiality",
		requirements(Confidentiality.NO,
			     ClientAuthentication.YES,
			     Integrity.YES),
		requirements(Confidentiality.YES,
			     ClientAuthentication.YES,
			     Integrity.YES),
		RETURNS),
	    new TestSwitchConstraints(
		"No confidentiality, different client principals",
		CLIENT_AUTH, TestUtilities.serverSubject,
		requirements(Confidentiality.NO,
			     ClientAuthentication.YES,
			     Integrity.YES,
			     minPrincipals(x500(clientRSA1))),
		new WithSubject() { {
		    addX500Principal("clientRSA1", subject);
		    addX500Principal("clientRSA2", subject);
		} }.subject(),
		requirements(Confidentiality.NO,
			     ClientAuthentication.YES,
			     Integrity.YES,
			     minPrincipals(x500(clientRSA2))),
		THROWS),
	    new TestSwitchConstraints(
		"Destroyed client credentials",
		CLIENT_AUTH, TestUtilities.serverSubject,
		requirements(ClientAuthentication.YES,
			     Confidentiality.YES),
		new WithSubject() { {
		    addX500Principal("clientRSA1", subject);
		} }.subject(false),
		requirements(ClientAuthentication.YES,
			     Confidentiality.NO),
		THROWS)
	    {
		Object canSwitchCompareTo() { return new Boolean(RETURNS); }
		void switchConstraints(SecureCallContext context)
		    throws IOException
		{
		    destroyPrivateCredentials(clientSubject);
		    super.switchConstraints(context);
		}
	    },
	    new TestSwitchConstraints(
		"Same constraints, null subject",
		NO_CLIENT_AUTH, TestUtilities.serverSubject,
		requirements(Confidentiality.YES,
			     ClientAuthentication.NO,
			     ServerAuthentication.YES,
			     Integrity.YES),
		null,
		requirements(Confidentiality.YES,
			     ClientAuthentication.NO,
			     ServerAuthentication.NO,
			     Integrity.YES),
		null,
		RETURNS),
	    new TestSwitchConstraints(
		"Ignore client max principals not in " +
		"min principal constraint",
		CLIENT_AUTH, TestUtilities.serverSubject,
		requirements(ClientAuthentication.YES,
			     Confidentiality.YES,
			     minPrincipals(x500(clientRSA1)),
			     maxPrincipals(x500(clientRSA1))),
		new WithSubject() { {
		    addX500Principal("clientRSA1", subject);
		    addX500Principal("clientRSA2", subject);
		} }.subject(),
		requirements(ClientAuthentication.YES,
			     Confidentiality.YES,
			     minPrincipals(x500(clientRSA1)),
			     maxPrincipals(x500(clientRSA1),
					   x500(clientRSA2))),
		RETURNS),
	    new TestSwitchConstraints(
		"No permission to switch",
		CLIENT_AUTH,
		TestUtilities.serverSubject,
		requirements(ClientAuthentication.YES,
			     Confidentiality.YES),
		TestUtilities.clientSubject,
		requirements(ClientAuthentication.YES,
			     Confidentiality.NO),
		THROWS)
	    {
		void switchConstraints(final SecureCallContext context)
		    throws IOException
		{
		    try {
			AccessController.doPrivileged(
			    new PrivilegedExceptionAction() {
				public Object run() throws IOException {
				    connection.switchConstraints(
					context);
				    return null;
				}
			    },
			    withAuthenticationPermissions(null));
		    } catch (PrivilegedActionException e) {
			throw (IOException) e.getException();
		    }
		}

		Object canSwitchCompareTo() {
		    return new Boolean(RETURNS);
		}

		public void check(Object result) throws IOException {
		    super.check(result);

		    /* Make sure that attempting to switch again succeeds */
		    if (!connection.canSwitchConstraints(getCallContext())) {
			throw new FailedException(
			    "Should still be able to switch constraints");
		    }
		}
	    },
	    new TestSwitchConstraints(
		"After client credentials expired",
		CLIENT_AUTH,
		TestUtilities.serverSubject,
		requirements(ClientAuthentication.YES,
			     Confidentiality.YES),
		new WithSubject() { {
		    addX500Principal("clientDSA2", subject);
		} }.subject(false /* readOnly */),
		requirements(ClientAuthentication.YES,
			     Confidentiality.YES),
		THROWS)
	    {
		Object canSwitchCompareTo() { return new Boolean(RETURNS); }
		void switchConstraints(SecureCallContext context)
		    throws IOException
		{
		    clientSubject.getPublicCredentials().clear();
		    addX500Principal("clientDSA2expired", clientSubject);
		    super.switchConstraints(context);
		}
	    }
	    /*
	     * XXX: Maybe add a test for what happens if the server requests a
	     * new handshake just before the client attempts to switch.  The
	     * client should wait until the server-initiated handshake is
	     * completed, notice that it picked the wrong suite, and try again.
	     * -tjb[15.Jan.2001]
	     */

	    /*
	     * XXX: Maybe add a test for what happens if the server requests a
	     * new handshake, especially to insure that the client always
	     * chooses the same principals.  I might be able to test this by
	     * modifying the server subject to include credentials with a
	     * different key type.  -tjb[16.Jan.2001]
	     */
	};

	final SecurityConstraints afterConstraints;
	final Subject afterSubject;
	SecureReusableConnection connection;

	public static void main(String[] args) {
	    test(localTests);
	}

	private TestSwitchConstraints(String name,
				      SecurityConstraints beforeConstraints,
				      SecurityConstraints afterConstraints,
				      boolean shouldReturn)
	{
	    this(name,
		 CLIENT_AUTH, TestUtilities.serverSubject,
		 beforeConstraints, TestUtilities.clientSubject,
		 afterConstraints, TestUtilities.clientSubject,
		 shouldReturn);
	}

	private TestSwitchConstraints(String name,
				      boolean clientAuth,
				      Subject serverSubject,
				      SecurityConstraints beforeConstraints,
				      Subject clientSubject,
				      SecurityConstraints afterConstraints,
				      boolean shouldReturn)
	{
	    this(name, clientAuth, serverSubject, beforeConstraints,
		 clientSubject, afterConstraints, clientSubject, shouldReturn);
	}

	private TestSwitchConstraints(String name,
				      boolean clientAuth,
				      Subject serverSubject,
				      SecurityConstraints beforeConstraints,
				      Subject beforeSubject,
				      SecurityConstraints afterConstraints,
				      Subject afterSubject,
				      boolean shouldReturn)
	{
	    super(name +
		  "\n  afterConstraints=" + afterConstraints +
		  "\n  afterSubject=" + subjectString(afterSubject),
		  clientAuth,
		  serverSubject,
		  beforeConstraints,
		  beforeSubject);
	    setCompareTo(new Boolean(shouldReturn));
	    this.afterConstraints = afterConstraints;
	    this.afterSubject = afterSubject;
	}

	public Object run() throws Exception {
	    boolean ok = false;
	    try {
		connection = (SecureReusableConnection) getConnection();
		SecureCallContext afterContext =
		    getEndpoint().getCallContext(
			afterConstraints, afterSubject);
		boolean canSwitchConstraints =
		    connection.canSwitchConstraints(afterContext);
		if (!new Boolean(canSwitchConstraints).equals(
		    canSwitchCompareTo()))
		{
		    throw new FailedException(
			"The canSwitchConstraints() method returned " +
			canSwitchConstraints + ", should be " +
			canSwitchCompareTo());
		}
		try {
		    switchConstraints(afterContext);
		    ok = true;
		    return null;
		} catch (IOException e) {
		    return e;
		} catch (IllegalArgumentException e) {
		    return e;
		}
	    } finally {
		if (!ok) {
		    cleanup();
		}
	    }
	}

	Object canSwitchCompareTo() {
	    return getCompareTo();
	}

	void switchConstraints(SecureCallContext context) throws IOException {
	    connection.switchConstraints(context);
	}

	public void check(Object result) throws IOException {
	    boolean shouldReturn = getCompareTo().equals(Boolean.TRUE);
	    boolean canSwitch = canSwitchCompareTo().equals(Boolean.TRUE);
	    cleanup();

	    if (result == null) {
		if (shouldReturn) {
		    return;
		} else {
		    throw new FailedException(
			"Should have thrown an exception");
		}
	    } 
	    String problem;
	    if (shouldReturn) {
		problem = "Unexpected exception";
	    } else if (canSwitch
		       ? !(result instanceof IOException)
		       : !(result instanceof IllegalArgumentException))
	    {
		problem = "Wrong exception";
	    } else {
		return;
	    }
	    throw new FailedException(
		problem +
		(testLevel >= 5
		 ? ":\n" + getStackTrace((Exception) result)
		 : ""));
	}
    }

    /* -- Test SecureConnection.close -- */

    static {
	tests.add(TestCloseConnection.localTests);
    }

    static class TestCloseConnection extends LocalConnectionTest {
	static Test[] localTests = {
	    new TestCloseConnection()
	};

	OutputStream requestOutputStream;
	InputStream responseInputStream;

	TestCloseConnection() {
	    super("", false /* clientAuth */, null /* serverSubject */,
		  SecurityConstraints.EMPTY, null /* clientSubject */);
	}

	public static void main(String[] args) {
	    test(localTests);
	}

	public Object run() throws IOException {
	    SecureOutboundRequest req =
		getEndpoint().newRequest(getCallContext());
	    requestOutputStream = req.getRequestOutputStream();
	    responseInputStream = req.getResponseInputStream();
	    SecureConnection connection = getOutboundRequestConnection(req);
	    connection.close();
	    return connection;
	}

	public void check(Object result) {
	    checkClosed(requestOutputStream, "request output stream");
	    checkClosed(responseInputStream, "response input stream");
	    cleanup();
	}
    }
}
