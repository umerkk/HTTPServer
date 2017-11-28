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

import java.util.ArrayList;
import java.util.Arrays;

import com.google.gson.Gson;

public class MuHttpResponse {

	protected String httpVersion = "HTTP/1.0";
	protected String responseCode = "200 OK";
	protected String ContentType = "";
	protected String body = "";
	protected String Server = "MuHttpServer RemoteFileManager 1.0";
	private final static String CRLF = "\r\n";

	public String getHttpVersion() {
		return httpVersion;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public MuHttpResponse(String StatusCode, String ContentType, String Body) {
		this.responseCode = StatusCode;
		this.ContentType = ContentType;
		this.body = Body;
	}

	public String GetResponseString(String contentype) {
		String op = "";

		op += httpVersion + " ";
		op += responseCode + CRLF;

		op += "Content-type: " + ContentType + CRLF;
		op += "Content-length: " + body.length() + CRLF;
		op += "Connection: close" + CRLF;
		op += "Server: " + Server + CRLF;
		op += CRLF;
		//op += body;
		if (contentype != null) {
			switch (contentype) {
			case "application/json":
				Gson parser = new Gson();
				op += parser.toJson(body);
				break;

			default:
				op += body;

			}
		} else {
			op += body;
		}
		return op;
	}
	
	public ArrayList<String> GetResponsePackets(String contentype) throws Exception {
		String op = "";
		ArrayList<String> output = new ArrayList<String>();
		String bodyTxt = "";

		op += httpVersion + " ";
		op += responseCode + CRLF;

		MuMessageHeader hd = new MuMessageHeader();
		hd.addHeader("Content-type", ContentType);
		
		hd.addHeader("Connection", "Close");
		hd.addHeader("Content-length", String.valueOf(body.length()));
		hd.addHeader("Server", Server);
		
		
		//op += "Content-type: " + ContentType + CRLF;
		//op += "Content-length: " + body.length() + CRLF;
		//op += "Connection: close" + CRLF;
		//op += "Server: " + Server + CRLF;
		//op += CRLF;
		
		int payloadSize = 1013 - (op.getBytes().length + (hd.toString() + CRLF).getBytes().length);
		
		if (contentype != null) {
			switch (contentype) {
			case "application/json":
				Gson parser = new Gson();
				bodyTxt = parser.toJson(body);
				break;
			default:
				bodyTxt = body;
			}
		} else {
			bodyTxt = body;
		}
		byte[][] dataChunks = splitBytes(bodyTxt.getBytes(), payloadSize);
		for(int k=0;k<dataChunks.length;k++) {
			hd.removeHeader("Content-length");
			hd.addHeader("Content-length", String.valueOf(new String(dataChunks[k]).length()));
			output.add(op + hd.toString() + CRLF + new String(dataChunks[k]));
		}
		
		return output;
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

}
