package ylj.line.transport.mina;

import java.io.IOException;
import java.net.InetSocketAddress;


import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import ylj.line.message.Message;

public class MessageServer {

	private static final int PORT = 11112;

	public static void main(String[] args) throws IOException {
		// 服务端监听端口用
		IoAcceptor acceptor = new NioSocketAcceptor();
		// 日志filter
		acceptor.getFilterChain().addLast("logger", new LoggingFilter());
		// 对象序列化工厂，用来将java对象序列化成二进制流
		acceptor.getFilterChain().addLast("mycodec", new ProtocolCodecFilter(new MessageCodecFactory()));
		// 业务处理handler
		acceptor.setHandler(new ServerHandler());

		acceptor.getSessionConfig().setReadBufferSize(2048);
		acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);

		// 设置监听端口
		acceptor.bind(new InetSocketAddress(PORT));

		System.out.println("Server starts to listen to PORT :" + PORT);

	}

	public static class ServerHandler implements IoHandler {

		int messageId = 0;

		@Override
		public void sessionCreated(IoSession session) throws Exception {
			System.out.println("Created "+session.getRemoteAddress());

			Message msg =new Message((short)1, (short)1, "hello".getBytes());
			session.write(msg);
		}

		@Override
		public void sessionOpened(IoSession session) throws Exception {
			System.out.println("Opened "+session.getRemoteAddress());
		}

		@Override
		public void sessionClosed(IoSession session) throws Exception {
			System.out.println("Closed "+session.getRemoteAddress());
		}

		@Override
		public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
			System.out.println("IDLE " + session.getIdleCount(status));
		}

		@Override
		public void messageReceived(IoSession session, Object message) throws Exception {
			System.out.println("Received ");

			Message msg = (Message) message;

		
			System.out.println("receive from client,Message=>" + new String(msg.data));

			Thread.sleep(1000);

			msg.data=("hello from server:"+messageId).getBytes();
			session.write(msg);
			messageId++;
		}

		@Override
		public void messageSent(IoSession session, Object message) throws Exception {

			System.out.println("Sent ");
		}

		/*
		 * Invoked when any exception is thrown by user IoHandler implementation
		 * or by MINA.(non-Javadoc)
		 */
		@Override
		public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
			System.out.println("Caught Exception "+session.getRemoteAddress());
			cause.printStackTrace();
		}

	}
}
