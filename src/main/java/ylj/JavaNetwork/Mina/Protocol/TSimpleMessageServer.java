package ylj.JavaNetwork.Mina.Protocol;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.xml.DOMConfigurator;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ylj.JavaNetwork.Mina.Protocol.TSimpleMessageClient.ClientHandler;

public class TSimpleMessageServer {

	private static final int PORT = 11112;

	public static void main(String[] args) throws IOException {
		
		DOMConfigurator.configureAndWatch("conf/log4j.xml");

		
		// 服务端监听端口用
		IoAcceptor acceptor = new NioSocketAcceptor();
		
		//SslFilter sslFilter = new SslFilter(BogusSslContextFactory .getInstance(true));
		// 日志filter
		acceptor.getFilterChain().addLast("logger", new LoggingFilter());
		// 对象序列化工厂，用来将java对象序列化成二进制流
		acceptor.getFilterChain().addLast("mycodec", new ProtocolCodecFilter(new MessageSimpleCodecFactory()));
		// 业务处理handler
		acceptor.setHandler(new ServerHandler());

		acceptor.getSessionConfig().setReadBufferSize(2048);
		acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 5);

		// 设置监听端口
		acceptor.bind(new InetSocketAddress(PORT));

		System.out.println("Server starts to listen to PORT :" + PORT);

	}

	public static class ServerHandler implements IoHandler {
		static Logger logger = LoggerFactory.getLogger(ServerHandler.class);

		int messageId = 0;

		@Override
		public void sessionCreated(IoSession session) throws Exception {
			logger.info("Created ");

			String hello="hello world";
			byte[] helloByte=hello.getBytes("utf-8");
			MessageSimple msg =new MessageSimple(1, helloByte);
			session.write(msg);
		}

		@Override
		public void sessionOpened(IoSession session) throws Exception {
			logger.info("Opened ");
		}

		@Override
		public void sessionClosed(IoSession session) throws Exception {
			logger.info("Closed ");
		}

		@Override
		public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
			logger.info("IDLE " + session.getIdleCount(status));
		}

		@Override
		public void messageReceived(IoSession session, Object message) throws Exception {
			logger.info("Received ");

			MessageSimple msg = (MessageSimple) message;
			logger.info("receive from client,Message=>" + msg+" "+ new String(msg.getData(),"utf-8"));

			
			MessageSimple replyMsg=new MessageSimple(0,("hello_"+messageId).getBytes("utf-8"));
			session.write(replyMsg);
			messageId++;
	
		}

		@Override
		public void messageSent(IoSession session, Object message) throws Exception {

			logger.info("Sent ");
		}

		/*
		 * Invoked when any exception is thrown by user IoHandler implementation
		 * or by MINA.(non-Javadoc)
		 */
		@Override
		public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
			logger.info("Caught Exception ");
			cause.printStackTrace();
		}

	}
}
