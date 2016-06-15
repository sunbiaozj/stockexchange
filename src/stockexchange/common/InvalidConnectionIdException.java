package stockexchange.common;

/**
 * Exception class for when a connection ID does not match the ID associated
 * with a specific user
 * @author Scott
 *
 */
public class InvalidConnectionIdException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a new InvalidConnectionIdException and accepts a message that describes why
	 * the error occurred 
	 * @param msg
	 */
	public InvalidConnectionIdException(String msg) {
		super(msg);
	}
}
