package Monitor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;

import Learning.TestPingRpcServerSetup;
import fr.univjfc.si.monitoring.opennms.monitor.OncRpc.OncRpcMonitor;
public class DistributableTestRpcMonitor {
	private static final String TARGET_HOST = "localhost";
	private Map<String,Object> params;
	private AbstractServiceMonitor monitor;
	private MonitoredService svc;
	private PollStatus result;
	private boolean usePortmapper;
	private TestPingRpcServerSetup serverSetup;
	
	@Before
	public void prepare() throws IOException, TimeoutException {
		serverSetup = new TestPingRpcServerSetup();
		
		
		
		monitor = new OncRpcMonitor();
		svc = Mockito.mock(MonitoredService.class);
		when(svc.getAddress()).thenReturn(InetAddress.getByName(TARGET_HOST));
		when(svc.getSvcName()).thenReturn("mockSVC");
		
		//OncRpcMonitor use portmap by default
		usePortmapper=true;
		params = Mockito.mock(Map.class);
		assertNotEquals(111, serverSetup.getPortmapPort());
	}
	/**
	 * We do not use the portmapper for this test
	 * param "use-portmapper" is false
	 * @throws UnknownHostException
	 */
	@Test
	public void testPortmapperUp() throws UnknownHostException {
		usePortmapper=false;
		when(params.get("use-portmapper")).thenReturn(Boolean.toString(usePortmapper));
		when(params.get("program-port")).thenReturn(serverSetup.getPortmapPort());
		when(params.get("program-number")).thenReturn("100000");
		when(params.get("program-version")).thenReturn(serverSetup.getPortmapVersion());
		when(params.get("program-monitor-operation")).thenReturn("0");
		when(params.get("ds-name")).thenReturn("dsPrefix");
		
		result = monitor.poll(svc,params);
		assertEquals("Up",result.getStatusName());
	}
	@Test
	public void testPortmapperDownBadProgram() throws UnknownHostException {
		usePortmapper=false;
		when(params.get("use-portmapper")).thenReturn(Boolean.toString(usePortmapper));
		when(params.get("program-port")).thenReturn(serverSetup.getPortmapPort());
		when(params.get("program-number")).thenReturn("100001");
		when(params.get("program-version")).thenReturn(serverSetup.getPortmapVersion());
		when(params.get("program-monitor-operation")).thenReturn("0");
		when(params.get("ds-name")).thenReturn("dsPrefix");
		
		result = monitor.poll(svc,params);
		assertEquals("Down",result.getStatusName());
		assertEquals("PROG_UNAVAIL error: RPC Program was not found,  because  of a wrong configured program number or else a wrong configured version number",result.getReason());
	}
	@Test
	public void testPortmapperDownBadVersion() throws UnknownHostException {
		usePortmapper=false;
		when(params.get("use-portmapper")).thenReturn(Boolean.toString(usePortmapper));
		when(params.get("program-port")).thenReturn(serverSetup.getPortmapPort());
		when(params.get("program-number")).thenReturn("100000");
		when(params.get("program-version")).thenReturn(serverSetup.getBadPortmapVersion());
		when(params.get("program-monitor-operation")).thenReturn("0");
		when(params.get("ds-name")).thenReturn("dsPrefix");
		
		result = monitor.poll(svc,params);
		assertEquals("Down",result.getStatusName());
		assertEquals("PROG_UNAVAIL error: RPC Program was not found,  because  of a wrong configured program number or else a wrong configured version number",result.getReason());
	}
	@Test
	public void testPortmapperDownBadProc() throws UnknownHostException {
		usePortmapper=false;
		when(params.get("use-portmapper")).thenReturn(Boolean.toString(usePortmapper));
		when(params.get("program-port")).thenReturn(serverSetup.getPortmapPort());
		when(params.get("program-number")).thenReturn("100000");
		when(params.get("program-version")).thenReturn(serverSetup.getPortmapVersion());
		when(params.get("program-monitor-operation")).thenReturn(serverSetup.getBadProcedure());
		when(params.get("ds-name")).thenReturn("dsPrefix");
		
		result = monitor.poll(svc,params);
		assertEquals("Down",result.getStatusName());
		assertEquals("PROC_UNAVAIL",result.getReason());
	}
	
	@Test
	public void testPingUP() throws IOException {
		usePortmapper=false;
		when(params.get("use-portmapper")).thenReturn(Boolean.toString(usePortmapper));
		when(params.get("program-port")).thenReturn(serverSetup.getProgramPort());
		when(params.get("program-number")).thenReturn(serverSetup.getProgramNumber());
		when(params.get("program-version")).thenReturn(serverSetup.getProgramVersion());
		when(params.get("program-monitor-operation")).thenReturn(serverSetup.getProgramProcedure());
		when(params.get("ds-name")).thenReturn("dsPrefix");
		
		result = monitor.poll(svc,params);
		assertEquals("Up",result.getStatusName());
		assertTrue(serverSetup.getProgramWasCalled());
		
	}
	
	@Test
	public void testPingDown() throws IOException {
		usePortmapper=false;
		when(params.get("use-portmapper")).thenReturn(Boolean.toString(usePortmapper));
		when(params.get("program-port")).thenReturn(serverSetup.getProgramBadPort());
		when(params.get("program-number")).thenReturn(serverSetup.getProgramNumber());
		when(params.get("program-version")).thenReturn(serverSetup.getProgramVersion());
		when(params.get("program-monitor-operation")).thenReturn(serverSetup.getProgramProcedure());
		when(params.get("ds-name")).thenReturn("dsPrefix");
		
		result = monitor.poll(svc,params);
		assertEquals("Down",result.getStatusName());
		assertEquals("Connection refused",result.getReason());
	}
	
	@Test
	public void testPortMapperPingUp() throws IOException {
		usePortmapper=true;
		when(params.get("use-portmapper")).thenReturn(Boolean.toString(usePortmapper));
		when(params.get("portmap-port")).thenReturn(serverSetup.getPortmapPort());
		when(params.get("portmap-transport")).thenReturn(serverSetup.getPortmapTransport());

		
		when(params.get("program-transport")).thenReturn(serverSetup.getProgramTransport());
		when(params.get("program-number")).thenReturn(serverSetup.getProgramNumber());
		when(params.get("program-version")).thenReturn(serverSetup.getProgramVersion());
		when(params.get("program-monitor-operation")).thenReturn(serverSetup.getProgramProcedure());
		when(params.get("ds-name")).thenReturn("dsPrefix");
		
		result = monitor.poll(svc,params);
		assertEquals("Up",result.getStatusName());
	}
	
	@Test
	public void testPortMapperPingDownPortmapDown() throws IOException {
		usePortmapper=true;
		when(params.get("use-portmapper")).thenReturn(Boolean.toString(usePortmapper));
		when(params.get("portmap-port")).thenReturn(serverSetup.getPortmapBadPort());
		when(params.get("portmap-transport")).thenReturn(serverSetup.getPortmapTransport());

		
		when(params.get("program-transport")).thenReturn(serverSetup.getProgramTransport());
		when(params.get("program-number")).thenReturn(serverSetup.getProgramNumber());
		when(params.get("program-version")).thenReturn(serverSetup.getProgramVersion());
		when(params.get("program-monitor-operation")).thenReturn(serverSetup.getProgramProcedure());
		when(params.get("ds-name")).thenReturn("dsPrefix");
		
		result = monitor.poll(svc,params);
		assertEquals("Unable to contact portmap: Exception java.net.ConnectException: Connection refused",result.getReason());
		assertEquals("Down",result.getStatusName());
	}
	

	
	@Test
	public void testPortMapperPingDownProgramBadNumber() throws IOException {
		usePortmapper=true;
		when(params.get("use-portmapper")).thenReturn(Boolean.toString(usePortmapper));
		when(params.get("portmap-port")).thenReturn(serverSetup.getPortmapPort());
		when(params.get("portmap-transport")).thenReturn(serverSetup.getPortmapTransport());

		
		when(params.get("program-transport")).thenReturn(serverSetup.getProgramTransport());
		when(params.get("program-number")).thenReturn(serverSetup.getProgramBadNumber());
		when(params.get("program-version")).thenReturn(serverSetup.getProgramVersion());
		when(params.get("program-monitor-operation")).thenReturn(serverSetup.getProgramProcedure());
		when(params.get("ds-name")).thenReturn("dsPrefix");
		
		result = monitor.poll(svc,params);
		assertEquals("Unable to contact portmap: Exception Program not found in portmap. Program number: 2, version: 1, transport: tcp",result.getReason());
		assertEquals("Down",result.getStatusName());
	}

	/**
	 * 
	 * @throws IOException
	 */
	@Test
	public void testPortMapperPingDown() throws IOException {
		usePortmapper=true;
		serverSetup.stopProgram();
		when(params.get("use-portmapper")).thenReturn(Boolean.toString(usePortmapper));
		when(params.get("portmap-port")).thenReturn(serverSetup.getPortmapPort());
		when(params.get("portmap-transport")).thenReturn(serverSetup.getPortmapTransport());

		
		when(params.get("program-transport")).thenReturn(serverSetup.getProgramTransport());
		when(params.get("program-number")).thenReturn(serverSetup.getProgramNumber());
		when(params.get("program-version")).thenReturn(serverSetup.getProgramVersion());
		when(params.get("program-monitor-operation")).thenReturn(serverSetup.getProgramProcedure());
		when(params.get("ds-name")).thenReturn("dsPrefix");
		
		result = monitor.poll(svc,params);
		assertEquals("Connection refused",result.getReason());
		assertEquals("Down",result.getStatusName());
	}

}
