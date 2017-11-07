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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

class RemoteFileManager {

	public static String working_dir = System.getProperty("user.dir");

	static String HandleGET(String filePath, MuMessageHeader header) {
		String outputBody = "";
		MuHttpResponse response;
		if (filePath.equals("/")) {
			File folder = new File(working_dir);
			File[] listOfFiles = folder.listFiles();
			
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					outputBody += listOfFiles[i].getName() + "\r\n";
				} else if (listOfFiles[i].isDirectory()) {
					outputBody += listOfFiles[i].getName() + "\r\n";
				}
			}
			response = new MuHttpResponse("200 OK","text/html",outputBody);
		} else {
			File file = new File(working_dir+filePath);
			if(file.exists()) {
				//Return
				outputBody +=  readFile(working_dir+filePath);
				if(!outputBody.equals("null"))
					response = new MuHttpResponse("200 OK","text/html",outputBody);
				else
					response = new MuHttpResponse("401 Unauthorized","text/html","");
			} else {
				//Not Found
				response = new MuHttpResponse("404 Not Found","text/html",outputBody);
			}
		}
		return response.GetResponseString(header.getHeaderValue("Content-type"));
	}
	
	static String HandlePOST (String filePath, MuMessageHeader header, String Body) {
		String outputBody = "";
		MuHttpResponse response;
		try {
		if (filePath.equals("/")) {
			response = new MuHttpResponse("400 Bad Request","text/html",outputBody);
		} else {
			File file = new File(working_dir+filePath);
			if(!file.exists()) {
				file.createNewFile();
			}
			boolean overWrite = header.getHeaderValue("Overwrite") == null ? false : true;
			writeFile(working_dir+filePath,Body,overWrite);
			response = new MuHttpResponse("200 Ok","text/html",outputBody);
		}
		} catch (Exception e) {
			e.printStackTrace();	
			response = new MuHttpResponse("500 Internal Server Error","text/html",outputBody);
		}
		return response.GetResponseString(header.getHeaderValue("Content-type"));
	}
	
	private static String readFile(String filename) {
		String content = null;
		File file = new File(filename);
		FileReader reader = null;
		try {
			reader = new FileReader(file);
			char[] chars = new char[(int) file.length()];
			reader.read(chars);
			content = new String(chars);
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					content = null;
				}
			}
		}
		return content;
	}
	
	private static void writeFile(String filename, String content, boolean isNotOverwrite) {
		File file = new File(filename);
		FileWriter writer = null;
		try {
			writer = new FileWriter(file, isNotOverwrite);
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
