package ylj.line.message;

import java.nio.ByteBuffer;

public class Message {

	public	short version;
	public	short type;	
	public byte[] data;
	
	public Message(){
		
	}
	public Message(	short version,short type){
		this.version=version;
		this.type=type;
	}
	public Message(	short version,short type,byte[] data){
		this.version=version;
		this.type=type;
		this.data=data;
	}
	public byte[] toBytes(){
	
		
		int size=Short.SIZE/Byte.SIZE+Short.SIZE/Byte.SIZE+data.length;
		System.out.println(size);
		
		ByteBuffer byteBuffer=ByteBuffer.allocate(size);
		
		byteBuffer.putShort(version);
		byteBuffer.putShort(type);
		byteBuffer.put(data);
		
		return byteBuffer.array();
	}
	
	public static Message buildFromByte(byte[] bytes){
	
		Message rebuildMessage=new Message();
		ByteBuffer byteBuffer=ByteBuffer.wrap(bytes);
		
		rebuildMessage.version=byteBuffer.getShort();
		rebuildMessage.type=byteBuffer.getShort();
		rebuildMessage.data=new byte[byteBuffer.remaining()];
		byteBuffer.get(rebuildMessage.data);
		
		return rebuildMessage;
	}
	
	public String toString(){
		return version+" "+type+" data:"+data.length;
	}
	
}
