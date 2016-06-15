package stockexchange.common;

/**
 * Exception class for when a user is not connected to the system
 * @author ScottMores
 *
 */
public class UserNotConnectedException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor that accepts the reason why the error occurred
	 * @param msg
	 */
	public UserNotConnectedException(String msg) {
		super(msg);
	}
}
