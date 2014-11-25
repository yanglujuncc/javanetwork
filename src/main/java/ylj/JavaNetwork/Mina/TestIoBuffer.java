package ylj.JavaNetwork.Mina;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import org.apache.mina.core.buffer.IoBuffer;

public class TestIoBuffer {

	public static void main(String[] args) throws IOException{
		/*

 对于ByteBuffer有如下常用的操作：

flip():：读写模式的转换。

rewind() ：将 position 重置为 0 ，一般用于重复读。

clear() ：清空 buffer ，准备再次被写入 (position 变成 0 ， limit 变成 capacity) 。

compact(): 将未读取的数据拷贝到 buffer 的头部位。

mark() 、 reset():mark 可以标记一个位置， reset 可以重置到该位置。

get()、getShort()等一系列get操作：获取ByteBuffer中的内容，当然这里get的内容都是从position开始的，所以要时刻注意position。每次get之后position都会改变。Position的变化是根据你get的类型，如果是short，那就是2个byte，如果是int，那就是增加4个byte，即32。

put()、putShort()等一系列put操作：向ByteBuffer添加内容，这里put的内容都是从position开始的。每次put之后position都会改变。


		 */
		CharSequence val="helloworld";	
		CharSequence val2="nimei";	
		Charset cs = Charset.forName("utf-8");
		CharsetEncoder encoder=cs.newEncoder();
		CharsetDecoder decoder=cs.newDecoder();
		
		IoBuffer ioBuffer=IoBuffer.allocate(1024, true);
		
		ioBuffer.putInt(1);
		ioBuffer.putInt(2);	
		ioBuffer.putString(val, encoder);
		ioBuffer.putInt(4);	
		ioBuffer.putString(val, encoder);
		ioBuffer.putString(val2, encoder);
		ioBuffer.putInt(7);	
		
		ioBuffer.flip();
		
		System.out.println("\tremaining:"+ioBuffer.remaining());	
		int firstInt=ioBuffer.getInt();	
		System.out.println(firstInt+"\tremaining:"+ioBuffer.remaining());	
		int secondInt=ioBuffer.getInt();	
		System.out.println(secondInt+"\tremaining:"+ioBuffer.remaining());	
		String third=ioBuffer.getString(decoder);
		System.out.println(third+"\tremaining:"+ioBuffer.remaining());	
		int fourth=ioBuffer.getInt();
		System.out.println(fourth+"\tremaining:"+ioBuffer.remaining());	
		String fifth=ioBuffer.getString(decoder);
		System.out.println(fifth+"\tremaining:"+ioBuffer.remaining());	
		String sixth=ioBuffer.getString(decoder);
		System.out.println(sixth+"\tremaining:"+ioBuffer.remaining());	
		
	
	}
}
