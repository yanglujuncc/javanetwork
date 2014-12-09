package ylj.line.message.transport.mina;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import ylj.line.client.LineClient;
import ylj.line.client.LineClientCallbackConnection;
import ylj.line.client.LineClientCallbackSent;
import ylj.line.message.Message;


public class MinaLineClient extends LineClient{

	NioSocketConnector connector;
	LineClientCallbackConnection connectionCallback;
	IoSession ioSession;
	
	SentMsgPair sendingMsg;
	LinkedBlockingQueue<SentMsgPair> sentQueue;
	
	SentRun sentRunner;
	Thread sentThread;
	
	class SentMsgPair{
		Message msg;
		LineClientCallbackSent callback;
		public SentMsgPair(Message msg,LineClientCallbackSent callback){
			this.msg=msg;
			this.callback=callback;
		}
	}
	
	public MinaLineClient(String userName, String password) {
		super(userName, password);
		sentQueue=new LinkedBlockingQueue<SentMsgPair>();
		sentRunner=new SentRun();
		sentThread=new Thread(sentRunner,"sent thread");
	}
	
	@Override
	public void connect(String url, LineClientCallbackConnection callback) {
		
		this.connectionCallback=callback;
		
		int CONNECT_TIMEOUT=100;
		
		connector = new NioSocketConnector();
		connector.setConnectTimeoutMillis(CONNECT_TIMEOUT);
		connector.getFilterChain().addLast("line.message.codec", new ProtocolCodecFilter(new MessageCodecFactory()));
		connector.getFilterChain().addLast("logger", new LoggingFilter());
		connector.setHandler(new ClientIOHandler());
		
	}
	
	@Override
	public void sent(Message msg, LineClientCallbackSent callback) {
		SentMsgPair sentMsgPair=new SentMsgPair(msg,callback);
		try {
			sentQueue.put(sentMsgPair);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	

	class SentRun implements Runnable{

		@Override
		public void run() {
			while(true){
				try {
					if(sendingMsg!=null)
						sentQueue.wait();
					
					sendingMsg=sentQueue.take();										
					ioSession.write(sendingMsg.msg);
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
				}
				
			}
			
		}
		
	}
	
	
	
	public  class ClientIOHandler implements IoHandler {

	
		@Override
		public void sessionCreated(IoSession session) throws Exception {
			System.out.println("create "+session.getRemoteAddress());
		}

		@Override
		public void sessionOpened(IoSession session) throws Exception {
			System.out.println("Opened "+session.getRemoteAddress());
			ioSession=session;
			connectionCallback.connected();
		}

		@Override
		public void sessionClosed(IoSession session) throws Exception {
			System.out.println("Closed "+session.getRemoteAddress());
			
			connectionCallback.connectionLost();
			if(sendingMsg!=null){
				sendingMsg.callback.sendFailed();
			}
		}

		@Override
		public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
			System.out.println("IDLE " + session.getIdleCount(status));
		}

		@Override
		public void messageReceived(IoSession session, Object message) throws Exception {
		
			System.out.println("Received ");

			if(!(message instanceof Message)){
				//not a Message obj
			}else{
				Message msg=(Message)message;
				callbackMsgReceive.messageReceived(msg);
			}
			

		}

		@Override
		public void messageSent(IoSession session, Object message) throws Exception {
			
			System.out.println("Sent ok");
			sendingMsg.notifyAll();
			sendingMsg.callback.sendSuccess();
			sendingMsg=null;
		}

		@Override
		public void exceptionCaught(IoSession session, Throwable cause) {
			System.out.println("Caught Exception "+session.getRemoteAddress());
			cause.printStackTrace();
			
			session.close(true);
			
			connectionCallback.connectionLost();
			
			if(sendingMsg!=null){
				sendingMsg.callback.sendFailed();
			}
		}

	}

	@Override
	public void start() {		
		sentThread.start();
	}

	@Override
	public void close() {	
		sentThread.interrupt();		
	}


	
	

	public static void main(String[] args) throws IOException {

		   String HOSTNAME = "localhost";
		   int PORT = 11112;
		   int CONNECT_TIMEOUT = 100;


	}



}
