package ylj.line.server;


import ylj.line.message.Message;

public abstract class LineServer {

	public abstract void listen(int port,LineServerCallbackAccept callback);
	
	public abstract void send(Message msg,LineServerCallbackSend callback);
}
