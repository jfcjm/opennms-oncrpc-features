package fr.univjfc.si.monitoring.opennms.protocol.OncRpc;

/**
 * Actions to realize after we got a response when trying to reach
 * an ONCRPC service.
 * @author jmk
 *
 */
public interface AbstractStepRecorder {
    /**
     * Initialization
     */
	public abstract void init() ;
	/*
	 * record that we had a response from  portmap/rpcbind
	 */
	public abstract void gotPortmapResponse();
	/*
	 * record that we got a response from the program
	 */
	public abstract void gotProgramResponse() ;
}