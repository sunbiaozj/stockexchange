package stockexchange.common;

/**
 * A class that represents Exceptions for when a User tries to subscribe to information
 * about a stock that he or she is already subscribed to receive information about.
 * @author ScottMores
 *
 */
public class AlreadySubscribedException extends Exception {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a new AlreadySubscribedException object.
	 * @param msg a message that describes the reason the Exception was raised.
	 */
	public AlreadySubscribedException(String msg) {
		super(msg);
	}
}
