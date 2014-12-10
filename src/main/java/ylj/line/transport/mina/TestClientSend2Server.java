package ylj.line.transport.mina;

import ylj.line.client.LineClientCallbackConnection;
import ylj.line.client.LineClientCallbackSend;
import ylj.line.message.Message;
import ylj.line.server.LineServerCallbackAccept;
import ylj.line.server.LineServerCallbackReceive;

public class TestClientSend2Server {

	public static MinaLineServer initServer() throws Exception {

		MinaLineServer mnaLineServer = new MinaLineServer();
		int port = 11111;

		mnaLineServer.setReceiveCB(new LineServerCallbackReceive() {

			@Override
			public void messageReceived(String addr, Message message) {

				System.out.println("received a message:" + message + " "
						+ new String(message.data) + " @" + addr);

			}

		});

		mnaLineServer.listen(port, new LineServerCallbackAccept() {

			@Override
			public void connected(String addr) {

				System.out.println("connected:" + addr);
			}

			@Override
			public void connectionLost(String addr) {

				System.out.println("connectionLost:" + addr);

			}

		});

		return mnaLineServer;
	}

	public static MinaLineClient initClient() {

		String userName = "";
		String password = "";
		String url = "tcp://localhost:11111";

		MinaLineClient minaLineClient = new MinaLineClient(userName, password);
		minaLineClient.connect(url, new LineClientCallbackConnection() {
			@Override
			public void connected() {
				System.out.println("connect ok");
			}

			@Override
			public void connectionLost() {
				System.out.println("connectionLost");
			}
		});

		return minaLineClient;

	}

	public static void main(String[] args) throws Exception {

		MinaLineServer minaLineServer = initServer();
		MinaLineClient minaLineClient1 = initClient();
		MinaLineClient minaLineClient2 = initClient();

		for (int i = 0; i < 1000; i++) {
			System.out.println("minaLineClient.isConnect():"
					+ minaLineClient1.isConnect());
			if (minaLineClient1.isConnect()) {
				minaLineClient1.send(new Message((short) 1, (short) 1,
						("i am client[1] " + i).getBytes()),
						new LineClientCallbackSend() {

							@Override
							public void sendSuccess() {
								System.out.println("send success callback");
							}

							@Override
							public void sendFailed() {
								System.out.println("send failed callback");
							}

						});

				System.out.println("minaLineClient.isConnect():"
						+ minaLineClient2.isConnect());
				if (minaLineClient2.isConnect()) {
					minaLineClient2.send(new Message((short) 1, (short) 1,
							("i am client[2] " + i).getBytes()),
							new LineClientCallbackSend() {

								@Override
								public void sendSuccess() {
									System.out.println("send success callback");
								}

								@Override
								public void sendFailed() {
									System.out.println("send failed callback");
								}

							});
				}
				Thread.sleep(1000);
			}
		}
	}
}
