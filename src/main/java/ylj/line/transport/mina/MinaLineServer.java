package ylj.line.transport.mina;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;


import ylj.line.message.Message;
import ylj.line.server.LineServer;
import ylj.line.server.LineServerCallbackAccept;
import ylj.line.server.LineServerCallbackSend;

public class MinaLineServer extends LineServer {
	
	IoAcceptor acceptor;
	LineServerCallbackAccept connectionCallback;
	

	Map<String,RemoteLinkedClient> remoteClientMap;
	
	class SentMsgPair {
	
		Message msg;
		LineServerCallbackSend callback;

		public SentMsgPair(Message msg, LineServerCallbackSend callback) {
			this.msg = msg;
			this.callback = callback;
		}
	}
	
	class RemoteLinkedClient{
		SentMsgPair sendingMsg;
		LinkedList<SentMsgPair> sendQueue;
		IoSession ioSession;
		
		
	}

	public MinaLineServer() {
	
		remoteClientMap=new HashMap<String,RemoteLinkedClient> ();
	}

	public void close(){
		acceptor.dispose();
	}


	@Override
	public void listen(int port, LineServerCallbackAccept callback) throws Exception {
		// 服务端监听端口用
		 acceptor = new NioSocketAcceptor();
		// 日志filter
		acceptor.getFilterChain().addLast("logger", new LoggingFilter());
		// 对象序列化工厂，用来将java对象序列化成二进制流
		acceptor.getFilterChain().addLast("line.message.codec", new ProtocolCodecFilter(new MessageCodecFactory()));
		// 业务处理handler
		acceptor.setHandler(new ServerIOHandler());

		acceptor.getSessionConfig().setReadBufferSize(2048);
	//	acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);

		// 设置监听端口
		acceptor.bind(new InetSocketAddress(port));

		System.out.println("Server starts to listen to PORT :" + port);

		this.connectionCallback=callback;
		
	}

	@Override
	public void send(String addr,Message msg, LineServerCallbackSend callback) {
		
		
		
		RemoteLinkedClient rmoteClient=remoteClientMap.get(addr);
		if(rmoteClient==null){
			System.out.println(" rmoteClient==null, addr:"+addr);
			return ;
		}
		
		SentMsgPair sentMsgPair = new SentMsgPair(msg, callback);
			
		synchronized(rmoteClient.sendQueue){
			
			if(rmoteClient.sendingMsg!=null){
				rmoteClient.sendQueue.addLast(sentMsgPair);
			}
			else{
				rmoteClient.sendingMsg=sentMsgPair;
				rmoteClient.ioSession.write(sentMsgPair.msg);
			}
		}
		
	}

	public class ServerIOHandler implements IoHandler {

		@Override
		public void sessionCreated(IoSession session) throws Exception {
			System.out.println("sessionCreated " + session.getRemoteAddress());
		}

		@Override
		public void sessionOpened(IoSession session) throws Exception {
			
			System.out.println("sessionOpened " + session.getRemoteAddress());
			InetSocketAddress saddr=(InetSocketAddress) session.getRemoteAddress();
			String addr=saddr.getAddress().getHostAddress()+":"+saddr.getPort();
			
			System.out.println("addr:"+addr);
			RemoteLinkedClient remoteClient=new RemoteLinkedClient();
			remoteClient.ioSession = session;
			remoteClient.sendingMsg=null;
			remoteClient.sendQueue=new LinkedList<SentMsgPair>();
			
			session.setAttribute("addr", addr);
			session.setAttribute("remoteClient", remoteClient);
			
			remoteClientMap.put(addr, remoteClient);
		
			connectionCallback.connected(addr);
		}

		@Override
		public void sessionClosed(IoSession session) throws Exception {
			System.out.println("sessionClosed " + session.getRemoteAddress());

			String addr=(String) session.getAttribute("addr");
			RemoteLinkedClient remoteClient=(RemoteLinkedClient)session.getAttribute("remoteClient");
		
			if (remoteClient.sendingMsg != null) {
				remoteClient.sendingMsg.callback.sendFailed();
			}
			for(SentMsgPair pendingMsgPair:remoteClient.sendQueue){
				pendingMsgPair.callback.sendFailed();
			}
			
			connectionCallback.connectionLost(addr);
		
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
				String addr=(String)session.getAttribute("addr");
				Message msg = (Message) message;
				callbackMsgReceive.messageReceived(addr,msg);
			}

		}

		@Override
		public void messageSent(IoSession session, Object message)
				throws Exception {

			System.out.println("messageSent");
			
			RemoteLinkedClient remoteClient=(RemoteLinkedClient)session.getAttribute("remoteClient");
		
			synchronized(remoteClient.sendQueue){
				
				if(remoteClient.sendQueue.size()==0){
					remoteClient.sendingMsg=null;
				}
				else{
					SentMsgPair firstMsgPair=remoteClient.sendQueue.removeFirst();
					remoteClient.sendingMsg=firstMsgPair;
					remoteClient.ioSession.write(firstMsgPair.msg);
				}
			}
			
		}

		@Override
		public void exceptionCaught(IoSession session, Throwable cause) {
			System.out.println("exceptionCaught " + session.getRemoteAddress());
			cause.printStackTrace();

			session.close(true);

			// connectionCallback.connectionLost();

			// if(sendingMsg!=null){
			// sendingMsg.callback.sendFailed();
			// }
		}

	}

	public static void main(String[] args) throws Exception {

		
		MinaLineServer mnaLineServer=new MinaLineServer();
		int port=11111;
		
		
		mnaLineServer.listen(port, new LineServerCallbackAccept(){

			@Override
			public void connected(String addr) {
				
				System.out.println("connected:"+addr);
			}

			@Override
			public void connectionLost(String addr) {

				System.out.println("connectionLost:"+addr);
				
			}
			
		});
	}



	

}
