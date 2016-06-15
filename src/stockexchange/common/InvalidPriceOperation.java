package stockexchange.common;

/**
 * A class that represents exceptions for invalid Price operations.
 * @author ScottMores
 */
public class InvalidPriceOperation extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new InvalidPriceOperation object.
	 * @param msg a message that describes the reason the Exception was raised.
	 */
	public InvalidPriceOperation (String msg) {
		super(msg);
	}
}
