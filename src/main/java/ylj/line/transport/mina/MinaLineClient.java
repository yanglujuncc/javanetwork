package ylj.line.transport.mina;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import ylj.line.client.LineClient;
import ylj.line.client.LineClientCallbackConnection;
import ylj.line.client.LineClientCallbackSend;
import ylj.line.message.Message;

public class MinaLineClient extends LineClient {

	NioSocketConnector connector;
	LineClientCallbackConnection connectionCallback;
	IoSession ioSession;

	SentMsgPair sendingMsg;
	LinkedList<SentMsgPair> sendQueue;

	ConnectFuture connectFuture;
	//SentRun sendRunner;
	//Thread sendThread;

	String HOSTNAME;
	int PORT;

	class SentMsgPair {
		Message msg;
		LineClientCallbackSend callback;

		public SentMsgPair(Message msg, LineClientCallbackSend callback) {
			this.msg = msg;
			this.callback = callback;
		}
	}

	public MinaLineClient(String userName, String password) {
		super(userName, password);
		sendQueue = new LinkedList<SentMsgPair>();

	}

	public String getAddr(){
		if(ioSession==null)
			return null;
		
		InetSocketAddress saddr=(InetSocketAddress) ioSession.getLocalAddress();
		if(saddr==null)
			return null;
		
		String addr=saddr.getAddress().getHostAddress()+":"+saddr.getPort();
		return addr;
	}
	public boolean isConnect(){
		if( connectFuture==null)
			return false;
		
		
		return connectFuture.isConnected();
	}

	
	public void close(){
		connector.dispose();
	}
	@Override
	public void connect(String url, LineClientCallbackConnection callback) {

		this.connectionCallback = callback;

		int CONNECT_TIMEOUT = 10;

		connector = new NioSocketConnector();
		connector.setConnectTimeoutMillis(CONNECT_TIMEOUT);
		connector.getFilterChain().addLast("line.message.codec",new ProtocolCodecFilter(new MessageCodecFactory()));
		connector.getFilterChain().addLast("logger", new LoggingFilter());
		connector.setHandler(new ClientIOHandler());

		String tcpFlag = "tcp://";

		String remain = url;
		int tcpIdx = url.indexOf(url);
		if (tcpIdx != -1) {
			remain = url.substring(tcpIdx + tcpFlag.length());
		}

		int portIdx = remain.indexOf(":");
		String hostname = remain.substring(0, portIdx);
		String port = remain.substring(portIdx + 1);

		System.out.println("hostName:" + hostname);
		System.out.println("    port:" + port);

		 connectFuture=connector.connect(new InetSocketAddress(hostname, Integer
				.parseInt(port)));

		//if(connectFuture.)
	}

	@Override
	public void send(Message msg, LineClientCallbackSend callback) {
		
		SentMsgPair sentMsgPair = new SentMsgPair(msg, callback);
		
		synchronized(sendQueue){
			
			if(sendingMsg!=null){
				sendQueue.addLast(sentMsgPair);
			}
			else{
				sendingMsg=sentMsgPair;
				ioSession.write(sentMsgPair.msg);
			}
		}
		
			
		

	}

	

	public class ClientIOHandler implements IoHandler {

		@Override
		public void sessionCreated(IoSession session) throws Exception {
			System.out.println("sessionCreated " + session.getRemoteAddress());
		}

		@Override
		public void sessionOpened(IoSession session) throws Exception {
			System.out.println("sessionOpened " + session.getRemoteAddress());
			ioSession = session;
			connectionCallback.connected();
	
			
		}

		@Override
		public void sessionClosed(IoSession session) throws Exception {
			System.out.println("sessionClosed " + session.getRemoteAddress());

			connectionCallback.connectionLost();
			if (sendingMsg != null) {
				sendingMsg.callback.sendFailed();
			}
			for(SentMsgPair pendingSentMsg:sendQueue){
				pendingSentMsg.callback.sendFailed();
			}
			connector.dispose();
			connectFuture=null;
		}

		@Override
		public void sessionIdle(IoSession session, IdleStatus status)
				throws Exception {
			System.out.println("sessionIdle " + session.getIdleCount(status));
		}

		@Override
		public void messageReceived(IoSession session, Object message)
				throws Exception {

			System.out.println("messageReceived ");

			if (!(message instanceof Message)) {
				// not a Message obj
			} else {
				Message msg = (Message) message;
				callbackMsgReceive.messageReceived(msg);
			}

		}

		@Override
		public void messageSent(IoSession session, Object message)
				throws Exception {

			synchronized(sendQueue){
				
				if(sendQueue.size()==0){
					sendingMsg=null;
				}
				else{
					SentMsgPair firstMsgPair=sendQueue.removeFirst();
					sendingMsg=firstMsgPair;
					ioSession.write(firstMsgPair.msg);
				}
			}
			
		}

		@Override
		public void exceptionCaught(IoSession session, Throwable cause) {
			System.out.println("exceptionCaught " + session.getRemoteAddress());
			cause.printStackTrace();

			session.close(true);

		}

	}

	public static void main(String[] args) throws Exception {

		String HOSTNAME = "localhost";
		int PORT = 11112;
		int CONNECT_TIMEOUT = 100;

		String userName = "";
		String password = "";
		String url = "tcp://localhost:11111";

		LineClientCallbackConnection callback = new LineClientCallbackConnection() {
			@Override
			public void connected() {
				System.out.println("connect ok");
			}

			@Override
			public void connectionLost() {
				System.out.println("connectionLost");
			}
		};
		
		MinaLineClient minaLineClient = new MinaLineClient(userName, password);
		minaLineClient.connect(url, callback);

		for(int i=0;i<1000;i++){
			if(minaLineClient.isConnect()){
				minaLineClient.send(new Message((short)1,(short)1,("i am client "+i).getBytes()), new LineClientCallbackSend(){

					@Override
					public void sendSuccess() {
						System.out.println("send success callback");
					}

					@Override
					public void sendFailed() {
						System.out.println("send failed callback");				
					}
					
				});
			}
			Thread.sleep(1000);
		}
	}

}
