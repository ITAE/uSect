package org.unbiquitous.games.uSect.environment;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.unbiquitous.uos.core.UOS;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Call.ServiceType;
import org.unbiquitous.uos.core.messageEngine.messages.Response;

public class PhotoManager {
	protected UOS appContext;
	
	public void inputFile(){
		appContext = new UOS();
		Response resp = new Response();
		DataInputStream input;
		DataOutputStream output;
		
		Call sCall = new Call();
		sCall.setDriver("StreamDriver");
		sCall.setServiceType(ServiceType.STREAM);
		sCall.setChannels(1);
		
		input = resp.getMessageContext().getDataInputStream(0);
		
		output = resp.getMessageContext().getDataOutputStream(0);
		
		
		

		
		
		
	}
}
