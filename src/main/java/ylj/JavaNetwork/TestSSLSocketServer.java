package ylj.JavaNetwork;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import javax.security.cert.X509Certificate;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.xml.DOMConfigurator;

import ylj.JavaNetwork.Mina.Protocol.ssl.TSimpleMessageSSLContextFactory;

public class TestSSLSocketServer {


	public static void main(String[] args) throws Exception {
		
		// key store相关信息
		DOMConfigurator.configureAndWatch("conf/log4j.xml");
		
		KeyManager[] kms= MySSLManager.getServerKeyManagers() ;
		TrustManager[] tms=MySSLManager.getClientTrustManagers() ;
	

		// 初始化ssl context
		
		SSLContext context = SSLContext.getInstance("SSL");
		context.init(kms,tms,new SecureRandom());
		
		
		//SSLContext context =TSimpleMessageSSLContextFactory.createServerSslContext();

		// 监听和接收客户端连接
		SSLServerSocketFactory factory = context.getServerSocketFactory();				
		SSLServerSocket server = (SSLServerSocket) factory.createServerSocket(10002);
	
		System.out.println("ok");
		SSLSocket ssocket = (SSLSocket) server.accept();
		
		printSession(ssocket.getSession());
		
		System.out.println(ssocket.getRemoteSocketAddress());
		System.out.println("-----------------------");
		System.out.println("UseClientMode:"+ssocket.getUseClientMode());
		
		System.out.println("-----------------------");
		
		// 向客户端发送接收到的字节序列
		OutputStream output = ssocket.getOutputStream();

		// 当一个普通 socket 连接上来, 这里会抛出异常
		// Exception in thread "main" javax.net.ssl.SSLException: Unrecognized
		// SSL message, plaintext connection?
		InputStream input = ssocket.getInputStream();
		byte[] buf = new byte[1024];
		int len = input.read(buf);
		System.out.println("received: " + new String(buf, 0, len));
		output.write(buf, 0, len);
		output.flush();
		output.close();
		input.close();

		// 关闭socket连接
		ssocket.close();
		server.close();
	}
public static void printSession(SSLSession sslSession) throws Exception{
		
		SSLSessionContext sessionContext= sslSession.getSessionContext();
		System.out.println("Protocol:"+sslSession.getProtocol());
		System.out.println("Protocol:"+sslSession.getCipherSuite());
		System.out.println("Protocol:"+sslSession.getApplicationBufferSize());
		System.out.println("Protocol:"+sslSession.getPacketBufferSize());
		System.out.println("Protocol:"+sslSession.getPeerHost());
		System.out.println("Protocol:"+sslSession.getPeerPort());
		System.out.println("LocalPrincipal:"+sslSession.getLocalPrincipal());
		Certificate[] certificate0=sslSession.getLocalCertificates();
		if(certificate0!=null)
			System.out.println("LocalCertificates:"+certificate0.length+" "+certificate0[0].getType());
		else
			System.out.println("LocalCertificates:null");
		
	
	
		try{
			System.out.println("PeerPrincipal:"+sslSession.getPeerPrincipal());
			
		Certificate[] certificate1=sslSession.getPeerCertificates();
		X509Certificate[] certificate2=sslSession.getPeerCertificateChain();
			
		System.out.println("PeerCertificates:"+certificate1.length+","+certificate1[0].getType());
		System.out.println("PeerCertificateChain:"+certificate2.length+","+certificate2[0].getVersion());
		}catch(Exception e){
			e.printStackTrace();
		}
	
		System.out.println("-----------------------");
	}
}
