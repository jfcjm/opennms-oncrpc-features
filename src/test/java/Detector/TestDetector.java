package Detector;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Ignore;
import org.junit.Test;
import org.opennms.netmgt.provision.support.SyncAbstractDetector;

import fr.univjfc.si.monitoring.opennms.detector.OncRpc.OncRpcDetector;
/**
 * These tests need  running Portmapper/NFS v3 server on localhost, NFS v4 must be disabled
 * @author jmk
 *
 */
@Ignore
public class TestDetector {
	private static final String TARGET_HOST = "127.0.0.1";
    @Test
	public void test() throws UnknownHostException {
		SyncAbstractDetector nfsDetector = new OncRpcDetector("NFS_V3",111);
		nfsDetector.init();
		boolean res = nfsDetector.isServiceDetected(InetAddress.getByName(TARGET_HOST));
		assertTrue(res);
	}
	@Test
	public void test2() throws UnknownHostException {
		SyncAbstractDetector nfsDetector = new OncRpcDetector("NFS_V4",111);
		nfsDetector.init();
		boolean res = nfsDetector.isServiceDetected(InetAddress.getByName(TARGET_HOST));
		assertFalse(res);
	}
	@Test
	public void test3() throws UnknownHostException {
		OncRpcDetector nfsDetector = new OncRpcDetector("PORTMAP_V4",111);
		nfsDetector.setProgramNumber(100000);
		nfsDetector.setProgramVersion(4);
		nfsDetector.init();
		boolean res = nfsDetector.isServiceDetected(InetAddress.getByName(TARGET_HOST));
		assertTrue(res);
	}
	@Test
	public void test4() throws UnknownHostException {
		OncRpcDetector nfsDetector = new OncRpcDetector("PORTMAP_V4",111);
		nfsDetector.setProgramNumber(100000);
		nfsDetector.setProgramVersion(1);
		nfsDetector.init();
		boolean res = nfsDetector.isServiceDetected(InetAddress.getByName(TARGET_HOST));
		assertFalse(res);
	}
	@Test
	public void testWithoutPortmap() throws UnknownHostException {
		OncRpcDetector nfsDetector = new OncRpcDetector("PORTMAP_V4",111);
		nfsDetector.setProgramNumber(100000);
		nfsDetector.setProgramVersion(3);
		nfsDetector.setUsePortmapper(false);
		nfsDetector.init();
		boolean res = nfsDetector.isServiceDetected(InetAddress.getByName(TARGET_HOST));
		assertTrue(res);
	}
}
