package ylj.line.server;


import ylj.line.message.Message;

public abstract class LineServer{

	

	protected  LineServerCallbackReceive callbackMsgReceive;

	
	public LineServer(){

	}
	
	/**
	 *  handle ReceiveCB event
	 * @param callback
	 */

	public void setReceiveCB(LineServerCallbackReceive callback){
		this.callbackMsgReceive=callback;
	}



	public abstract void listen(int port,LineServerCallbackAccept callback)throws Exception ;
	
	/**
	 * 
	 * @param addr localhost:8080
	 * @param msg
	 * @param callback
	 * @throws Exception
	 */
	public abstract void send(String addr,Message msg,LineServerCallbackSend callback)throws Exception;
}
