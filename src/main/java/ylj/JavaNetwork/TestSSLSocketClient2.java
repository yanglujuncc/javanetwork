package ylj.JavaNetwork;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.security.cert.X509Certificate;

import org.apache.log4j.xml.DOMConfigurator;

import ylj.JavaNetwork.Mina.Protocol.ssl.TSimpleMessageSSLContextFactory;

public class TestSSLSocketClient2 {

	/**
	 * @param args
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	
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
		
		Certificate[] certificate1=sslSession.getPeerCertificates();
		X509Certificate[] certificate2=sslSession.getPeerCertificateChain();
		
		System.out.println("PeerPrincipal:"+sslSession.getPeerPrincipal());
		System.out.println("PeerCertificates:"+certificate1.length+","+certificate1[0].getType());
		System.out.println("PeerCertificateChain:"+certificate2.length+","+certificate2[0].getVersion());
		
	
		System.out.println("-----------------------");
	}
	public static void main(String[] args) throws Exception {
		DOMConfigurator.configureAndWatch("conf/log4j.xml");

		
		KeyManager[] kms= MySSLManager.getClientKeyManagers() ;
		TrustManager[] tms=MySSLManager.getServerTrustManagers() ;
		
		SSLContext context =  SSLContext.getInstance("SSL");
		context.init(kms,tms, null);
		
		//SSLContext context =TSimpleMessageSSLContextFactory.createClientSslContext();

	
		SSLSocketFactory ssf = context.getSocketFactory();
	
		SSLSocket s = (SSLSocket) ssf.createSocket("localhost", 10002);
		
		String[] enabledCipherSuites=s.getEnabledCipherSuites();
		for(int i=0;i<enabledCipherSuites.length;i++){
			System.out.println(enabledCipherSuites[i]);
		}
		System.out.println("-----------------------");
		String[] enabledCipherProtocols=s.getEnabledProtocols();
		for(int i=0;i<enabledCipherProtocols.length;i++){
			System.out.println(enabledCipherProtocols[i]);
		}
		System.out.println("-----------------------");
		System.out.println("UseClientMode:"+s.getUseClientMode());
		 printSession(s.getSession());
		System.out.println("-----------------------");
		
		
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
