package stockexchange.tradable;

import stockexchange.common.GlobalConstants.BookSide;
import stockexchange.common.InvalidDataException;
import stockexchange.price.Price;

/**
 * Abstract class that represents requests from a user to buy or sell
 * stocks.
 * @author ScottMores
 *
 */
public abstract class StockOrderRequest {
	
	// The user name attached to this order.
	private String userName;
	// The stock symbol (product) attached to this order.
	private String stockSymbol;
	// The order ID attached to this order.
	private String orderId;
	// The buy or sell side of this order.
	private BookSide orderSide;
	// The Price object that represents this order's price.
	private Price price;
	// The original order volume.
	private int originalVol;
	// The remaining order volume.
	private int remainingVol;
	// The cancelled order volume.
	private int cancelledVol;
	

	/**
	 * Constructs a new Order object with the provided input data.
	 * @param inUserName the user name attached to this order.
	 * @param inStockSymbol the stock symbol (product) attached to this order.
	 * @param inOrderPrice the price of this order.
	 * @param inOriginalVolume the starting volume of this order.
	 * @param inSide the "BUY" or "SELL" side of this order.
	 * @param orderIdIn the String Id for this order.
	 * @throws InvalidDataException if the original volume is less than or equal to 0, 
	 * if the original order price is less than or equal to 0 and if the order side is not 
	 * either "BUY" or "SELL".
	 */
	protected StockOrderRequest(String inUserName, String inStockSymbol, Price inOrderPrice, 
			int inOriginalVolume, BookSide inSide, String orderIdIn) throws InvalidDataException {
		setUserName(inUserName);
		setStockSymbol(inStockSymbol);
		setPrice(inOrderPrice);
		setOriginalVolume(inOriginalVolume);
		setOrderSide(inSide);
		setRemainingVolume(inOriginalVolume);
		setOrderId(orderIdIn);
	}
	
	/**
	 * Creates a copy of the Order object passed in with all of the same values.
	 * @param o the Order object to be copied.
	 */
	protected StockOrderRequest(StockOrderRequest requestIn) {
		userName = requestIn.getUser();
		stockSymbol = requestIn.getProduct();
		orderId = requestIn.getId();
		orderSide = requestIn.getSide();
		price = requestIn.getPrice();
		originalVol = requestIn.getOriginalVolume();
		remainingVol = requestIn.getRemainingVolume();
		cancelledVol = requestIn.getCancelledVolume();
	}
	
	/**
	 * Default constructor so that subclasses can properly extend this class.
	 */
	protected StockOrderRequest() {}
	
	/**
	 * Gets the stock symbol (product) associated with this object.
	 * @return the stock symbol (product) for this Order.
	 */
	public String getProduct() {
		return stockSymbol;
	}
	
	/**
	 * Gets the price associated with this object.
	 * @return the Price object for this Order.
	 */
	public Price getPrice() {
		return price; // safe because PriceFactory manages Price references
	}
	
	/**
	 * Gets the original order volume for this object.
	 * @return the original volume for this Order.
	 */
	public int getOriginalVolume() {
		return originalVol;
	}
	
	/**
	 * Gets the remaining volume associated with this object.
	 * @return the remaining volume for this Order.
	 */
	public int getRemainingVolume() {
		return remainingVol;
	}
	
	/**
	 * Gets the cancelled volume associated with this object.
	 * @return the cancelled volume for this Order.
	 */
	public int getCancelledVolume() {
		return cancelledVol;
	}
	
	/**
	 * Sets the user name associated with this request.
	 * @param userNameIn
	 * @throws InvalidDataException if the username is null or an empty String.
	 */
	private void setUserName(String userNameIn) throws InvalidDataException {
		if (userNameIn == null || userNameIn.equals("")) throw new InvalidDataException("The username must contain at least one character.");
		userName = userNameIn;
	}
	
	/**
	 * Sets the stock symbol associated with this request.
	 * @param stockSymbolIn
	 * @throws InvalidDataException if the stock symbol is null or an empty String.
	 */
	private void setStockSymbol(String stockSymbolIn) throws InvalidDataException{
		if (stockSymbolIn == null || stockSymbolIn.equals("")) throw new InvalidDataException("The product name must contain at least one character.");
		stockSymbol = stockSymbolIn;
	}
	
	/**
	 * Sets this object's orderId member variable.
	 * @param id the String that will be associated with the orderId.
	 * @throws InvalidDataException if the order ID is null or an empty String.
	 */
	private void setOrderId(String orderIdIn) throws InvalidDataException {
		if (orderIdIn == null || orderIdIn.equals("")) throw new InvalidDataException("The order ID must contain at least one character.");
		orderId = orderIdIn;
	}
	
	/**
	 * Sets the order side associated with this request.
	 * @param orderSideIn
	 * @throws InvalidDataException if the order side is not "BUY" or "SELL".
	 */
	private void setOrderSide(BookSide orderSideIn) throws InvalidDataException {
		if (orderSideIn == null) throw new InvalidDataException("The order side cannot be null.");
		orderSide = orderSideIn;
	}
	
	/**
	 * Sets the price associated with this request.
	 * @param priceIn
	 * @throws InvalidDataException if the price object is null or the price value is less than or equal to 0.
	 */
	private void setPrice(Price priceIn) throws InvalidDataException {
		if (priceIn == null || (!priceIn.isMarket() && priceIn.getValue() <= 0)) throw new InvalidDataException("Your starting order price must be positive. Your price: " + priceIn.getValue());
		price = priceIn;
	}
	
	/**
	 * Sets the original volume associated with this request.
	 * @param originalVolIn
	 * @throws InvalidDataException if the original volume is less than or equal to 0.
	 */
	private void setOriginalVolume(int originalVolIn) throws InvalidDataException {
		if (originalVolIn <= 0) throw new InvalidDataException("Your original volume must be greater than 0. You entered: " + originalVolIn);
		originalVol = originalVolIn;
	}
	
	/**
	 * Sets the cancelled volume of this Order to the input volume.
	 * @param newCancelledVolume the value used to set this Order's cancelled volume.
	 * @throws InvalidDataException if the input volume is less than 0 or if the requested volume plus the remaining 
	 * volume is greater than the original volume.
	 */
	public void setCancelledVolume(int newCancelledVolume) throws InvalidDataException {
		if (newCancelledVolume < 0) throw new InvalidDataException("The cancelled volume cannot be negative. You entered: " + newCancelledVolume);
		if (newCancelledVolume + getCancelledVolume() > getOriginalVolume()) {
			throw new InvalidDataException("Remaining Volume: [" + getRemainingVolume() + "] + " + "Requested Cancelled Volume: [" 
		    + newCancelledVolume + "] would exceed Original Volume: [" + getOriginalVolume() + "]");
		}
		cancelledVol = newCancelledVolume;
	}
	
	/**
	 * Sets the remaining volume of this Order to the input volume.
	 * @param newRemainingVolume the value used to set this Order's remaining volume.
	 * @throws InvalidDataException if the input value is negative or if the input value plus the cancelled volume 
	 * is greater than the original volume.
	 */
	public void setRemainingVolume(int newRemainingVolume) throws InvalidDataException {
		if (newRemainingVolume < 0) throw new InvalidDataException("The remaining volume cannot be negative");
		if (newRemainingVolume + getCancelledVolume() > getOriginalVolume()) {
			throw new InvalidDataException("Requested Remaing Volume: [" + newRemainingVolume + "] + " + "Cancelled Volume: [" 
				    + getCancelledVolume() + "] would exceed Original Volume: [" + getOriginalVolume() + "]");
		}
		remainingVol = newRemainingVolume;
	}
	
	/**
	 * Gets the name of the user associated with this object.
	 * @return the user name for this Order.
	 */
	public String getUser() {
		return userName;
	}
	
	/**
	 * Gets the order side for this object.
	 * @return the "BUY" or "SELL" side for this Order.
	 */
	public BookSide getSide() {
		return orderSide;
	}
	
	/**
	 * Checks whether or not this object is part of a Quote.
	 * @return true if this Order is part of a Quote and false otherwise.
	 */
	abstract boolean isQuote();
	
	/**
	 * Gets the order ID associated with this object.
	 * @return the order ID for this Order.
	 */
	public String getId() {
		return orderId;
	}
}
