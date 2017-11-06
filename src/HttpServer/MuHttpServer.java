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

	public MuHttpServer(Socket sock) {
		this.sock = sock;
	}
	public void run() {
		try {
			BufferedInputStream inputStream = new BufferedInputStream(this.sock.getInputStream());
			OutputStream outputStream = this.sock.getOutputStream();
			BufferedOutputStream buffOp = new BufferedOutputStream(outputStream);
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
			
			String StatusLine="HTTP/1.0 200 Not Found\r\n";
			String ContentTypeLine="Content-type: text/html\r\n";
			
			String res = StatusLine + ContentTypeLine;
			buffOp.write(StatusLine.getBytes("UTF-8"));
			buffOp.write("Content-Length: 0\r\n".getBytes());
			buffOp.write("\r\n ontent-Length: 22".getBytes());
			/*buffOp.write(CRLF.getBytes());
			buffOp.write(("Location: http://localhost"+reqFile).getBytes());
			buffOp.write(CRLF.getBytes());
			buffOp.write("Connection: close".getBytes());
			buffOp.write(CRLF.getBytes());
			buffOp.write(("Date: "+new Date().toString()).getBytes());
			buffOp.write(CRLF.getBytes());
			buffOp.write("Server: MuHttpServer".getBytes());
			buffOp.write(CRLF.getBytes());*/
			buffOp.write(ContentTypeLine.getBytes("UTF-8"));

			buffOp.flush();
			//buffOp.close();
			sock.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
