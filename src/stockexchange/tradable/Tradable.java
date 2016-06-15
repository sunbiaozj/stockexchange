package stockexchange.tradable;

import stockexchange.common.GlobalConstants.BookSide;
import stockexchange.common.InvalidDataException;
import stockexchange.price.Price;

/**
 * An interface for anything representing a "BUY" or "SELL" request that can be traded 
 * in the stockexchange application.
 * @author ScottMores
 *
 */
public interface Tradable {
	
	/**
	 * Gets the stock symbol (product) associated with the Tradable object.
	 * @return the stock symbol (product) for this Tradable object.
	 */
	public String getProduct();
	
	/**
	 * Gets the price associated with this Tradable object.
	 * @return the price for this Tradable object.
	 */
	public Price getPrice();
	
	/**
	 * Gets the original volume associated with this Tradable object.
	 * @return the original volume for this Tradable object.
	 */
	public int getOriginalVolume();
	
	/**
	 * Gets the remaining volume associated with this Tradable object.
	 * @return the remaining volume for this Tradable object.
	 */
	public int getRemainingVolume();
	
	/**
	 * Gets the cancelled volume associated with this Tradable object.
	 * @return the cancelled volume for this Tradable object.
	 */
	public int getCancelledVolume();
	
	/**
	 * Sets the cancelled volume associated with this Tradable object.
	 * @param newCancelledVolume the value used to set the new cancelled volume for this Tradable object.
	 * @throws InvalidDataException if the given volume is negative or if it would make the original volume or 
	 * remaining volume inconsistent.
	 */
	public void setCancelledVolume(int newCancelledVolume) throws InvalidDataException;
	
	/**
	 * Sets the remaining volume associated with this Tradable object.
	 * @param newRemainingVolume the value used to set the new remaining volume for this Tradable object.
	 * @throws InvalidDataException if the given volume is negative or if it would make the original volume or 
	 * cancelled volume inconsistent.
	 */
	public void setRemainingVolume(int newRemainingVolume) throws InvalidDataException;
	
	/**
	 * Gets the user name associated with this Tradable object.
	 * @return the user name for this Tradable object.
	 */
	public String getUser();
	
	/**
	 * Gets the side ("BUY" or "SELL") associated with this Tradable object.
	 * @return the side ("BUY" or "SELL") for this Tradable object.
	 */
	public BookSide getSide();
	
	/**
	 * Checks to see whether or not this Tradable object is part of a Quote.
	 * @return true if this Tradable object is part of a Quote and false otherwise.
	 */
	public boolean isQuote();

	/**
	 * Gets the order id associated with this Tradable object.
	 * @return the order id for this Tradable object.
	 */
	public String getId();
	
}
