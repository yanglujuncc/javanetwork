package ylj.line.server;

import ylj.line.message.Message;

public interface LineServerCallbackReceive {
	


	public void messageReceived(String addr,Message message) ;

}
