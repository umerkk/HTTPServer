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
import java.net.Socket;
import java.util.Date;

public class MuHttpServer implements Runnable {
	private final static String CRLF = "\r\n";
	Socket sock;
	public MuMessageHeader header;
	public String MuMethod = null;
	public String reqFile = "/";
	public String Body = "";
	boolean isVerbose = false;
	//String working_dir = null;

	public MuHttpServer(Socket sock, String working_dir, boolean isVerb) {
		this.sock = sock;
		this.isVerbose = isVerb;
		RemoteFileManager.working_dir = working_dir;
		
		if(isVerbose) {
			System.out.println("Incoming Request - Thread Created...");
		}
	}

	public void run() {
		BufferedInputStream inputStream;
		OutputStream outputStream;
		BufferedOutputStream buffOp;
		try {
			inputStream = new BufferedInputStream(this.sock.getInputStream());
			outputStream = this.sock.getOutputStream();
			buffOp = new BufferedOutputStream(outputStream);
			header = new MuMessageHeader();

			int g = inputStream.available();

			byte[] buffer = new byte[g];
			inputStream.read(buffer);

			String[] req = new String(buffer, "UTF-8").split(CRLF);
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
				buffOp.write(op.getBytes("UTF-8"));
				if(isVerbose) {
					System.out.println(op);
				}
				break;
			case "POST":
				String op2 = RemoteFileManager.HandlePOST(this.reqFile,header,Body);
				buffOp.write(op2.getBytes("UTF-8"));
				if(isVerbose) {
					System.out.println(op2);
				}
				break;
			default:

				break;
			}
			buffOp.flush();
			sock.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
