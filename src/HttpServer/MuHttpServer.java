/******************************************
 * ______________COMP6461__________________
 * _Data Communication & Computer Networks_
 * 
 *			  Assignment # 2
 * 
 *____________Submitted By_________________
 *		  Muhammad Umer (40015021)
 * 	  Reza Morshed Behbahani (40039400)
 * 
 ******************************************/
package HttpServer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;

public class MuHttpServer implements Runnable {
	private final static String CRLF = "\r\n";
	DatagramSocket sock;
	DatagramPacket packet;
	public MuMessageHeader header;
	public String MuMethod = null;
	public String reqFile = "/";
	public String Body = "";
	boolean isVerbose = false;
	//String working_dir = null;

	public MuHttpServer(DatagramSocket sock, DatagramPacket packet, String working_dir, boolean isVerb) {
		this.sock = sock;
		this.packet = packet;
		this.isVerbose = isVerb;
		RemoteFileManager.working_dir = working_dir;
		
		if(isVerbose) {
			System.out.println("Incoming Request - Thread Created...");
		}
	}

	public void run() {
		/*
        */
		/*BufferedInputStream inputStream;
		OutputStream outputStream;
		BufferedOutputStream buffOp;*/
		try {
			//inputStream = new BufferedInputStream(this.sock.getInputStream());
			//outputStream = this.sock.getOutputStream();
			//buffOp = new BufferedOutputStream(outputStream);
			header = new MuMessageHeader();
			byte[] buf = new byte[2048];

			//int g = inputStream.available();
			InetAddress address = packet.getAddress();
	        int port = packet.getPort();
	        
	        String received = new String(packet.getData(), 0, packet.getLength());
	        
	        //sock.send(packet);

			//byte[] buffer = new byte[g];
			//inputStream.read(buffer);

			String[] req = received.split(CRLF);
			String[] firstLineArr = req[0].trim().split(" ");

			this.MuMethod = firstLineArr[0];
			this.reqFile = firstLineArr[1];

			boolean isBody = false;
			for (int k = 1; k < req.length; k++) {
				if (req[k].equals("")) {
					isBody = true;
					continue;
				}
				if (!isBody) {
					String[] headerVals = req[k].split(":");
					header.addHeader(headerVals[0], headerVals[1].trim());
				} else {
					Body += req[k];
				}
			}

			switch (this.MuMethod) {
			case "GET":
				String op = RemoteFileManager.HandleGET(this.reqFile,header);
				//buffOp.write(op.getBytes("UTF-8"));
				packet = new DatagramPacket(op.getBytes("UTF-8"), op.getBytes("UTF-8").length, address, port);
				if(isVerbose) {
					System.out.println(op);
				}
				break;
			case "POST":
				String op2 = RemoteFileManager.HandlePOST(this.reqFile,header,Body);
				//buffOp.write(op2.getBytes("UTF-8"));
				packet = new DatagramPacket(op2.getBytes("UTF-8"), op2.getBytes("UTF-8").length, address, port);
				if(isVerbose) {
					System.out.println(op2);
				}
				break;
			default:

				break;
			}
			//
			//buffOp.flush();
			sock.send(packet);
			//sock.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
