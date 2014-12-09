package ylj.line.client;

import ylj.line.message.Message;

public abstract class LineClient {

	protected  String userName;
	protected  String password;
	
	protected  LineClientCallbackConnection callbackConnection;
	protected  LineClientCallbackReceive callbackMsgReceive;
	protected  LineClientCallbackSent callbackMsgSent;
	
	public LineClient(String userName,String password){
		this.userName=userName;
		this.password=password;
	}
	
	/**
	 *  handle ReceiveCB event
	 * @param callback
	 */

	public void setCReceiveCB(LineClientCallbackReceive callback){
		this.callbackMsgReceive=callback;
	}

	
	public abstract void connect(String url,LineClientCallbackConnection callback);
	
	public abstract void sent(Message msg,LineClientCallbackSent callback);
	
	public abstract void start();
	public abstract void close();
	
}
