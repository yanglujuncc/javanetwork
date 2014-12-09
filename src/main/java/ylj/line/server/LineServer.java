package ylj.line.server;


import ylj.line.message.Message;

public abstract class LineServer{

	
	protected  LineServerCallbackAccept callbackAccept;
	protected  LineServerCallbackReceive callbackMsgReceive;

	
	public LineServer(){

	}
	
	/**
	 *  handle ReceiveCB event
	 * @param callback
	 */

	public void setCReceiveCB(LineServerCallbackReceive callback){
		this.callbackMsgReceive=callback;
	}



	public abstract void listen(int port,LineServerCallbackAccept callback)throws Exception ;
	
	public abstract void send(Message msg,LineServerCallbackSend callback)throws Exception;
}
