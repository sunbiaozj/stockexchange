package stockexchange.common;

/**
 * A class that represents Exceptions for when an order does not currently exist
 * in a product book.
 * @author ScottMores
 *
 */
public class OrderNotFoundException extends Exception {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a new OrderNotFoundException object.
	 * @param msg a message that describes the reason the Exception was raised.
	 */
	public OrderNotFoundException(String msg) {
		super(msg);
	}
}
