package Learning;

import java.io.IOException;

import org.dcache.xdr.OncRpcException;
import org.dcache.xdr.RpcCall;
import org.dcache.xdr.RpcDispatchable;
import org.dcache.xdr.XdrVoid;
public class OpennmsPingTestSrv implements RpcDispatchable {
	public static final int PROGRAM_VERSION = 1;
	static final int PROGRAM_PROCEDURE = 0;
	public boolean wasCalled = false;
	@Override
	public void dispatchOncRpcCall(RpcCall call) throws OncRpcException, IOException {
        int version = call.getProgramVersion();
        System.out.println("SRV: program called "+version);
        switch(version) {
            case PROGRAM_VERSION:
                processV1Call(call);
                break;
            default:
                call.failProgramMismatch(2, 4);
        }
		
	}
	private void processV1Call(RpcCall call) {
		switch(call.getProcedure()) {
			case PROGRAM_PROCEDURE:
				wasCalled = true;
				call.reply(XdrVoid.XDR_VOID);
				break;
			default:
				call.failProcedureUnavailable();
		}
		
	}

}