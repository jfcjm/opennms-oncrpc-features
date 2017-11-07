package Detector;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import Learning.OpennmsPingTestSrv;
import Learning.TestPingRpcServerSetup;
import fr.univjfc.si.monitoring.opennms.detector.OncRpc.OncRpcDetector;
/**
 * Tests against embedded portmapper/program servers for OncRpcDectector
 * @author jmk
 *
 */
public class TestDetectorDistributable {
	private static final String TARGET_HOST = "localhost";
    private TestPingRpcServerSetup serverSetup;

	@Before
	public void prepare() throws IOException, TimeoutException {
		serverSetup = new TestPingRpcServerSetup();
	}
	@Test
	public void testWithPortmap() throws UnknownHostException {
		OncRpcDetector nfsDetector = new OncRpcDetector("TestOncRpcServiceWithPortmap",serverSetup.getPortmapPort());
		nfsDetector.setUsePortmapper(true);
		nfsDetector.setProgramNumber(TestPingRpcServerSetup.PINGSRV_PROGRAM_NUMBER);
		nfsDetector.setProgramVersion(OpennmsPingTestSrv.PROGRAM_VERSION);
		nfsDetector.setProgramTransport("TCP");
		nfsDetector.init();
		boolean res = nfsDetector.isServiceDetected(InetAddress.getByName(TARGET_HOST));
		assertTrue(res);
	}
	@Test
	public void testWithPortmapBadPort() throws UnknownHostException {
		OncRpcDetector nfsDetector = new OncRpcDetector("TestOncRpcServiceWithPortmap",Integer.parseInt(serverSetup.getPortmapBadPort()));
		nfsDetector.setUsePortmapper(true);
		nfsDetector.setProgramNumber(TestPingRpcServerSetup.PINGSRV_PROGRAM_NUMBER);
		nfsDetector.setProgramVersion(OpennmsPingTestSrv.PROGRAM_VERSION);
		nfsDetector.setProgramTransport("TCP");
		nfsDetector.init();
		boolean res = nfsDetector.isServiceDetected(InetAddress.getByName(TARGET_HOST));
		assertFalse(res);
	}
	@Test
	public void testWithBadPortmapProgramStopped() throws IOException {
		serverSetup.stopProgram();
		OncRpcDetector nfsDetector = new OncRpcDetector("TestOncRpcServiceWithPortmap",serverSetup.getPortmapPort());
		nfsDetector.setUsePortmapper(true);
		nfsDetector.setProgramNumber(TestPingRpcServerSetup.PINGSRV_PROGRAM_NUMBER);
		nfsDetector.setProgramVersion(OpennmsPingTestSrv.PROGRAM_VERSION);
		nfsDetector.setProgramTransport("TCP");
		nfsDetector.init();
		boolean res = nfsDetector.isServiceDetected(InetAddress.getByName(TARGET_HOST));
		assertFalse(res);
	}
	@Test(timeout=2000)
	public void testWithoutPortmap() throws UnknownHostException {
		OncRpcDetector nfsDetector = new OncRpcDetector(
				"TestOncRpcServiceWithoutPortmap",
				Integer.parseInt(serverSetup.getProgramPort()));
		nfsDetector.setUsePortmapper(false);
		nfsDetector.setProgramNumber(TestPingRpcServerSetup.PINGSRV_PROGRAM_NUMBER);
		nfsDetector.setProgramVersion(OpennmsPingTestSrv.PROGRAM_VERSION);
		nfsDetector.init();
		boolean res = nfsDetector.isServiceDetected(InetAddress.getByName(TARGET_HOST));
		assertTrue(res);
	}
	@Test(timeout=2000)
	public void testWithoutPortmapBadPort() throws UnknownHostException {
		OncRpcDetector nfsDetector = new OncRpcDetector("TestOncRpcServiceWithoutPortmap",
				Integer.parseInt(serverSetup.getProgramBadPort()));
		nfsDetector.setUsePortmapper(false);
		nfsDetector.setProgramNumber(TestPingRpcServerSetup.PINGSRV_PROGRAM_NUMBER);
		nfsDetector.setProgramVersion(OpennmsPingTestSrv.PROGRAM_VERSION);
		nfsDetector.init();
		boolean res = nfsDetector.isServiceDetected(InetAddress.getByName(TARGET_HOST));
		assertFalse(res);
	}
}
