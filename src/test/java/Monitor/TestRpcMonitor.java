package Monitor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;

import fr.univjfc.si.monitoring.opennms.monitor.OncRpc.OncRpcMonitor;

@Ignore
public class TestRpcMonitor {
	private static final String TARGET_HOST = "127.0.0.1";
	private Map<String,Object> params;
	private AbstractServiceMonitor monitor;
	private MonitoredService svc;
	private PollStatus result;
	private boolean usePortmapper;
	
	@Before
	public void prepare() throws UnknownHostException {
		monitor = new OncRpcMonitor();
		svc = Mockito.mock(MonitoredService.class);
		when(svc.getAddress()).thenReturn(InetAddress.getByName(TARGET_HOST));
		when(svc.getSvcName()).thenReturn("mockSVC");
		
		//we use portmap by default
		usePortmapper=true;
	}

	
	@Test
	public void testPortmapper() throws UnknownHostException {
		params = Mockito.mock(Map.class);
		usePortmapper=false;
		when(params.get("use-portmapper")).thenReturn(Boolean.toString(usePortmapper));
		when(params.get("program-port")).thenReturn("111");
		when(params.get("program-number")).thenReturn("100000");
		when(params.get("portmap-transport")).thenReturn("UDP");
		when(params.get("program-version")).thenReturn("3");
		when(params.get("program-monitor-operation")).thenReturn("0");
		when(params.get("ds-name")).thenReturn("dsPrefix");
		
		result = monitor.poll(svc,params);
		verify(params,never()).get(ArgumentMatchers.eq("portmap-port"));
	}
	
	@Test
	public void testNfsv3() throws UnknownHostException {
		params = Mockito.mock(Map.class);
		
		when(params.get("portmap-port")).thenReturn("111");
		when(params.get("portmap-transport")).thenReturn("TCP");
		
		when(params.get("program-number")).thenReturn("100003");
		when(params.get("program-version")).thenReturn("3");
		when(params.get("program-monitor-operation")).thenReturn("0");
		when(params.get("ds-name")).thenReturn("dsPrefix");
		
		result = monitor.poll(svc,params);
		assertEquals("Up",result.getStatusName());
	}

	@Test
	public void testNfsv4() throws UnknownHostException {
		params = Mockito.mock(Map.class);
		
		when(params.get("portmap-port")).thenReturn("111");
		when(params.get("portmap-transport")).thenReturn("TCP");
		
		when(params.get("program-number")).thenReturn("100003");
		when(params.get("program-version")).thenReturn("4");
		when(params.get("program-monitor-operation")).thenReturn("0");
		when(params.get("ds-name")).thenReturn("dsPrefix");
		
		result = monitor.poll(svc,params);
		assertEquals("Down",result.getStatusName());
	}

	@Test
	public void testStatd() throws UnknownHostException {
		params = Mockito.mock(Map.class);
		
		when(params.get("portmap-port")).thenReturn("111");
		when(params.get("portmap-transport")).thenReturn("TCP");
		
		when(params.get("program-number")).thenReturn("100024");
		when(params.get("program-version")).thenReturn("1");
		when(params.get("program-monitor-operation")).thenReturn("0");
		when(params.get("ds-name")).thenReturn("dsPrefix");
		
		result = monitor.poll(svc,params);
		assertEquals("Up",result.getStatusName());
	}

	@Test
	public void testLockd_v3() throws UnknownHostException {
		params = Mockito.mock(Map.class);
		
		when(params.get("portmap-port")).thenReturn("111");
		when(params.get("portmap-transport")).thenReturn("TCP");
		
		when(params.get("program-number")).thenReturn("100021");
		when(params.get("program-version")).thenReturn("1");
		when(params.get("program-monitor-operation")).thenReturn("0");
		when(params.get("ds-name")).thenReturn("dsPrefix");
		
		result = monitor.poll(svc,params);
	}

	@Test
	public void testLockd_v4() throws UnknownHostException {
		params = Mockito.mock(Map.class);
		
		when(params.get("portmap-port")).thenReturn("111");
		when(params.get("portmap-transport")).thenReturn("TCP");
		
		when(params.get("program-number")).thenReturn("100021");
		when(params.get("program-version")).thenReturn("4");
		when(params.get("program-monitor-operation")).thenReturn("0");
		when(params.get("ds-name")).thenReturn("dsPrefix");
		
		result = monitor.poll(svc,params);
	}
	
	@After
	public void verifyCalls(){
		verify(svc).getAddress();
		verify(params).get(ArgumentMatchers.eq("program-number"));
		
		verify(params).get(ArgumentMatchers.eq("program-version"));
		if (usePortmapper){
			verify(params).get(ArgumentMatchers.eq("portmap-port"));
			verify(params).get(ArgumentMatchers.eq("portmap-transport"));
		} else {
			verify(params).get(ArgumentMatchers.eq("program-port"));
		}
		verify(params).get(ArgumentMatchers.eq("program-monitor-operation"));

		System.out.println(result.getClass());
		System.err.println(result.getProperties());
		if (usePortmapper){
			if (result.isUp()){
				assertEquals(3,result.getProperties().values().size());
				Number dsPrefixportMapper = result.getProperty("dsPrefixportMapper");
				Number dsPrefixprogram = result.getProperty("dsPrefixprogram");
				double calcTot = dsPrefixportMapper.doubleValue() + dsPrefixprogram.doubleValue();
				Number responseTime = result.getProperty("response-time");
				assertEquals(responseTime.doubleValue(),calcTot,0.1);
			}
		} else {
			assertEquals(1,result.getProperties().values().size());
		}
		
	}

}
