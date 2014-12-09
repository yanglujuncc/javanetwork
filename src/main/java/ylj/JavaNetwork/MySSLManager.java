package ylj.JavaNetwork;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class MySSLManager {
	public static KeyManager[] getClientKeyManagers()throws Exception{
		String keyName = "mykey";
		char[] keyStorePwd = "123456".toCharArray();
		char[] keyPwd = "123456789".toCharArray();
		
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		InputStream in =new FileInputStream(keyName);
		keyStore.load(in, keyStorePwd);
		in.close();

		// 初始化key manager factory
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(keyStore, keyPwd);

		return kmf.getKeyManagers();
	}
	
	public static TrustManager[] getClientTrustManagers() throws Exception{
		String path = "mykey";
		char[] password = "123456".toCharArray();

		KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
		ts.load(new FileInputStream(path), password);
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		tmf.init(ts);
		
		TrustManager[] tms = tmf.getTrustManagers();
		return tms;
	}
	
	public static KeyManager[] getServerKeyManagers()throws Exception{
		String keyName = "serverkey";
		char[] keyStorePwd = "123456".toCharArray();
		char[] keyPwd = "123456789".toCharArray();
		
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		InputStream in =new FileInputStream(keyName);
		keyStore.load(in, keyStorePwd);
		in.close();

		// 初始化key manager factory
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(keyStore, keyPwd);

		return kmf.getKeyManagers();
	}
	
	public static TrustManager[] getServerTrustManagers() throws Exception{
		  String path = "serverkey";
		  char[] password = "123456".toCharArray();

		KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
		ts.load(new FileInputStream(path), password);
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		tmf.init(ts);
		
		TrustManager[] tms = tmf.getTrustManagers();
		return tms;
	}
	public static TrustManager[] getTrustKey509() throws Exception{
		 X509TrustManager x509m = new X509TrustManager() {  
			  
		        @Override  
		        public java.security.cert.X509Certificate[] getAcceptedIssuers() {  
		            return null;  
		        }  
		  

				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws CertificateException {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws CertificateException {
					// TODO Auto-generated method stub
					
				}  
		    };
		 return new TrustManager[] {x509m };
	}
	
	public static void main(String[] args) throws Exception{
		System.out.println("KeyManagerFactory.getDefaultAlgorithm():"+KeyManagerFactory.getDefaultAlgorithm());
		System.out.println("KeyStore.getDefaultType:"+KeyStore.getDefaultType());
		KeyManager[] keyManagers=getServerKeyManagers();
		
		System.out.println("get key manage success");
	}
}
