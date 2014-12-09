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
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import ylj.JavaNetwork.Mina.SimpleServer.ServerHandler;
import ylj.line.client.LineClientCallbackConnection;
import ylj.line.message.Message;
import ylj.line.server.LineServer;
import ylj.line.server.LineServerCallbackAccept;
import ylj.line.server.LineServerCallbackSend;


public class MinaLineServer extends LineServer {
	
	IoAcceptor acceptor;
	LineServerCallbackAccept connectionCallback;
	


	int PORT;

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
		LinkedList<SentMsgPair> sentQueue;
		IoSession ioSession;
		
		
	}

	public MinaLineServer() {
	
		remoteClientMap=new HashMap<String,RemoteLinkedClient> ();
	}



	@Override
	public void listen(int port, LineServerCallbackAccept callback) throws Exception {
		// 服务端监听端口用
		IoAcceptor acceptor = new NioSocketAcceptor();
		// 日志filter
		acceptor.getFilterChain().addLast("logger", new LoggingFilter());
		// 对象序列化工厂，用来将java对象序列化成二进制流
		acceptor.getFilterChain().addLast("line.message.codec", new ProtocolCodecFilter(new MessageCodecFactory()));
		// 业务处理handler
		acceptor.setHandler(new ServerHandler());

		acceptor.getSessionConfig().setReadBufferSize(2048);
		acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);

		// 设置监听端口
		acceptor.bind(new InetSocketAddress(PORT));

		System.out.println("Server starts to listen to PORT :" + PORT);

		
	}

	@Override
	public void send(String addr,Message msg, LineServerCallbackSend callback) {
		SentMsgPair sentMsgPair = new SentMsgPair(msg, callback);
	

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
			remoteClient.sentQueue=new LinkedList<SentMsgPair>();
			
			session.setAttribute("addr", addr);
			session.setAttribute("remoteClient", remoteClient);
			remoteClientMap.put(addr, remoteClient);
		}

		@Override
		public void sessionClosed(IoSession session) throws Exception {
			System.out.println("sessionClosed " + session.getRemoteAddress());

			String addr=(String) session.getAttribute("addr");
			RemoteLinkedClient remoteClient=(RemoteLinkedClient)session.getAttribute("remoteClient");
		
			if (remoteClient.sendingMsg != null) {
				remoteClient.sendingMsg.callback.sendFailed();
			}
			for(SentMsgPair pendingMsgPair:remoteClient.sentQueue){
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
				Message msg = (Message) message;
				callbackMsgReceive.messageReceived(msg);
			}

		}

		@Override
		public void messageSent(IoSession session, Object message)
				throws Exception {

			System.out.println("messageSent");
			
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

	public static void main(String[] args) throws IOException {

		
		MinaLineServer mnaLineServer=new MinaLineServer();
		mnaLineServer.listen(port, callback);
	}



	

}
