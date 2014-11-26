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

	public  static  class MessageEncoder implements ProtocolEncoder {

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
			out.flush();
		}

		@Override
		public void dispose(IoSession session) throws Exception {
		

		}

	}

	public  static class MessageDecoder implements ProtocolDecoder {
		
		static final String DECODER_STATE_KEY = MessageDecoder.class.getName() + ".STATE";
		static final int  DECODER_STATE_BEGIN=0;
		static final int  DECODER_STATE_HEAD_OK=1;
		static final int  DECODER_STATE_OK=2;
		
		static final String MESSAGE_KEY = MessageSimple.class.getName() + ".STATE";
		
		@Override
		public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {


			Integer stateObj=(Integer) session.getAttribute(DECODER_STATE_KEY);
			MessageSimple msg=(MessageSimple) session.getAttribute(MESSAGE_KEY);
			
			if(stateObj==null){							
				msg = new MessageSimple();
				session.setAttribute(DECODER_STATE_KEY, DECODER_STATE_BEGIN);		
				session.setAttribute(MESSAGE_KEY, msg);
				stateObj=DECODER_STATE_BEGIN;				
			}
			
			if(stateObj==DECODER_STATE_BEGIN){
				
				if (in.remaining() >= MessageSimple.FixSize) {
					int protocol = in.getInt();
					int dataSize = in.getInt();
					
					msg.setProtocol(protocol);
					msg.setDataSize(dataSize);
					
					session.setAttribute(DECODER_STATE_KEY, DECODER_STATE_HEAD_OK);
					stateObj=DECODER_STATE_HEAD_OK;		
				}else{
					return ;
				}
			}
			
			if(stateObj==DECODER_STATE_HEAD_OK){
				
				if (in.remaining() >= msg.getDataSize()) {
				
					byte[] bytes = new byte[msg.getDataSize()];
				    in.get(bytes);
				    msg.setData(bytes);
		    
				
					stateObj=DECODER_STATE_OK;							
				}else{
					return ;
				}
			}
			
			if(stateObj==DECODER_STATE_OK){
				
				out.write(msg);
				
				session.setAttribute(DECODER_STATE_KEY, DECODER_STATE_BEGIN);		
				session.setAttribute(MESSAGE_KEY, new MessageSimple());
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
