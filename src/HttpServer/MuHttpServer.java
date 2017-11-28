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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;


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
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		sock.receive(packet);
		Packet pAck = Packet.fromBytes(packet.getData());
		if (pAck.getType() == 2)
			return true;
		else
			return false;
		// Everything done.
	}

	private long getNextSeqNumber() {
		if (this.clientSeqNum >= 7l) {
			this.clientSeqNum = 0l;
		} else {
			this.clientSeqNum++;
		}
		return this.clientSeqNum;
	}

	public byte[][] splitBytes(final byte[] data, final int chunkSize) {
		final int length = data.length;
		final byte[][] dest = new byte[(length + chunkSize - 1) / chunkSize][];
		int destIndex = 0;
		int stopIndex = 0;

		for (int startIndex = 0; startIndex + chunkSize <= length; startIndex += chunkSize) {
			stopIndex += chunkSize;
			dest[destIndex++] = Arrays.copyOfRange(data, startIndex, stopIndex);
		}

		if (stopIndex < length)
			dest[destIndex] = Arrays.copyOfRange(data, stopIndex, length);

		return dest;
	}

	private void sendRequest(Packet p) throws Exception {
		InetAddress address = InetAddress.getByName("localhost");

		packet = new DatagramPacket(p.toBytes(), p.toBytes().length, address, 3000);
		sock.send(packet);
	}

	private boolean recieveAck() throws Exception {
		byte[] buf = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);

		this.sock.receive(packet);

		Packet p = Packet.fromBytes(packet.getData());
		if (p.getType() == 4) {
			this.clientSeqNum = p.getSequenceNumber();
			return true;
		} else {
			return false;
		}
	}

	public void run() {

		try {

			header = new MuMessageHeader();
			Packet resp = null;

			byte[] buf = new byte[1024];

			// int g = inputStream.available();
			// InetAddress address = packet.getAddress();
			//InetAddress address = InetAddress.getByName("localhost");
			Packet p = Packet.fromBytes(packet.getData());

			boolean rsp = doHandShake(p);
			if (rsp) {
				System.out.println("Handshake Successful.");
				isHandShake = false;
			} else {
				System.out.println("Handshake FAILED.");
			}

			while (isHandShake == false) {
				header = new MuMessageHeader();
				buf = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				sock.receive(packet);
				p = null;
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
					ArrayList<String> op = RemoteFileManager.HandleGET(this.reqFile, header);
					if (op.size() <= 1) {
						resp = p.toBuilder().setPayload(op.get(0).getBytes("UTF-8")).create();
						sendRequest(resp);
						if (isVerbose) {
							System.out.println(op.get(0));
						}
					} else {
						//long size = RemoteFileManager.GetFileSizeOfRequest(this.reqFile);
						//ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
						//buffer.putLong(op.size());
						resp = p.toBuilder().setPayload(String.valueOf(op.size()).getBytes()).setType(3).create();

						sendRequest(resp);
						if (isVerbose) {
							System.out.println(op.size());
						}
						boolean isAck = recieveAck();

						if (isAck) {
							for (int k = 0; k < op.size(); k++) {
								resp = p.toBuilder().setPayload(op.get(k).getBytes("UTF-8"))
										.setSequenceNumber(getNextSeqNumber()).create();
								sendRequest(resp);
								
								if (isVerbose) {
									System.out.println(op.get(k));
								}
							}
						}
					}
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
					sendRequest(resp);
					break;
				default:

					break;
				}
				//
				// buffOp.flush();
				// packet = new DatagramPacket(resp.toBytes(),
				// resp.toBytes().length, address, 3000);
				// new DatagramPacket(p.toBytes(), p.toBytes().length, address,
				// 3000);
				// sock.send(packet);
				// sock.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
