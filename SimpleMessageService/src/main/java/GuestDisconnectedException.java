
public class GuestDisconnectedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String userName;
	
	public GuestDisconnectedException(String userName) {
		this.userName = userName;
	}
	
	@Override
	public String getMessage() {
		return userName + " has disconnected.";
	}

}
