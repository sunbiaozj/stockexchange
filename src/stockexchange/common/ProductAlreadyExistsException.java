package stockexchange.common;

/**
 * An Exception class for when trying to create a ProductBook for a product that already exists
 * @author ScottMores
 *
 */
public class ProductAlreadyExistsException extends Exception {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a new Exception with details about the error that occurred
	 * @param msg the details to provide with the Exception when it is thrown
	 */
	public ProductAlreadyExistsException(String msg) {
		super(msg);
	}
}
