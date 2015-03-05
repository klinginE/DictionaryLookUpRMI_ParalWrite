package dictionaryLookUpRMI_ParalWrite;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

public class MyRMIsocketFactory extends RMISocketFactory {

	private static final int PREFERED_PORT = 1234;

	@Override
	public ServerSocket createServerSocket(int port) throws IOException {

		if (port == 0)
			return new ServerSocket(PREFERED_PORT);
		return super.getDefaultSocketFactory().createServerSocket(port);

	}

	@Override
	public Socket createSocket(String host, int port) throws IOException {

		if (port == 0)
			return new Socket(host, PREFERED_PORT);
		return super.getDefaultSocketFactory().createSocket(host, port);

	}

}
