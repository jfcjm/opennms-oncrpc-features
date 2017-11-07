package fr.univjfc.si.monitoring.opennms.detector.OncRpc;

import org.dcache.xdr.RpcCall;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import fr.univjfc.si.monitoring.opennms.protocol.OncRpc.OncRpcOnmsClient;
import fr.univjfc.si.monitoring.opennms.protocol.OncRpc.OncRpcOnmsClientImpl;

@Component

@Scope("prototype")
/**
 * In this subclass the (inherited) port parameter maybe
 * <ul>
 * <li>A program port if useportmapper is false</li>
 * <li>A port to contact the portmapper if useportmapper is true</li>
 * <ul>
 * 
 * @author jmk
 *
 */
public class OncRpcDetector extends BasicDetector<Object, Object> {
    private static final Logger LOG = LoggerFactory.getLogger(OncRpcDetector.class);
    /**
     * port to use if none is specified
     */
    private static int DEFAULT_PORT              = 111;
    
    /**
     * Name of the detector, by default portmap
     */
    private static String defaultServiceName    = "Portmap";
    
    /**
     * Does the client should use the postmapper to 
     * detect the program ?
     */
    private boolean     m_usePortmapper         = true;
    
    /**
     * Underlying transport to use with the portmapper
     * default to TCP
     */
    private String  m_portmapProtocolType       = "TCP";
    
    /**
     * Underlying transport to use with the program
     * default to TCP
     */
    private String  m_programTransport          = "TCP";
    
    /**
     * Program number to detext, default to rpcbind
     */
    private Integer     m_programNumber         = 10000;
    
    /**
     * program version
     */
    private Integer     m_programVersion        = 3;
    
    /**
     * Remote procedure to call, this procedure must 
     * be without parameters for now
     */
    private Integer     m_programMonitorRpc     = 0;

    
    public OncRpcDetector() {
        this(defaultServiceName, DEFAULT_PORT);
    }
    
    public OncRpcDetector(String serviceName, int port) {
        super(serviceName, port);

    }

    public void setPortmapIpProtocolType(String pType) {
        m_portmapProtocolType = pType;
    }

    public String getPortmapIpProtocolType() {
        return m_portmapProtocolType;
    }

    public void setProgramNumber(int programNumber) {
        m_programNumber = programNumber;
    }

    public int gettProgramNumber() {
        return m_programNumber;
    }

    public int getProgramVersion() {
        return m_programVersion;
    }

    public void setProgramVersion(int programVersion) {
        this.m_programVersion = programVersion;
    }

    public String getProgramTransport() {
        return m_programTransport;
    }

    public void setProgramTransport(String programTransport) {
        this.m_programTransport = programTransport;
    }

    public int getProgramMonitorRpc() {
        return m_programMonitorRpc;
    }

    public void setProgramMonitorRpc(int rpcNumber) {
        this.m_programMonitorRpc = rpcNumber;
    }

    public boolean getUsePortmapper() {
        return m_usePortmapper;
    }

    public void setUsePortmapper(boolean usesPortmapper) {
        m_usePortmapper = usesPortmapper;
    }

    @Override
    protected Client getClient() {
        Client<RpcCall, Boolean> result = new OncRpcOnmsClientImpl() {

            @Override
            public
            int getProgramNumber() {
                return m_programNumber;
            }

            @Override
            public int getProgramVersion() {
                return m_programVersion;
            }

            @Override
            public int getProgramTransport() {
                return mapTransport.get(m_programTransport);
            }

            @Override
            public int getPortmapIpProtocolType() {
                return mapTransport.get(m_portmapProtocolType);
            }

            @Override
            public int getProcedureNumber() {
                return m_programMonitorRpc;
            }

            @Override
            public boolean getUsePortmapper() {
                return m_usePortmapper;
            }

        };
        return result;
    }

    @Override
    protected void onInit() {
        if (m_usePortmapper){
            onInitPortWithMapper();
        }
    }

    private void onInitPortWithMapper() {
        if ((null == m_portmapProtocolType)){
            LOG.warn("Using portmap/rpcbind: portMapProtocolType should be specified ");
        }
        
        
    }

}
