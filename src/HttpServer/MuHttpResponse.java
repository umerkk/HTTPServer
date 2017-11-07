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

}
