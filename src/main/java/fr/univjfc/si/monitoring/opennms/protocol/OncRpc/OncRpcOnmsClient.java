package fr.univjfc.si.monitoring.opennms.protocol.OncRpc;

import org.dcache.xdr.RpcCall;
import org.opennms.netmgt.provision.support.Client;
/**
 * OncRpcOnmsClient interface. Implementing classes must define the
 * getters needed for OncRpc program
 * @author jmk
 */
public interface OncRpcOnmsClient<Call,Result> extends Client<Call,Result>{
    /**
     * @return true if the client will use a portmap/rpcbind service
     */
    boolean getUsePortmapper();
    /**
     * @return the transport that will be us ed to contact the 
     * portmap/rpcbind service
     */
    int     getPortmapIpProtocolType();
    /**
     * @return the IP transport that will be used to contact the program 
     */
    int     getProgramTransport();
    
    /**
     * return the program rpc number to poll
     * @return
     */
    int     getProgramNumber();
    /**
     * @return the version of the program to poll
     */
    int     getProgramVersion();
    /**
     * @return the procedure number to call
     */
    int     getProcedureNumber();
}
