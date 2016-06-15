package stockexchange.common;

/**
 * A class that represents exceptions for illegal quotes
 * @author ScottMores
 */
public class DataValidationException extends Exception {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a new Exception with details about the error that occurred
	 * @param msg the details to provide with the Exception when it is thrown
	 */
	public DataValidationException(String msg) {
		super(msg);
	}
}
