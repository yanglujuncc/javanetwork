package ylj.JavaNetwork.Mina.Protocol;

public class MessageSimple {
	
	
	int protocol;
	int dataSize;
	byte[] data;
	
	public static final int FixSize=8;
	
	public MessageSimple(){
		
		protocol=-1;
		dataSize=-1;
		data=null;
	}
	
	
	public MessageSimple(int protocol,byte[] data){
		this.protocol=protocol;
		this.data=data;
		this.dataSize=data.length;
	}
	public MessageSimple(int protocol,int dataSize){
		this.protocol=protocol;

		this.dataSize=data.length;
	}
	
	
	public int getProtocol(){
		return protocol;
	}
	public int getDataSize(){
		return dataSize;
	}
	public byte[] getData(){
		return data;
	}
	
	public void  setProtocol(int protocol){
		this.protocol=protocol;	
	}
	public void  setDataSize(int DataSize){	
		this.dataSize=DataSize;
	}
	public void  setData(byte[] data){	
		this.data=data;	
	}
	
	public String toString(){
		return "protocl:"+protocol+" dataSize:"+dataSize;
	}
}
