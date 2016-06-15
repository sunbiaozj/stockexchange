package stockexchange.tradable;

import stockexchange.common.GlobalConstants.BookSide;
import stockexchange.common.InvalidDataException;
import stockexchange.price.Price;

/**
 * A class that represents one side ("BUY" or "SELL") of a Quote
 * @author ScottMores
 */
public class QuoteSide extends StockOrderRequest implements Tradable {
	
	/**
	 * Constructs a new QuoteSide object with the given input values.
	 * @param inUserName the user name to attach to this object.
	 * @param inStockSymbol the stock symbol (product) to attach to this object.
	 * @param inPrice the price to attach to this object.
	 * @param inOriginalVolume the starting volume of this object.
	 * @param inOrderSide the "BUY" or "SELL" side of this object.
	 * @throws InvalidDataException if the original volume is less than or equal to 0, 
	 * if the original order price is less than or equal to 0 and if the order side is not 
	 * either "BUY" or "SELL".
	 */
	public QuoteSide(String inUserName, String inStockSymbol, Price inPrice, 
			int inOriginalVolume, BookSide inOrderSide) throws InvalidDataException {
		super(inUserName, inStockSymbol, inPrice, inOriginalVolume, inOrderSide,
				inUserName+inStockSymbol+System.nanoTime());
	}
	
	/**
	 * Constructs a copy of the QuoteSide object passed in with all of the same values.
	 * @param qs the QuoteSide object to be copied.
	 */
	public QuoteSide(QuoteSide qs) {
		super(qs);
	}
	
	/**
	 * Checks whether or not this object is part of a Quote.
	 * @return true if this object is part of a quote and false otherwise.
	 */
	public boolean isQuote() {
		return true;
	}
	
	/**
	 * Overrides Order object's toString() method and produces a String representation 
	 * of this object.
	 */
	public String toString() {
		return getPrice() + " x " + getRemainingVolume() + " (Original Vol: " + getOriginalVolume()
				+ ", CXL'd Vol: " + getCancelledVolume() + ") [" + getId() + "]";
	}
}
