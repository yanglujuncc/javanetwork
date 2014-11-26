package ylj.JavaNetwork;

import java.io.InputStream;
import java.io.OutputStream;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class TestSSLSocketClient3 {
	public static void main(String[] args) throws Exception {
		
		
		// java  -Djavax.net.ssl.trustStore=mykey 
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();  
		    SSLSocket s = (SSLSocket) factory.createSocket("localhost", 10002);  
		    System.out.println("ok");  
		  
		    OutputStream output = s.getOutputStream();  
		    InputStream input = s.getInputStream();  
		  
		    output.write("alert".getBytes());  
		    System.out.println("sent: alert");  
		    output.flush();  
		  
		    byte[] buf = new byte[1024];  
		    int len = input.read(buf);  
		    System.out.println("received:" + new String(buf, 0, len));  
		
	}
}
