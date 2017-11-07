package Monitor;

import static org.mockito.Mockito.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;

import fr.univjfc.si.monitoring.opennms.monitor.OncRpc.OncRpcMonitor;
/**
 * This class should test the logic of OncRpcMonitor.poll against
 * the value of the  "use-portmapper" parameter
 * 
 * Result of the poll is not significative for these tests so they 
 * maybe run aven if there is no portmapper or program service  
 * running 
 * @author jmk
 *
 */
public class OncRpcMonitorPollLogic {
	private static final String TARGET_HOST = "localhost";
	private Map<String,Object> params;
	private AbstractServiceMonitor monitor;
	private MonitoredService svc;
	@Before
	public void prepare() throws UnknownHostException {
		monitor = new OncRpcMonitor();
		svc = Mockito.mock(MonitoredService.class);
		when(svc.getAddress()).thenReturn(InetAddress.getByName(TARGET_HOST));
		when(svc.getSvcName()).thenReturn("mockSVC");
	}
	@Test
	public void noPortmappertest() {
		params = Mockito.mock(Map.class);
		when(params.get("use-portmapper")).thenReturn("false");
		
		when(params.get("program-port")).thenReturn("111");
		when(params.get("program-number")).thenReturn("100000");
		when(params.get("program-version")).thenReturn("3");
		when(params.get("program-monitor-operation")).thenReturn("0");
		when(params.get("ds-name")).thenReturn("dsPrefix");
		monitor.poll(svc, params);
		verify(params,never()).get(ArgumentMatchers.eq("portmap-transport"));
		verify(params,never()).get(ArgumentMatchers.eq("portmap-port"));
		verify(params).get(ArgumentMatchers.eq("program-port"));
		verify(params).get(ArgumentMatchers.eq("program-transport"));
        verify(params).get(ArgumentMatchers.eq("program-version"));
        verify(params).get(ArgumentMatchers.eq("program-monitor-operation"));
	}
	@Test
	public void withPortmappertest() {
		params = Mockito.mock(Map.class);
		when(params.get("use-portmapper")).thenReturn("true");
		
		when(params.get("portmap-transport")).thenReturn("UDP");
		when(params.get("program-number")).thenReturn("100000");
		when(params.get("program-version")).thenReturn("3");
		when(params.get("program-monitor-operation")).thenReturn("0");
		when(params.get("ds-name")).thenReturn("dsPrefix");
        monitor.poll(svc, params);
        
		verify(params,never()).get(ArgumentMatchers.eq("program-port"));
		
        verify(params).get(ArgumentMatchers.eq("portmap-port"));
        verify(params).get(ArgumentMatchers.eq("portmap-transport"));
        
        verify(params).get(ArgumentMatchers.eq("program-transport"));
        verify(params).get(ArgumentMatchers.eq("program-version"));
        verify(params).get(ArgumentMatchers.eq("program-monitor-operation"));
        
	}

}
