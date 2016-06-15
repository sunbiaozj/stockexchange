package stockexchange.client;

import stockexchange.common.GlobalConstants.BookSide;
import stockexchange.common.InvalidDataException;

/**
 * A class that holds selected data elements related to the Tradables a user 
 * has submitted to the system.
 * @author Scott
 *
 */
public class TradableUserData {
	/** The user this data is for */
	private String userName;
	/** The stock symbol this data is for */
	private String stock;
	/** The BUY or SELL side of this data*/
	private BookSide side;
	/** The order ID of this data */
	private String orderId;
	
	/**
	 * Constructs a new TradableUserData object with the provided parameters
	 * @param userNameIn the username to associate with this object
	 * @param stockIn the stock symbol to associate with this object
	 * @param sideIn the side to associate with this object
	 * @param orderIdIn the order ID to associate with this object
	 * @throws InvalidDataException if any of the data is null or an empty String
	 */
	public TradableUserData(String userNameIn, String stockIn, BookSide sideIn, String orderIdIn) throws InvalidDataException {
		setUserName(userNameIn);
		setStock(stockIn);
		setSide(sideIn);
		setOrderId(orderIdIn);
	}
	
	/**
	 * Retrieves the username associated with this object
	 * @return the username associated with this object
	 */
	public String getUserName() {
		return userName;
	}
	
	/**
	 * Retrieves the stock symbol associated with this object
	 * @return the stock symbol associated with this object
	 */
	public String getStock() {
		return stock;
	}
	
	/**
	 * Retrieves the BUY or SELL side associated with this object
	 * @return the BUY or SELL side associated with this object
	 */
	public BookSide getSide() {
		return side;
	}
	
	/**
	 * Retrieves the order ID associated with this object
	 * @return the order ID associated with this object
	 */
	public String getOrderId() {
		return orderId;
	}
	
	/**
	 * Sets the userName for this object
	 * @param userIn the username to associate with this object
	 * @throws InvalidDataException
	 */
	private void setUserName(String userIn) throws InvalidDataException {
		if (userIn == null || userIn.isEmpty()) throw new InvalidDataException("The username cannot be null or empty");
		userName = userIn;
	}
	
	/**
	 * Sets the stock for this object
	 * @param stockIn the stock to associate with this object
	 * @throws InvalidDataException
	 */
	private void setStock(String stockIn) throws InvalidDataException {
		if (stockIn == null || stockIn.isEmpty()) throw new InvalidDataException("The stock cannot be null or empty");
		stock = stockIn;
	}
	
	/**
	 * Sets the side for this object
	 * @param sideIn the side to associate with this object
	 * @throws InvalidDataException
	 */
	private void setSide(BookSide sideIn) throws InvalidDataException {
		if (sideIn == null) throw new InvalidDataException("The side cannot be null");
		side = sideIn;
	}
	
	/**
	 * Sets the order ID for this object
	 * @param orderIdIn the order ID to associate with this object
	 * @throws InvalidDataException
	 */
	private void setOrderId(String orderIdIn) throws InvalidDataException {
		if (orderIdIn == null || orderIdIn.isEmpty()) throw new InvalidDataException("The order ID cannot be null or empty");
		orderId = orderIdIn;
	}
	
	/**
	 * Overrides Java's default implementation of toString to form a String representation of this object
	 * @return a String representation of this object
	 */
	public String toString() {
		return "User: " + getUserName() + "," + getSide() + getStock() + "(" + getOrderId() + ")";
	}
	
}
