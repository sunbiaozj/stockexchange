package stockexchange.common;

/**
 * A class that represents exceptions for invalid method parameters.
 * @author ScottMores
 */
public class InvalidDataException extends Exception {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new InvalidDataException object.
	 * @param msg a message that describes the reason the Exception was raised.
	 */
	public InvalidDataException(String msg) {
		super(msg);
	}
}
