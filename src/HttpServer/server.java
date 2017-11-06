package HttpServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class server {
	

	public static void main(String[] args) {
		ServerSocket soc;
		try {
			soc = new ServerSocket(8080);
			while(true)
	        {
	            Socket inSoc=soc.accept();
	            MuHttpServer request=new MuHttpServer(inSoc);
	            Thread t = new Thread(request);
	            t.start();
	            //request.process();
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
	}
}
