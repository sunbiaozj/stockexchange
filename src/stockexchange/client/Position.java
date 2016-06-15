package stockexchange.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import stockexchange.common.GlobalConstants.BookSide;
import stockexchange.common.InvalidDataException;
import stockexchange.common.InvalidPriceOperation;
import stockexchange.price.Price;
import stockexchange.price.PriceFactory;

/**
 * A class that holds an individual user's profit and loss information, including 
 * how much they have spent buying stock, how much they gained or lost selling 
 * stock, and the value of the stock they currently own
 * @author ScottMores
 *
 */
public class Position {
	
	/** Holds the stocks owned by the user and the value of the shares */
	private HashMap<String, Integer> holdings = new HashMap<String, Integer>();
	/** The balance between the money spent on stock purchases and the money gained from stock sales */
	private Price accountCosts = PriceFactory.makeLimitPrice(0);
	/** The current value of the stocks owned by a user */
	private HashMap<String, Price> lastSales = new HashMap<String, Price>();
	
	/**
	 * Constructs a new Position object
	 */
	public Position(){}
	
	/**
	 * Updates the holdings list and the account costs when some market activity occurs
	 * @param product the stock to update the holdings for
	 * @param price the price to update to
	 * @param side the side to update to
	 * @param volume the volume to update to
	 * @throws InvalidDataException if any parameter is null or if the volume is negative
	 * @throws InvalidPriceOperation if the Price is a Market price
	 */
	public void updatePosition(String product, Price price, BookSide side, int volume) throws InvalidDataException, InvalidPriceOperation {
		if (product == null || product.isEmpty()) throw new InvalidDataException("The product cannot be null or empty");
		if (price == null) throw new InvalidDataException("The price cannot be null");
		if (side == null) throw new InvalidDataException("The side cannot be null");
		if (volume < 0) throw new InvalidDataException("The volume cannot be negative. Your volume: " + volume);
		int adjustedVolume;
		if (side.equals(BookSide.BUY)) adjustedVolume = volume;
		else adjustedVolume = -volume;
		if (!getHoldingsMap().containsKey(product)) getHoldingsMap().put(product, adjustedVolume);
		else {
			int currentVolume = getHoldingsMap().get(product);
			int newVolume = currentVolume + adjustedVolume;
			if (newVolume == 0) getHoldingsMap().remove(product);
			else getHoldingsMap().put(product, newVolume);
		}
		Price totalPrice = price.multiply(volume);
		if (side.equals(BookSide.BUY)) setAccountCosts(getAccountCosts().subtract(totalPrice));
		else setAccountCosts(getAccountCosts().add(totalPrice));
	}
	
	/**
	 * Inserts the last sale for the specified stock into the “last sales” HashMap
	 * @param product the stock to update
	 * @param price the new price to update to
	 * @throws InvalidDataException if the product is null or empty or if the price is null
	 */
	public void updateLastSale(String product, Price price) throws InvalidDataException {
		if (product == null || product.isEmpty()) throw new InvalidDataException("The stock cannot be null or empty");
		if (price == null) throw new InvalidDataException("The price cannot be null");
		getLastSales().put(product, price);
	}
	
	/**
	 * Returns the volume of the specified stock this user owns
	 * @param product the stock to look up the volume for
	 * @return the volume of the specified stock this user owns
	 * @throws InvalidDataException if the product is null or empty
	 */
	public int getStockPositionVolume(String product) throws InvalidDataException {
		if (product == null || product.isEmpty()) throw new InvalidDataException("The stock cannot be null or empty");
		if (!getHoldingsMap().containsKey(product)) return 0;
		else return getHoldingsMap().get(product);
	}
	
	/**
	 * Returns a sorted ArrayList of Strings containing the stock symbols this user owns
	 * @return a sorted ArrayList of Strings containing the stock symbols this user owns
	 */
	public ArrayList<String> getHoldings() {
		ArrayList<String> h = new ArrayList<String>(getHoldingsMap().keySet());
		Collections.sort(h);
		return h;
	}
	
	/**
	 * Returns the current value of the stock symbol passed in that is owned by the user
	 * @param product the stock to retrieve the current value for
	 * @return the current value of the stock symbol passed in that is owned by the user
	 * @throws InvalidDataException if the product is null or empty
	 * @throws InvalidPriceOperation the the price stored in the holdings map is a market price
	 */
	public Price getStockPositionValue(String product) throws InvalidDataException, InvalidPriceOperation {
		if (product == null || product.isEmpty()) throw new InvalidDataException("The product cannot be null or empty");
		if (!getHoldingsMap().containsKey(product)) return PriceFactory.makeLimitPrice(0);
		Price lastSale = getLastSales().get(product);
		if (lastSale == null) return PriceFactory.makeLimitPrice(0);
		else return lastSale.multiply(getHoldingsMap().get(product));
	}
	
	
	/**
	 * Returns the accountCosts for this object
	 * @return the accountCosts for this object
	 */
	public Price getAccountCosts() {
		return accountCosts;
	}
	
	/**
	 * Returns the total current value of all stocks this user owns
	 * @return the total current value of all stocks this user owns
	 * @throws InvalidPriceOperation if the holdings map contains a market price
	 * @throws InvalidDataException if the holdings map contains a null or empty stock
	 */
	public Price getAllStockValue() throws InvalidPriceOperation, InvalidDataException {
		Price p = PriceFactory.makeLimitPrice(0);
		for (String stock: getHoldingsMap().keySet()) {
			Price temp = getStockPositionValue(stock);
			p = p.add(temp);
		}
		return p;
	}
	
	/**
	 * Returns the net value of the user's account
	 * @return the net value of the user's account
	 * @throws InvalidPriceOperation if the holdings map contains a market price
	 * @throws InvalidDataException if the holdings map contains a null or empty stock
	 */
	public Price getNetAccountValue() throws InvalidPriceOperation, InvalidDataException {
		return getAllStockValue().add(getAccountCosts());
	}
	
	/**
	 * Returns the holdings for this object
	 * @return the holdings for this object
	 */
	private HashMap<String, Integer> getHoldingsMap() {
		return holdings;
	}
	
	/**
	 * Returns the lastSales for this object
	 * @return the lastSales for this object
	 */
	private HashMap<String, Price> getLastSales() {
		return lastSales;
	}
	
	/**
	 * Sets the accountCosts for this object
	 * @param p the price to set the accountCosts to
	 * @throws InvalidDataException if the price is null
	 */
	private void setAccountCosts(Price p) throws InvalidDataException {
		if (p == null) throw new InvalidDataException("The Price cannot be null");
		accountCosts = p;
	}
	
}
