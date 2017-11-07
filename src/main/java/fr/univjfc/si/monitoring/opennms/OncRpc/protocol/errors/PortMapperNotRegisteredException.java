package fr.univjfc.si.monitoring.opennms.OncRpc.protocol.errors;

import fr.univjfc.si.monitoring.opennms.protocol.OncRpc.OncRpcOnmsClient;

public class PortMapperNotRegisteredException extends PortMapperPollException {
    private static final long serialVersionUID = -920135941432840968L;

    public PortMapperNotRegisteredException(OncRpcOnmsClient oncRpcPortmapClient, int programNumber, int targetVersion, String transport) {
		super(
				new StringBuilder("Program not found in portmap. Program number: ")
				.append(oncRpcPortmapClient.getProgramNumber())
				.append(", version: ")
				.append(oncRpcPortmapClient.getProgramVersion())
				.append(", transport: ")
				.append(transport).toString()
			);
	}
}