package ylj.JavaNetwork.Mina.Protocol;

public class Message {
		private int width;
	    private int height;
	    private int numberOfCharacters;
	    
	    
	    public Message(int width, int height, int numberOfCharacters) {
	        this.width = width;
	        this.height = height;
	        this.numberOfCharacters = numberOfCharacters;
	    }

	    public void setWidth(int width) {
	         this.width=width;
	    }

	    public void setHeight(int height) {
	         this.height=height;
	    }
	    
	    public int getWidth() {
	        return width;
	    }

	    public int getHeight() {
	        return height;
	    }

	    public int getNumberOfCharacters() {
	        return numberOfCharacters;
	    }
}
