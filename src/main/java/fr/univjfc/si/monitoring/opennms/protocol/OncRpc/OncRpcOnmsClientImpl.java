package fr.univjfc.si.monitoring.opennms.protocol.OncRpc;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.dcache.utils.net.InetSocketAddresses;
import org.dcache.xdr.IpProtocolType;
import org.dcache.xdr.OncRpcClient;
import org.dcache.xdr.RpcAuth;
import org.dcache.xdr.RpcAuthTypeNone;
import org.dcache.xdr.RpcCall;
import org.dcache.xdr.XdrTransport;
import org.dcache.xdr.XdrVoid;
import org.dcache.xdr.portmap.GenericPortmapClient;
import org.dcache.xdr.portmap.OncPortmapClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univjfc.si.monitoring.opennms.OncRpc.protocol.errors.PortMapperNotRegisteredException;
import fr.univjfc.si.monitoring.opennms.OncRpc.protocol.errors.PortMapperPollException;

/**
 * OncRpc Client helping to detect and then monitor ONCRPC services
 * @author jmk
 *
 */
public abstract class OncRpcOnmsClientImpl implements OncRpcOnmsClient<RpcCall,Boolean>{
    private static final Logger LOG = LoggerFactory.getLogger(OncRpcOnmsClientImpl.class);
    private static final AbstractStepRecorder defaultRecorder = new NoActionStepRecorder();
    protected static final HashMap<String,Integer> mapTransport = new HashMap<String,Integer>();
	static {
		mapTransport.put("TCP",IpProtocolType.TCP);
		mapTransport.put("UDP",IpProtocolType.UDP);
		mapTransport.put("tcp",IpProtocolType.TCP);
		mapTransport.put("udp",IpProtocolType.UDP);
	}

    private AbstractStepRecorder m_stepRecorder;
	
	private OncRpcClient m_rpcClient = null;
	
	
	public OncRpcOnmsClientImpl(){
         m_stepRecorder = createStepRecorder();
    }
	
	@Override
	public void close() {
		if (m_rpcClient != null){
			try {
				m_rpcClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	

	protected AbstractStepRecorder createStepRecorder() {
		return defaultRecorder;
	}
	
	public AbstractStepRecorder getStepRecorder(){
		return m_stepRecorder;
	}
	
	@Override
	public void connect(InetAddress inetAddr, int port, int timeOut) throws IOException, Exception {
			LOG.debug("Connect start");
			m_stepRecorder.init();
			InetSocketAddress programInetAddress = null;
			if (getUsePortmapper()) {
				try {
					LOG.info("Using Portmapper on port" + port );
					programInetAddress = contactPortMapper(inetAddr,port,timeOut);
					m_stepRecorder.gotPortmapResponse();
					LOG.info("Found program {} in Portmap",programInetAddress);
				} catch(PortMapperPollException e){
					throw e;
				} catch (Exception e){
					throw new PortMapperPollException(e);
				}
			} else {
				LOG.info("Skipping Portmapper step, connect directly to " + port );
				programInetAddress = new InetSocketAddress(inetAddr,port);
			}
			try {
				connect(inetAddr, programInetAddress, port, timeOut);
				m_stepRecorder.gotProgramResponse();
			} catch (Exception e){
				LOG.warn("Exception. message: {}", e.getMessage());
				throw(e);
			}
	}
	
			
	
	
	public InetSocketAddress contactPortMapper(InetAddress inetAddr, int port, int timeOut) throws IOException, Exception {
		LOG.info("Trying to connect to portmapper@{} on port {} with timeout {} seconds", inetAddr, port, timeOut);
		
		m_rpcClient = new OncRpcClient(inetAddr, getPortmapIpProtocolType(), port);
		
		final XdrTransport transport = m_rpcClient.connect(timeOut, TimeUnit.SECONDS);
		final OncPortmapClient portmapClient = new GenericPortmapClient(transport);
		final String transportAsString = IpProtocolType.toString(getProgramTransport());
		LOG.debug("Asking for targetProgram: {}, targetVersion: {},  targetransport: {}", 
				getProgramNumber(),
				getProgramVersion(), 
				transportAsString);

		final String programdAddress = portmapClient.getPort(getProgramNumber(), getProgramVersion(), transportAsString);

		
		
		m_rpcClient.close();
		
		InetSocketAddress address = null;
		
		try {
			LOG.debug("Found address {} in portmap",programdAddress);
			address = InetSocketAddresses.forUaddrString(programdAddress);
			if (0 == address.getPort()) {
				throw new PortMapperNotRegisteredException(this, getProgramNumber(),getProgramVersion(),transportAsString);
			}
			LOG.debug("Port Number is " + address.getPort());
		} catch (IllegalArgumentException e){
			throw new IOException("portmap returned an invalid address for  : '" + programdAddress+"'" + "on host " + inetAddr);
		}
		
		return address;
	}
	
	

	public void connect(InetAddress inetAddr, InetSocketAddress inetSoAddr, int port, int timeOut) throws IOException, Exception {
		//TODO process timeout values more accurately
		LOG.info("Trying to connect to proc {} of program {} (version {}) @{} on port {} with timeout {} seconds", 
				getProcedureNumber(),getProgramNumber(),getProgramVersion(),inetAddr, port, timeOut);
		try {
				
				InetSocketAddress address = new InetSocketAddress(inetAddr, inetSoAddr.getPort());
				m_rpcClient = new OncRpcClient(address, getProgramTransport());
				
				RpcAuth auth = new RpcAuthTypeNone();
				XdrTransport transport = m_rpcClient.connect(timeOut, TimeUnit.SECONDS); // should be timeOut - current op' time
				
				final RpcCall m_call = new RpcCall(getProgramNumber(), getProgramVersion(), auth, transport);
				Boolean result = sendRequest(m_call);
				if (! result){
					throw new Exception("Result of call is false");
				}
				LOG.info("Connected to inetAddr {} on port {} with timeout {}", inetAddr, port, timeOut);
		} 
		 catch (Exception e){
			 LOG.warn("An exception occurred" + e);
			 throw e;
		 }
		finally  {
			if (m_rpcClient != null)
				m_rpcClient.close();
		}
	}


	
	@Override
	public Boolean receiveBanner() throws IOException, Exception {
		return null;
	}

	@Override
	public Boolean sendRequest(RpcCall call) throws IOException, Exception {
		LOG.info("Call the monitor rpc " + getProcedureNumber()) ;
		call.call(getProcedureNumber(), XdrVoid.XDR_VOID,XdrVoid.XDR_VOID);
		return Boolean.TRUE;
	}


	

}
