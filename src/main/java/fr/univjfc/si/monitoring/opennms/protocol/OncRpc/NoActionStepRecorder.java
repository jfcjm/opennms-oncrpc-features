package fr.univjfc.si.monitoring.opennms.protocol.OncRpc;
/**
 * Default step recorder : does nothing
 * @author jmk
 *
 */
class NoActionStepRecorder implements AbstractStepRecorder{
	@Override
	public void init() {
		//No Op
	}

	@Override
	public void gotPortmapResponse() {
		//No Op
	}

	@Override
	public void gotProgramResponse() {
		//No Op
	}
	
}