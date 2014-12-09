package ylj.JavaNetwork.NIO.cs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class MyClient {
	public static void main(String args[]) throws Exception {
	
			MyClient client = new MyClient();
			client.work();
		
	}

	SocketChannel sc = null;

	Selector selector = null;

	// 发送接收缓冲区
	ByteBuffer send = ByteBuffer.wrap("data come from client".getBytes());
	ByteBuffer receive = ByteBuffer.allocate(1024);

	public void work() throws Exception {

		selector = Selector.open();

		// 注册为非阻塞通道
		sc = SocketChannel.open();
		sc.configureBlocking(false);

		sc.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		sc.connect(new InetSocketAddress("localhost", 8081));

		// Set<SelectionKey> selectionKeys = null;
		while (true) {
			// 选择
			if (selector.select() == 0)
				continue;

			Iterator<SelectionKey> it = selector.selectedKeys().iterator();

			while (it.hasNext()) {
				SelectionKey key = it.next();
				// 必须由程序员手动操作
				it.remove();
				sc = (SocketChannel) key.channel();

				if (key.isConnectable()) {
					if (sc.isConnectionPending()) {
						// 结束连接，以完成整个连接过程
						sc.finishConnect();
						System.out.println("connect completely");
						sc.write(send);
						// sc.register(selector,
						// SelectionKey.OP_READ|SelectionKey.OP_WRITE);
					}

				} else if (key.isReadable()) {
					try {
						receive.clear();
						sc.read(receive);
						System.out.println(new String(receive.array()));

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else if (key.isWritable()) {
					receive.flip();
					send.flip();
					sc.write(send);

				}

			}// end while

		}// end while(true)

	}// end work()

}
