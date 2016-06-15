package stockexchange.common;

/**
 * Exception class that is thrown when trying to connect a user to the system who is already connected
 * @author ScottMores
 *
 */
public class AlreadyConnectedException extends Exception {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a new AlreadyConnectedException and accepts a message that describes why the Exception was thrown
	 * @param msg
	 */
	public AlreadyConnectedException(String msg) {
		super(msg);
	}
}
