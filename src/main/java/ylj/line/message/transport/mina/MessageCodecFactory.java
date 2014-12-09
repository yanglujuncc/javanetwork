package ylj.line.message.transport.mina;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import ylj.JavaNetwork.Mina.Protocol.MessageSimple;
import ylj.JavaNetwork.Mina.Protocol.MessageSimpleCodecFactory.MessageDecoder;
import ylj.line.message.Message;

public class MessageCodecFactory implements ProtocolCodecFactory {

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
			IoBuffer buffer = IoBuffer.allocate(8+msg.data.length, false);
			buffer.putShort(msg.version);
			buffer.putShort(msg.type);
			if (msg.data != null) {
				buffer.putInt(msg.data.length);
				buffer.put(msg.data);
			} else {
				buffer.putInt(0);
			}
			buffer.flip();
			out.write(buffer);

		}

		@Override
		public void dispose(IoSession session) throws Exception {

		}

	}

	static class MessageDecoder implements ProtocolDecoder {

		static final String DECODER_STATE_KEY = MessageDecoder.class.getName() + ".STATE";
		static final int DECODER_STATE_BEGIN = 0;
		static final int DECODER_STATE_HEAD_OK = 1;
		static final int DECODER_STATE_BODY_OK = 2;

		static final String DecodingMESSAGE_KEY = Message.class.getName()+".decoding";

		static final int MaxPayloadSize=1024*1024;
		@Override
		public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {

			Integer stateObj = (Integer) session.getAttribute(DECODER_STATE_KEY);
			Message decodingMsg = (Message) session.getAttribute(DecodingMESSAGE_KEY);

			if (stateObj == null) {
				stateObj = DECODER_STATE_BEGIN;
				decodingMsg = new Message();			
			}
			
			if (DECODER_STATE_BEGIN == stateObj) {
				if (in.remaining() >= 8) {
					short version = in.getShort();
					short type = in.getShort();
					int payloadSize = in.getInt();
					//max 
					if(payloadSize>MaxPayloadSize||payloadSize<0){				
						throw new Exception(" payloadSize "+MaxPayloadSize+">"+MaxPayloadSize);
					}
					decodingMsg.version=version;
					decodingMsg.type=type;
					
					if(payloadSize==0)
					{
						decodingMsg.data=null;
						stateObj=DECODER_STATE_BODY_OK;					
					}		
					else if(payloadSize>0)
					{
						decodingMsg.data=new byte[payloadSize];
						stateObj=DECODER_STATE_HEAD_OK;					
					}
				
				}
			}
			
			if (DECODER_STATE_HEAD_OK == stateObj) {
				if (in.remaining() >= decodingMsg.data.length) {
					in.get(decodingMsg.data, 0, decodingMsg.data.length);						
					stateObj=DECODER_STATE_BODY_OK;			
				}				
			}
			
			if(DECODER_STATE_BODY_OK==stateObj){
				out.write(decodingMsg);		
				session.removeAttribute(DECODER_STATE_KEY);
				session.removeAttribute(DecodingMESSAGE_KEY);
			}else{
				session.setAttribute(DECODER_STATE_KEY,stateObj);
				session.setAttribute(DecodingMESSAGE_KEY,decodingMsg);
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
