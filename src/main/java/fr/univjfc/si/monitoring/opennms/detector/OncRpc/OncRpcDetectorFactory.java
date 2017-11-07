package fr.univjfc.si.monitoring.opennms.detector.OncRpc;

import org.opennms.netmgt.provision.support.GenericServiceDetectorFactory;
import org.springframework.stereotype.Component;
@Component
public class OncRpcDetectorFactory extends GenericServiceDetectorFactory<OncRpcDetector>{

    public OncRpcDetectorFactory() {
        super(OncRpcDetector.class);
    }

}
