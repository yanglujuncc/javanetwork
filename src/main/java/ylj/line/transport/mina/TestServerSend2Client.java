package ylj.line.transport.mina;

import ylj.line.client.LineClientCallbackConnection;
import ylj.line.client.LineClientCallbackReceive;

import ylj.line.message.Message;
import ylj.line.server.LineServerCallbackAccept;
import ylj.line.server.LineServerCallbackReceive;
import ylj.line.server.LineServerCallbackSend;

public class TestServerSend2Client {
	
	static int port=11113;
	public static MinaLineServer initServer() throws Exception{
		
		MinaLineServer mnaLineServer=new MinaLineServer();
	
		
		mnaLineServer.setReceiveCB(new LineServerCallbackReceive(){

			@Override
			public void messageReceived(String addr,Message message) {
				
				System.out.println("received a message:"+message+" "+new String(message.data)+" @"+addr);
				
			}
			
		});
		
		mnaLineServer.listen(port, new LineServerCallbackAccept(){

			@Override
			public void connected(String addr) {
				
				System.out.println("connected:"+addr);
			}

			@Override
			public void connectionLost(String addr) {

				System.out.println("connectionLost:"+addr);
				
			}
			
		});
		
		return mnaLineServer;
	}
	public static MinaLineClient initClient(){
		

		String userName = "";
		String password = "";
		String url = "tcp://localhost:"+ port;

		
		final MinaLineClient minaLineClient = new MinaLineClient(userName, password);
		minaLineClient.setReceiveCB(new LineClientCallbackReceive(){

			@Override
			public void messageReceived(Message message) {
				System.out.println(minaLineClient.getAddr()+" received a message:"+message+" "+new String(message.data)+" @server");
				
			}
			
		});
		minaLineClient.connect(url, new LineClientCallbackConnection() {
			@Override
			public void connected() {
				System.out.println("connect ok");
			}

			@Override
			public void connectionLost() {
				System.out.println("connectionLost");
			}}
		);

		return minaLineClient;
		
		
	}
	public static void main(String[] args) throws Exception {

	 	MinaLineServer minaLineServer=initServer();		
	 	MinaLineClient minaLineClient1=initClient();
		MinaLineClient minaLineClient2=initClient();
	 	
		 
	 	for(int i=0;i<100;i++){
	 		System.out.println("minaLineClient.isConnect():"+minaLineClient1.isConnect());
			if(minaLineClient1.isConnect()){
				
				String addr=minaLineClient1.getAddr();
				
				minaLineServer.send(addr,new Message((short)1,(short)1,("i am server "+i).getBytes()),new LineServerCallbackSend(){

					@Override
					public void sendSuccess() {
						System.out.println("send success");
					}

					@Override
					public void sendFailed() {
						System.out.println("send failed");
					}
					
				});
				
			}
			System.out.println("minaLineClient.isConnect():"+minaLineClient2.isConnect());
			if(minaLineClient2.isConnect()){
				
				String addr=minaLineClient2.getAddr();
				
				minaLineServer.send(addr,new Message((short)1,(short)1,("i am server "+i).getBytes()),new LineServerCallbackSend(){

					@Override
					public void sendSuccess() {
						System.out.println("send success");
					}

					@Override
					public void sendFailed() {
						System.out.println("send failed");
					}
					
				});
				
			}
			Thread.sleep(1000);
		}
	 	minaLineServer.close();
	 	minaLineClient1.close();
		minaLineClient2.close();
	}
}

