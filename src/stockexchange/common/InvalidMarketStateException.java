package stockexchange.common;

/**
 * Exception class for when an illegal operation is attempted due to the market's state
 * @author ScottMores
 *
 */
public class InvalidMarketStateException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidMarketStateException(String msg) {
		super(msg);
	}
}
