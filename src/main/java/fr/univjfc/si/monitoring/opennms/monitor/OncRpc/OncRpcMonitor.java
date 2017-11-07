package fr.univjfc.si.monitoring.opennms.monitor.OncRpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univjfc.si.monitoring.opennms.OncRpc.protocol.errors.PortMapperPollException;
import fr.univjfc.si.monitoring.opennms.protocol.OncRpc.OncRpcOnmsClientImpl;

import java.net.InetAddress;
import java.util.Map;

import org.dcache.xdr.OncRpcAcceptedException;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
@Distributable
public class OncRpcMonitor extends AbstractServiceMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(OncRpcMonitor.class);
	
	private static final String P_DS_PREFIX = "ds-name";
	private static final String P_PROGRAM_TRANSPORT 		= "program-transport";

	private static final String P_PROGRAM_NUMBER 			= "program-number";
	private static final String P_PROGRAM_VERSION 			= "program-version";
	private static final String P_PROGRAM_PORT 				= "program-port";
	private static final String P_PROGRAM_MONITOR_OPERATION = "program-monitor-operation";


	private static final String P_USEPORTMAP 				= "use-portmapper";

	private static final String P_PORTMAP_TRANSPORT 		= "portmap-transport";

	private static final String P_PORTMAP_PORT 				= "portmap-port";
	
	private static final int DEFAULT_RETRY = 1;
	private static final int DEFAULT_TIMEOUT = 3000;
	

	public static final String DSNAME_PROGRAM 		= "program";
	public static final String DSNAME_PORTMAPPER 	= "portMapper";
	
	
	
	
	
	
	@Override
	public PollStatus poll(MonitoredService svc, Map<String, Object> params) {
		final InetAddress ipv4Addr  = svc.getAddress();
		
		
		final boolean 	usePortmapper = ParameterMap.getKeyedBoolean(params,P_USEPORTMAP, true) ;
		LOG.debug("OncRpcMonitor will use portmapper : {}" , usePortmapper);
		final Integer   pmPort = usePortmapper ? ParameterMap.getKeyedInteger(params,P_PORTMAP_PORT, 111) : null;
		if (LOG.isDebugEnabled()){
			LOG.debug("OncRpcMonitor will {} use portmapper port : {}" , null==pmPort ? "not" : "", null==pmPort ? "":pmPort);
		}
		final String 	portMapperTransport 	= usePortmapper ? ParameterMap.getKeyedString(params, P_PORTMAP_TRANSPORT, "TCP"):null;
		if (LOG.isDebugEnabled()){
			LOG.debug("OncRpcMonitor will {} use portmapper transport : {}" , null==portMapperTransport ? "not" : "", null==portMapperTransport ? "":portMapperTransport);
		}

		final String 	programTransport 	= ParameterMap.getKeyedString(params, P_PROGRAM_TRANSPORT, "TCP");
		if (LOG.isDebugEnabled()){
			LOG.debug("OncRpcMonitor will {} use program transport : {}" , null== programTransport? "not" : "", null== programTransport? "":programTransport);
		}
		
		final Integer		programPort 		= usePortmapper ? null : ParameterMap.getKeyedInteger(params,P_PROGRAM_PORT,-1) ;
		if ((null != programPort) && (-1 == programPort)){
			LOG.warn("A program port must be defined for RPCs when not using a portmappers");
			return PollStatus.unavailable("A program port must be defined for RPCs when not using a portmappers");
		}
		if (LOG.isDebugEnabled()){
			LOG.debug("OncRpcMonitor will {} use program port : {}" , null== programPort? "not" : "", null==programPort ? "":programPort);
		}
		
		final int    	programNumber  		= ParameterMap.getKeyedInteger(params,P_PROGRAM_NUMBER, 0) ;
		final int    	programVersion 		= ParameterMap.getKeyedInteger(params,P_PROGRAM_VERSION, 0) ;
		final int 	 	programMonitorRpc 	= ParameterMap.getKeyedInteger(params,P_PROGRAM_MONITOR_OPERATION, 0);
		
		final String dsPrefix				= ParameterMap.getKeyedString(params, P_DS_PREFIX, svc.getSvcName());
		final TimeoutTracker tracker        = new TimeoutTracker(params, DEFAULT_RETRY, DEFAULT_TIMEOUT);
		
		OncRpcOnmsClientImpl client = new OncRpcOnmsClientImpl(){
			@Override
			public TrackingtepRecorder createStepRecorder(){
				return new TrackingtepRecorder(tracker,dsPrefix);
			}
			@Override
            public int getProgramVersion() {
				return programVersion;
			}

			@Override
            public int getProgramNumber() {
				return programNumber;
			}
			
			@Override
            public int getPortmapIpProtocolType(){
				return mapTransport.get(portMapperTransport);
			}

			@Override
            public int getProgramTransport() {
				return mapTransport.get(programTransport);
			}

			@Override
            public int getProcedureNumber() {
				return programMonitorRpc;
			}
			@Override
            public boolean getUsePortmapper() {
				return usePortmapper;
			}
		};
		
		PollStatus serviceStatus = PollStatus.unavailable("Starting poll");
		
		final int targetPort = usePortmapper ? pmPort : programPort;
		for (tracker.reset(); tracker.shouldRetry() && !serviceStatus.isAvailable(); tracker.nextAttempt()) {
			try {
				client.connect(ipv4Addr, targetPort, (int)tracker.getTimeoutInSeconds());
				serviceStatus = PollStatus.available();
				((TrackingtepRecorder)client.getStepRecorder()).setTotalResponseTime(tracker.elapsedTimeInMillis());
			} catch(OncRpcAcceptedException e) {
				for (StackTraceElement elt: e.getStackTrace()){
					LOG.info(elt.toString());
				}
				LOG.warn("Exception. message: {}. Please activate log info level if you want to see a stacktrace", e.getMessage());
				serviceStatus = PollStatus.unavailable(mapMessage(e.getMessage()));
			} catch (PortMapperPollException e) {
				for (StackTraceElement elt: e.getStackTrace()){
					LOG.info(elt.toString());
				}
				LOG.warn("Unable to contact portmap: Exception message: {}. Please activate log info level if you want to see a stacktrace", e.getMessage());
				serviceStatus = PollStatus.unavailable("Unable to contact portmap: Exception " +e.getMessage());
				
			} catch (Exception e) {
				for (StackTraceElement elt: e.getStackTrace()){
					LOG.info(elt.toString());
				}
				LOG.warn("Exception. message: {}. Please activate log info level if you want to see a stacktrace", e.getMessage());
				serviceStatus = PollStatus.unavailable(e.getMessage());
			} finally {
				LOG.info("responsetime : {}", ((TrackingtepRecorder)client.getStepRecorder()).m_responseTimes);
				serviceStatus.setProperties(((TrackingtepRecorder)client.getStepRecorder()).m_responseTimes);
				client.close();
			}
		}
		
		return serviceStatus;
	}



	private String mapMessage(String message) {
		if ("PROG_UNAVAIL".equals(message)){
			return "PROG_UNAVAIL error: RPC Program was not found,  because  of a wrong configured program number or else a wrong configured version number";
		} else {
			return message;
		}
	}

}
