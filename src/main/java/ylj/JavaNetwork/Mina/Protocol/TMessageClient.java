package ylj.JavaNetwork.Mina.Protocol;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Calendar;
import java.util.Date;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

public class TMessageClient {

	private static final String HOSTNAME = "localhost";
	private static final int PORT = 11112;
	private static final int CONNECT_TIMEOUT = 100;
	private static final boolean USE_CUSTOM_CODEC = false;

	public static void main(String[] args) throws IOException {

		NioSocketConnector connector = new NioSocketConnector();
		connector.setConnectTimeoutMillis(CONNECT_TIMEOUT);

		// connector.getFilterChain().addLast("codec", new
		// ProtocolCodecFilter(new SumUpProtocolCodecFactory(false)));

		connector.getFilterChain().addLast("mycodec", new ProtocolCodecFilter(new MessageCodecFactory()));
		connector.getFilterChain().addLast("logger", new LoggingFilter());

		connector.setHandler(new ClientHandler());
		IoSession session;

		for (;;) {
			try {
				ConnectFuture future = connector.connect(new InetSocketAddress(HOSTNAME, PORT));
				future.awaitUninterruptibly();
				session = future.getSession();
				break;
			} catch (RuntimeIoException e) {
				System.err.println("Failed to connect.");
				e.printStackTrace();
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

		// wait until the summation is done
		session.getCloseFuture().awaitUninterruptibly();
		connector.dispose();

	}

	public static class ClientHandler implements IoHandler {

		int messageId = 0;

		@Override
		public void sessionCreated(IoSession session) throws Exception {
			System.out.println("Created ");
		}

		@Override
		public void sessionOpened(IoSession session) throws Exception {
			System.out.println("Opened ");
		}

		@Override
		public void sessionClosed(IoSession session) throws Exception {
			System.out.println("Closed ");
		}

		@Override
		public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
			System.out.println("IDLE " + session.getIdleCount(status));
		}

		@Override
		public void messageReceived(IoSession session, Object message) throws Exception {
		
			System.out.println("Received ");

			Message msg = (Message) message;

			int Width = msg.getWidth();
			System.out.println("receive from server,Message=>" + Width);

			Thread.sleep(1000);

			Width++;
			msg.setWidth(Width);
			session.write(msg);
			messageId++;

		}

		@Override
		public void messageSent(IoSession session, Object message) throws Exception {
			System.out.println("Sent ");

		}

		@Override
		public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
			System.out.println("Caught Exception ");
			cause.printStackTrace();
		}

	}
}
