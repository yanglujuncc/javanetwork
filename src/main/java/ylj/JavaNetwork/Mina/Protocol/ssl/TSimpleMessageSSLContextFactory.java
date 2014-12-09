package ylj.JavaNetwork.Mina.Protocol.ssl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import ylj.JavaNetwork.Mina.MySSLTrustManagerFactory;
import ylj.JavaNetwork.Mina.MySslContextFactory;

public class TSimpleMessageSSLContextFactory {
	/**
	 * @param args
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	public static KeyManager[] getServerKeyManagers()throws Exception{
		
		String keyName = "serverkey";
		char[] keyStorePwd = "123456".toCharArray();
		char[] keyPwd = "123456789".toCharArray();
		
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		InputStream in =TSimpleMessageSSLContextFactory.class.getResourceAsStream(keyName);
		keyStore.load(in, keyStorePwd);
		in.close();

		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(keyStore, keyPwd);

		return kmf.getKeyManagers();
	}
	
	public static TrustManager[] getServerTrustManagers() throws Exception{
		
		String keyName = "serverkey";
		char[] keyStorePwd = "123456".toCharArray();
	
		KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
		InputStream in =TSimpleMessageSSLContextFactory.class.getResourceAsStream(keyName);
		ts.load( in, keyStorePwd);
		in.close();
		
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(ts);		
	
		return tmf.getTrustManagers();
	}
	
	public static SSLContext createServerSslContext() throws Exception {
		X509TrustManager X509 = new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
			}

			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		};
		
		// Create keystore
		SSLContext context = SSLContext.getInstance("SSL");
		context.init(getServerKeyManagers(), new TrustManager[] { X509 }, new SecureRandom());
		return context;
	}
	public static SSLContext createServerSslContext2() throws Exception {
	
		
		// Create keystore
		SSLContext context = SSLContext.getInstance("SSL");
		context.init(getServerKeyManagers(), null, new SecureRandom());
		return context;
	}
	public static SSLContext createClientSslContext() throws Exception {
	
		// Create keystore
		SSLContext context = SSLContext.getInstance("SSL");
		context.init(null, getServerTrustManagers(), new SecureRandom());
		
	
		return context;
	}

	public static void main(String[] args) throws Exception {
	
	
	}
}
