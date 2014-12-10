package ylj.line.client;

import ylj.line.message.Message;

public abstract class LineClient {

	protected  String userName;
	protected  String password;
	
	protected  LineClientCallbackConnection callbackConnection;
	protected  LineClientCallbackReceive callbackMsgReceive;
	
	public LineClient(String userName,String password){
		this.userName=userName;
		this.password=password;
	}
	
	/**
	 *  handle ReceiveCB event
	 * @param callback
	 */

	public void setReceiveCB(LineClientCallbackReceive callback){
		this.callbackMsgReceive=callback;
	}

	
	public abstract void connect(String url,LineClientCallbackConnection callback)throws Exception ;
	
	public abstract void send(Message msg,LineClientCallbackSend callback)throws Exception ;
	
}
