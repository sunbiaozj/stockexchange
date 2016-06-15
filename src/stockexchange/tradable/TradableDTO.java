package stockexchange.tradable;

import stockexchange.common.GlobalConstants.BookSide;
import stockexchange.price.Price;

public class TradableDTO implements Comparable<TradableDTO> {
	
	/**
	 * The stock symbol (product) for this Tradable object.
	 */
	public String product;
	
	/**
	 * The price for this Tradable object.
	 */
	public Price price;
	
	/**
	 * The original starting volume of this Tradable object. 
	 */
	public int originalVolume;
	
	/**
	 * The remaining volume of this Tradable object.
	 */
	public int remainingVolume;
	
	/**
	 * The cancelled volume of this Tradable object.
	 */
	public int cancelledVolume;
	
	/**
	 * The user name associated with this Tradable object.
	 */
	public String user;
	
	/**
	 * The side ("BUY" or "SELL") associated with this Tradable object.
	 */
	public BookSide side;
	
	/**
	 * The indicator for whether or not this Tradable object is part of a Quote.
	 */
	public boolean isQuote;
	
	/**
	 * The order id for this Tradable object.
	 */
	public String id;
	
	/**
	 * Constructs a TradableDTO object with the given input values, which are taken from an existing Tradable object.
	 * @param inProduct the stock symbol (product) of this TradableDTO
	 * @param inPrice the price of this TradableDTO
	 * @param inOriginalVolume the starting original volume of this TradableDTO
	 * @param inRemainingVolume the remaining volume of this TradableDTO
	 * @param inCancelledVolume the cancelled volume of this TradableDTO
	 * @param inUser the user name associated with this TradableDTO 
	 * @param inSide the side ("BUY" or "SELL") of this TradableDTO
	 * @param inQuote the indicator for whether or not this TradableDTO is part of a Quote
	 * @param inId the order ID for this TradableDTO
	 */
	public TradableDTO(String inProduct, Price inPrice, int inOriginalVolume, int inRemainingVolume, int inCancelledVolume, 
			String inUser, BookSide inSide, boolean inQuote, String inId) {
		product = inProduct;
		price = inPrice;
		originalVolume = inOriginalVolume;
		remainingVolume = inRemainingVolume;
		cancelledVolume = inCancelledVolume;
		user = inUser;
		side = inSide;
		isQuote = inQuote;
		id = inId;
	}
	
	/**
	 * Overrides Java's default implementation of toString() and constructs a String representation of this TradableDTO
	 */
	public String toString() {
		return "Product: " + product + ", Price: " + price + ", OriginalVolume: " + originalVolume
				+ ", RemainingVolume: " + remainingVolume + ", CancelledVolume: " + cancelledVolume
				+ ", User: " + user + ", Side: " + side + ", IsQuote: " + isQuote + ", Id: " + id;
	}

	/**
	 * Method that allows TradableDTO objects to be compared by their Price
	 */
	public int compareTo(TradableDTO other) {
		if (price.greaterThan(other.price)) return 1;
		else if (price.lessThan(other.price)) return -1;
		return 0;
	}
}
