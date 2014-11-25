package ylj.JavaNetwork.Mina.Protocol;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class MessageSimpleCodecFactory implements ProtocolCodecFactory{

	private ProtocolEncoder encoder;
	private ProtocolDecoder decoder;

	public MessageSimpleCodecFactory() {

		encoder = new MessageEncoder();
		decoder = new MessageDecoder();
	}
	
	@Override
	public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
		return encoder;
	}
	
	@Override
	public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
		return decoder;
	}

	class MessageEncoder implements ProtocolEncoder {

		@Override
		public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {

			MessageSimple msg = (MessageSimple) message;
			int size=MessageSimple.FixSize+msg.getDataSize();
			IoBuffer buffer = IoBuffer.allocate(size, false);
			buffer.putInt(msg.getProtocol());
			buffer.putInt(msg.getDataSize());
			buffer.put(msg.getData());
			buffer.flip();
			out.write(buffer);

		}

		@Override
		public void dispose(IoSession session) throws Exception {
		

		}

	}

	class MessageDecoder implements ProtocolDecoder {

		@Override
		public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {


			//need fixSize
			//need dataSize
			in.prefixedDataAvailable(prefixLength)
			if (in.remaining() >= MessageSimple.FixSize) {
				int protocol = in.getInt();
				int dataSize = in.getInt();
				int numberOfCharachters = in.getInt();
				Message msg = new Message(width, height, numberOfCharachters);
				out.write(msg);

			}

		}

		@Override
		public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
			

		}

		@Override
		public void dispose(IoSession session) throws Exception {
			

		}

	}
}
