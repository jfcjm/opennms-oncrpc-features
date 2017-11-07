package Learning;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;
import org.dcache.utils.net.InetSocketAddresses;
import org.dcache.xdr.IpProtocolType;
import org.dcache.xdr.OncRpcClient;
import org.dcache.xdr.OncRpcProgram;
import org.dcache.xdr.OncRpcSvc;
import org.dcache.xdr.OncRpcSvcBuilder;
import org.dcache.xdr.XdrTransport;
import org.dcache.xdr.portmap.GenericPortmapClient;
import org.dcache.xdr.portmap.OncRpcPortmap;
import org.dcache.xdr.portmap.OncRpcbindServer;

public class TestPingRpcServerSetup {
	private static final String TARGET_HOST = "127.0.0.1";
    public static final int PINGSRV_PROGRAM_NUMBER = 1;
	private OncRpcSvc rpcbindServer;
	private int portmapperPort;
	private String targetUaddrPort;
	private OpennmsPingTestSrv echoResponder;
	private OncRpcSvc rpcEchoServer;
	private InetSocketAddress programAddress;

	public TestPingRpcServerSetup() throws IOException, TimeoutException {
		rpcbindServer = new OncRpcSvcBuilder()
				.withTCP()
				.withUDP()
				.withoutAutoPublish()
				.withRpcService(new OncRpcProgram(OncRpcPortmap.PORTMAP_PROGRAMM, OncRpcPortmap.PORTMAP_V2),new OncRpcbindServer())
				.build();
		rpcbindServer.start();
		portmapperPort = rpcbindServer.getInetSocketAddress(IpProtocolType.TCP).getPort();
		assertNotEquals(111,portmapperPort);
		System.out.println(rpcbindServer.getInetSocketAddress(IpProtocolType.TCP));
		System.out.println(InetSocketAddresses.uaddrOf(rpcbindServer.getInetSocketAddress(IpProtocolType.TCP)));
		System.out.println(portmapperPort);
		String uAddr = InetSocketAddresses.uaddrOf(rpcbindServer.getInetSocketAddress(IpProtocolType.TCP));
		String[] split = uAddr.split("\\.");
		targetUaddrPort = split[split.length - 2] + "." + split[split.length - 1];
		System.out.println(targetUaddrPort);
		startProgram();
	}

	private void startProgram() throws IOException, TimeoutException {
		echoResponder = new OpennmsPingTestSrv();
		rpcEchoServer = new OncRpcSvcBuilder()
                .withTCP()
                .withUDP()
                .withoutAutoPublish()
                .withServiceName("OpennmsEchoTestSrv")
                .withRpcService(new OncRpcProgram(PINGSRV_PROGRAM_NUMBER, OpennmsPingTestSrv.PROGRAM_VERSION), echoResponder)
                .build();
		rpcEchoServer.start();

		programAddress = rpcEchoServer.getInetSocketAddress(IpProtocolType.TCP);
		
		String srvRfc56665Addr = InetSocketAddresses.uaddrOf(programAddress);
		try(OncRpcClient rpcClient = new OncRpcClient(InetAddress.getByName(TARGET_HOST), IpProtocolType.TCP, portmapperPort)){
			XdrTransport transport = rpcClient.connect();
			assertNotNull(transport);
			//THe generic client send ping in order to determine the version
			// of the portmap program (ver2 or ver 3)
			assertEquals(portmapperPort, transport.getRemoteSocketAddress().getPort());
			GenericPortmapClient portmapClient = new GenericPortmapClient(transport);
			assertTrue(portmapClient.setPort(PINGSRV_PROGRAM_NUMBER, OpennmsPingTestSrv.PROGRAM_VERSION, "tcp", srvRfc56665Addr, "opennmstester"));
			assertEquals(3,portmapClient.dump().size());
		}
	}


	public void stopProgram() throws IOException {
		rpcEchoServer.stop();
	}
	public int getPortmapPort() {
		return portmapperPort;
	}

	public String getRfc56665Address() {
		return InetSocketAddresses.uaddrOf(rpcbindServer.getInetSocketAddress(IpProtocolType.TCP));
	}

	public boolean getProgramWasCalled() throws IOException {
		this.stop();
		return echoResponder.wasCalled;
	}

	public void stop() throws IOException {
		rpcbindServer.stop();
	}

	public String getProgramPort() {
		return Integer.toString(programAddress.getPort());
	}

	public String getProgramBadPort() {
		return Integer.toString(programAddress.getPort() + 1);
	}

	public String getPortmapBadPort() {
		return Integer.toString(portmapperPort+1);
	}

	public String getProgramNumber() {
		return Integer.toString(PINGSRV_PROGRAM_NUMBER);
	}


	public Object getProgramBadNumber() {
		return Integer.toString(PINGSRV_PROGRAM_NUMBER+1);
	}
	public String getProgramVersion() {
		return Integer.toString(OpennmsPingTestSrv.PROGRAM_VERSION);
	}

	public String getProgramProcedure() {
		return Integer.toString(OpennmsPingTestSrv.PROGRAM_PROCEDURE);
	}

	public String getPortmapVersion() {
		return "2";
	}

	public String getBadPortmapVersion() {
		return "24";
	}

	public String getBadProcedure() {
		return "12";
	}

	public String getPortmapTransport() {
		return "TCP";
	}

	public Object getProgramTransport() {
		return "TCP";
	}
}
