package stockexchange.common;

/**
 * A class that represents exceptions for trying to reference stocks that don't
 * exist in the system.
 * @author ScottMores
 *
 */
public class InvalidStockException extends Exception {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a new InvalidStockException with a message
	 * that describes the reason the exception was raised.
	 * @param msg
	 */
	public InvalidStockException(String msg) {
		super(msg);
	}
}
