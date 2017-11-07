package fr.univjfc.si.monitoring.opennms.OncRpc.protocol.errors;

public class PortMapperPollException extends Exception {
    private static final long serialVersionUID = 8193651162762163649L;
    
	public PortMapperPollException(Exception e) {
		super(e);
	}

    public PortMapperPollException(String message) {
        super(message);
    }
}