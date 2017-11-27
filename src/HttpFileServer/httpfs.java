/******************************************
 * ______________COMP6461__________________
 * _Data Communication & Computer Networks_
 * 
 *			  Assignment # 3
 * 
 *____________Submitted By_________________
 *		  Muhammad Umer (40015021)
 * 	  Reza Morshed Behbahani (40039400)
 * 
 ******************************************/
package HttpFileServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

import HttpServer.MuHttpServer;

public class httpfs {

	public static void main(String[] args) {

		String working_dir = System.getProperty("user.dir");
		int port = 8080;
		boolean isVerbose = false;
		final String CRLF = "\r\n";

		for (int k = 0; k < args.length; k++) {
			if (args[k].equalsIgnoreCase("-v")) {
				isVerbose = true;
			}

			if (args[k].equalsIgnoreCase("-p")) {
				// Header is followed on next iteration.
				String headerDump = args[k + 1];
				port = Integer.parseInt(headerDump);
			}

			if (args[k].equalsIgnoreCase("-d")) {
				working_dir = args[k + 1];
			}
		}

		DatagramSocket soc;
		try {
			soc = new DatagramSocket(port);
			while (true) {
				byte[] buf = new byte[1024];
				
				DatagramPacket packet 
	              = new DatagramPacket(buf, buf.length);
	            soc.receive(packet);
	            
	            
				//Socket inSoc = soc.accept();
				MuHttpServer request = new MuHttpServer(soc, packet, working_dir, isVerbose);
				Thread t = new Thread(request);
				t.start();
				// request.process();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}