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
package HttpServer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MuHttpServer implements Runnable {
	private final static String CRLF = "\r\n";
	DatagramSocket sock;
	DatagramPacket packet;
	public MuMessageHeader header;
	public String MuMethod = null;
	public String reqFile = "/";
	public String Body = "";
	boolean isVerbose = false;
	// String working_dir = null;

	/// ARQ Params
	long mySeqNum = 1000L;
	long clientSeqNum = 0l;
	int[] sendWindow = new int[4];
	int[] recvWindow = new int[4];
	boolean isHandShake = true;

	public MuHttpServer(DatagramSocket sock, DatagramPacket packet, String working_dir, boolean isVerb) {
		this.sock = sock;
		this.packet = packet;
		this.isVerbose = isVerb;
		RemoteFileManager.working_dir = working_dir;

		if (isVerbose) {
			System.out.println("Incoming Request - Thread Created...");
		}
	}

	public boolean doHandShake(Packet pr) throws Exception {
		InetAddress address = InetAddress.getByName("localhost");

		// Response from Server
		
		if (pr.getType() == 1)
			this.clientSeqNum = pr.getSequenceNumber();
		else
			return false;

		// Send Last ACK
		Packet resp = pr.toBuilder().setPayload(new byte[0]).setSequenceNumber(mySeqNum).create();

		packet = new DatagramPacket(resp.toBytes(), resp.toBytes().length, address, 3000);
		this.sock = new DatagramSocket();
		this.sock.send(packet);

		byte[] buf = new byte[1024];
		DatagramPacket packet 
          = new DatagramPacket(buf, buf.length);
        sock.receive(packet);
        Packet pAck = Packet.fromBytes(packet.getData());
        if(pAck.getType() == 2)
        	return true;
        else 
        	return false;
		// Everything done.
	}
	
	private long getNextSeqNumber() {
		if(this.mySeqNum >= 7l){
			this.mySeqNum =  0l;
		} else {
			this.mySeqNum++;
		}
		return this.mySeqNum;
	}

	public void run() {

		try {

			header = new MuMessageHeader();
			Packet resp = null;

			byte[] buf = new byte[1024];

			// int g = inputStream.available();
			// InetAddress address = packet.getAddress();
			InetAddress address = InetAddress.getByName("localhost");
			Packet p = Packet.fromBytes(packet.getData());
			
			boolean rsp = doHandShake(p);
			if(rsp) {
				System.out.println("Handshake Successful.");
				isHandShake=false;
			} else {
				System.out.println("Handshake FAILED.");
			}
			
			while(isHandShake == false) {
				header = new MuMessageHeader();
				buf = new byte[1024];
				DatagramPacket packet 
	              = new DatagramPacket(buf, buf.length);
	            sock.receive(packet);
	            
				
				p = Packet.fromBytes(packet.getData());
				
				String received = new String(p.getPayload(), 0, p.getPayload().length);

				// sock.send(packet);

				// byte[] buffer = new byte[g];
				// inputStream.read(buffer);

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
					String op = RemoteFileManager.HandleGET(this.reqFile, header);
				
					resp = p.toBuilder().setPayload(op.getBytes("UTF-8")).create();
					if (isVerbose) {
						System.out.println(op);
					}
					break;
				case "POST":
					String op2 = RemoteFileManager.HandlePOST(this.reqFile, header, Body.trim());
					
					resp = p.toBuilder().setPayload(op2.getBytes("UTF-8")).create();
					if (isVerbose) {
						System.out.println(op2);
					}
					break;
				default:

					break;
				}
				//
				// buffOp.flush();
				packet = new DatagramPacket(resp.toBytes(), resp.toBytes().length, address, 3000);
				// new DatagramPacket(p.toBytes(), p.toBytes().length, address,
				// 3000);
				sock.send(packet);
				// sock.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
