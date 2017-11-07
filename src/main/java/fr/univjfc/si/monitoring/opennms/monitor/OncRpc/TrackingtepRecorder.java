package fr.univjfc.si.monitoring.opennms.monitor.OncRpc;

import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.poller.PollStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univjfc.si.monitoring.opennms.protocol.OncRpc.AbstractStepRecorder;

/**
 * 
 * OncStepRecorder for monitoring an OncRpc Program.
 * 
 * This class uses a timeoutTracker to record the response times of the
 * different response steps (portmap response, program response).
 * 
 * @author jmk
 *
 */
public class TrackingtepRecorder implements AbstractStepRecorder {
    private static final Logger LOG = LoggerFactory.getLogger(TrackingtepRecorder.class);
    
    private TimeoutTracker m_tracker;
    public Map<String, Number> m_responseTimes = new HashMap<String, Number>();
    private double m_portMapElapsed;
    private String m_portmapDsName;
    private String m_programDsName;
    private String m_totalDsName;


    //Cf https://oss.oetiker.ch/rrdtool/doc/rrdcreate.en.html
    //A ds-name must be 1 to 19 characters long in the characters [a-zA-Z0-9_].
    
    private final static String  mkDsName(String prefix, String rpcDsName){
        final String    dsName  = prefix+rpcDsName;
        final int       length  = dsName.length();
        final boolean   ok      = (length <= 19) && (length  >=0);
        if (!ok) {
            LOG.warn("Length of datasource is off limit. Prefix: {}, RpcDatadource: {}, length {}", 
                    prefix,rpcDsName,length);
        }
        return dsName;
    }
    
    
    TrackingtepRecorder(TimeoutTracker tracker, String dsPrefix) {
        m_tracker = tracker;
        m_portmapDsName = mkDsName(dsPrefix, OncRpcMonitor.DSNAME_PORTMAPPER);
        m_programDsName = mkDsName(dsPrefix, OncRpcMonitor.DSNAME_PROGRAM);
        m_totalDsName = PollStatus.PROPERTY_RESPONSE_TIME;
    }

    @Override
    public void init() {
        m_tracker.startAttempt();
    }

    @Override
    public void gotPortmapResponse() {
        m_portMapElapsed = m_tracker.elapsedTimeInMillis();
        m_responseTimes.put(m_portmapDsName, m_portMapElapsed);
    }

    @Override
    public void gotProgramResponse() {
        double programElapsed = m_tracker.elapsedTimeInMillis();
        m_responseTimes.put(m_programDsName, programElapsed - m_portMapElapsed);

    }

    public void setTotalResponseTime(double d) {
        m_responseTimes.put(m_totalDsName, d);
    }
}