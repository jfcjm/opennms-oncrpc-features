package Learning;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeoutException;

import org.dcache.utils.net.InetSocketAddresses;
import org.dcache.xdr.IpProtocolType;
import org.dcache.xdr.OncRpcClient;
import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcAuthTypeNone;
import org.dcache.xdr.RpcCall;
import org.dcache.xdr.XdrTransport;
import org.dcache.xdr.XdrVoid;
import org.dcache.xdr.portmap.GenericPortmapClient;
import org.dcache.xdr.portmap.OncRpcPortmap;
import org.dcache.xdr.portmap.rpcb;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class OncRpc4jPortmapper {
	private static final String DEFAULt_TRANSPORT = "tcp";
    private static final String TARGET_HOST = "127.0.0.1";
    private int targetPort;
	private String targetUaddrPort;
	private GenericPortmapClient portmapClient;
	private OncRpcClient rpcClient;
	private TestPingRpcServerSetup pingTestSetup;

	/**
	 * 
	 * Create a portmap server (rpcbindServer) and then a client (rpcClient)
	 * 
	 * @see #close()
	 * @throws IOException
	 * @throws TimeoutException
	 * 
	 */
	@Before
	public void launchPortmapper() throws IOException, TimeoutException {
		pingTestSetup = new TestPingRpcServerSetup();
		targetPort = pingTestSetup.getPortmapPort();
		String uAddr = pingTestSetup.getRfc56665Address();
		String[] split = uAddr.split("\\.");
		targetUaddrPort = split[split.length - 2] + "." + split[split.length - 1];
		System.out.println(targetUaddrPort);

		rpcClient = new OncRpcClient(InetAddress.getByName(TARGET_HOST), IpProtocolType.TCP, targetPort);
		XdrTransport transport = rpcClient.connect();
		assertNotNull(transport);
		assertEquals(targetPort, transport.getRemoteSocketAddress().getPort());
		portmapClient = new GenericPortmapClient(transport);
	}

	@Test(timeout = 1000)
	public void testConnect() throws IOException, TimeoutException {
		assertEquals(3, portmapClient.dump().size());

	}

	/**
	 * Trying to associate in portmapper its dynamically allocated port
	 * 
	 * By RFCs the portmapper port is 111, we would like it to be registered
	 * with the dynamic port allocated for the test
	 * 
	 * This test is deactivated as it triggers a problem in portmapper See :
	 * https://github.com/dCache/oncrpc4j/issues/56
	 * 
	 * @throws IOException
	 * @throws TimeoutException
	 */
	@Ignore
	@Test(timeout = 1000)
	public void testSetPortMapAddress() throws IOException, TimeoutException {

		assertEquals(2, portmapClient.dump().size());
		for (rpcb r : portmapClient.dump()) {
			System.out.println(r);
		}

		boolean isUnset = portmapClient.unsetPort(OncRpcPortmap.PORTMAP_PROGRAMM, OncRpcPortmap.PORTMAP_V2,
				"superuser");
		assertTrue(isUnset);
		for (rpcb r : portmapClient.dump()) {
			System.out.println(r);
		}
		boolean isSet = portmapClient.setPort(OncRpcPortmap.PORTMAP_PROGRAMM, OncRpcPortmap.PORTMAP_V2, DEFAULt_TRANSPORT,
				TARGET_HOST + targetUaddrPort, "");
		assertTrue(isSet);
		String port = portmapClient.getPort(OncRpcPortmap.PORTMAP_PROGRAMM, OncRpcPortmap.PORTMAP_V2, DEFAULt_TRANSPORT);
		assertEquals("", port);
	}

	/**
	 * Associate a new RpcProgram in the portmapper
	 * 
	 * @throws TimeoutException
	 * @throws IOException
	 * @throws OncRpcException
	 */
	@Test(timeout = 1000)
	public void testAssociate() throws OncRpcException, IOException, TimeoutException {

		String targetAddress = portmapClient.getPort(1, 1, DEFAULt_TRANSPORT);

		InetSocketAddress address = InetSocketAddresses.forUaddrString(targetAddress);
		try (OncRpcClient rpcPingClient = new OncRpcClient(address, IpProtocolType.TCP)) {
			XdrTransport transport = rpcPingClient.connect();
			assertNotNull(transport);
			RpcCall call = new RpcCall(1, 1, new RpcAuthTypeNone(), transport);
			call.call(0, XdrVoid.XDR_VOID, XdrVoid.XDR_VOID);
		}
		assertTrue(pingTestSetup.getProgramWasCalled());
	}

	/**
	 * Shutdown the server and close the client
	 * 
	 * @throws IOException
	 */
	@After
	public void close() throws IOException {
		rpcClient.close();
		pingTestSetup.stop();
	}
}
