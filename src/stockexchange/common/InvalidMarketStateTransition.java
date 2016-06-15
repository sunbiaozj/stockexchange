package stockexchange.common;

/**
 * Error class for when a user tries to make an invalid transition between market 
 * states.
 * @author ScottMores
 *
 */
public class InvalidMarketStateTransition extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor for new error messages.
	 * @param msg the error message to pass along when the Exception is thrown
	 */
	public InvalidMarketStateTransition(String msg) {
		super(msg);
	}
}
