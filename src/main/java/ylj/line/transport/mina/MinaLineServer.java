package ylj.line.transport.mina;

import java.io.IOException;
import java.net.InetSocketAddress;
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
import ylj.line.client.LineClientCallbackSend;
import ylj.line.message.Message;
import ylj.line.transport.mina.MinaLineClient.ClientIOHandler;
import ylj.line.transport.mina.MinaLineClient.SentMsgPair;
import ylj.line.transport.mina.MinaLineClient.SentRun;

public class MinaLineServer {
	
	NioSocketConnector connector;
	LineClientCallbackConnection connectionCallback;
	IoSession ioSession;

	SentMsgPair sendingMsg;
	LinkedBlockingQueue<SentMsgPair> sentQueue;

	SentRun sendRunner;
	Thread sendThread;

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

	public MinaLineServer(String userName, String password) {
	
		sentQueue = new LinkedBlockingQueue<SentMsgPair>();

	}

	public void startSendThread() {
		if (sendThread != null && sendThread.isAlive()) {
			sendThread.interrupt();
		}
		sendRunner = new SentRun();
		sendThread = new Thread(sendRunner, "sent thread");
		sendThread.start();

	}

	public void stopSendThread() {

		if (sendThread != null && sendThread.isAlive()) {
			sendThread.interrupt();
		}

	}


	public void connect(String url, LineClientCallbackConnection callback) {
		// 服务端监听端口用
				IoAcceptor acceptor = new NioSocketAcceptor();
				// 日志filter
				acceptor.getFilterChain().addLast("logger", new LoggingFilter());
				// 对象序列化工厂，用来将java对象序列化成二进制流
				acceptor.getFilterChain().addLast("textcodec", new ProtocolCodecFilter(new TextLineCodecFactory()));
				// 业务处理handler
				acceptor.setHandler(new ServerHandler());

				acceptor.getSessionConfig().setReadBufferSize(2048);
				acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);

				// 设置监听端口
				acceptor.bind(new InetSocketAddress(PORT));

				System.out.println("Server starts to listen to PORT :" + PORT);

	}

	@Override
	public void send(Message msg, LineClientCallbackSend callback) {
		SentMsgPair sentMsgPair = new SentMsgPair(msg, callback);
		try {
			sentQueue.put(sentMsgPair);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	class SentRun implements Runnable {

		@Override
		public void run() {
			System.out.println("send thread start...");
			while (true) {
				try {
					if (sendingMsg != null)
						sentQueue.wait();

					sendingMsg = sentQueue.take();
					ioSession.write(sendingMsg.msg);

				} catch (InterruptedException e) {
					e.printStackTrace();
					break;

				}

			}
			System.out.println("send thread end.");

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
			startSendThread();
		}

		@Override
		public void sessionClosed(IoSession session) throws Exception {
			System.out.println("sessionClosed " + session.getRemoteAddress());

			connectionCallback.connectionLost();
			if (sendingMsg != null) {
				sendingMsg.callback.sendFailed();
			}
			stopSendThread();
			connector.dispose();
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
			sendingMsg.notifyAll();
			sendingMsg.callback.sendSuccess();
			sendingMsg = null;
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

	}

}
