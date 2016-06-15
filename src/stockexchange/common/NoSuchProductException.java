package stockexchange.common;

/**
 * Error class for when a user tries to look up a product that does not exist.
 * @author ScottMores
 *
 */
public class NoSuchProductException extends Exception {
	private static final long serialVersionUID = 1L;

	public NoSuchProductException(String msg) {
		super(msg);
	}
}
