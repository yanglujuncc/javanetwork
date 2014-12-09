package ylj.JavaNetwork.Mina.Protocol;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class MessageCodecFactory implements ProtocolCodecFactory{

	private ProtocolEncoder encoder;
	private ProtocolDecoder decoder;

	public MessageCodecFactory() {

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

			Message msg = (Message) message;
			IoBuffer buffer = IoBuffer.allocate(12, false);
			buffer.putInt(msg.getWidth());
			buffer.putInt(msg.getHeight());
			buffer.putInt(msg.getNumberOfCharacters());
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


			if (in.remaining() >= 12) {
				int width = in.getInt();
				int height = in.getInt();
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
