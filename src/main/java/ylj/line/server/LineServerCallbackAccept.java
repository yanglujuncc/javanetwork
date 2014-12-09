package ylj.line.server;


public interface LineServerCallbackAccept {
	

	
	/**
	 * 
	 * @param addr
	 */
	public void connected(String addr);

	public void connectionLost(String addr);

}
