package stockexchange.tradable;

import stockexchange.common.GlobalConstants.BookSide;
import stockexchange.common.InvalidDataException;
import stockexchange.price.Price;

/**
 * Represents a request from a user to buy or sell a stock either at a
specified price or at the current market price.
 * @author ScottMores
 */
public class Order extends StockOrderRequest implements Tradable {
	
	/**
	 * Constructs a new Order object with the provided input data.
	 * @param inUserName the user name attached to this order.
	 * @param inStockSymbol the stock symbol (product) attached to this order.
	 * @param inOrderPrice the price of this order.
	 * @param inOriginalVolume the starting volume of this order.
	 * @param inSide the "BUY" or "SELL" side of this order.
	 * @throws InvalidDataException if the original volume is less than or equal to 0, 
	 * if the original order price is less than or equal to 0 and if the order side is not 
	 * either "BUY" or "SELL".
	 */
	public Order(String inUserName, String inStockSymbol, Price inOrderPrice, 
			int inOriginalVolume, BookSide inSide) throws InvalidDataException {
		super(inUserName,inStockSymbol,inOrderPrice,inOriginalVolume,inSide,
				inUserName+inStockSymbol+inOrderPrice+System.nanoTime());
	}
	
	/**
	 * Creates a copy of the Order object passed in with all of the same values.
	 * @param o the Order object to be copied.
	 */
	public Order(Order o) {
		super(o);
	}
	
	/**
	 * Checks whether or not this object is part of a Quote.
	 * @return true if this Order is part of a Quote and false otherwise.
	 */
	public boolean isQuote() {
		return false;
	}
	
	/**
	 * Overrides Java's default toString() implementation and produces 
	 * a String representation of the Order object.
	 */
	public String toString() {
		return getUser() + " order: " + getSide() + " " + getRemainingVolume() + " " + getProduct() + " at " + getPrice()
				+ " (Original Vol: " + getOriginalVolume() + ", CXL'd Vol: " + getCancelledVolume() + "), ID: " + getId();
	}
}
