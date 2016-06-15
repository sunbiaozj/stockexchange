package stockexchange.common;

/**
 * A class that represents Exceptions for when a User tries to get information about
 * a stock he or she is not subscribed to get information about.
 * @author ScottMores
 *
 */
public class NotSubscribedException extends Exception {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a new NotSubscribedException object.
	 * @param msg a message that describes the reason the Exception was raised.
	 */
	public NotSubscribedException(String msg) {
		super(msg);
	}
}
