package stockexchange.price;

import java.util.HashMap;

/**
 * A class that builds Price objects to represent limit prices and market prices.
 * @author ScottMores
 */
public class PriceFactory {
	
	// Holds the Price objects that have already been created.
	private static final HashMap<Long,Price> prices = new HashMap<Long,Price>();
	// The one and only instance of a MarketPrice object.
	private static final MarketPrice market = new MarketPrice();
	
	/**
	 * Makes a new Price object that represents a limit price.
	 * @param value the String representation of the value to be used for the new Price object.
	 * @return a reference to a Price object that represents the value of the input price.
	 */
	public static Price makeLimitPrice(String value) {
		String priceString = value.replaceAll("[$,]", "");
		double priceDouble = Double.parseDouble(priceString) * 100.0;
		long priceLong = Math.round(priceDouble);
		// Return a reference if a Price object with this value has already been created.
		if (prices.containsKey(priceLong)) return prices.get(priceLong);
		// Add the new Price object to the HashMap so it can be reused later.
		else {
			prices.put(priceLong, new Price(priceLong));
			return prices.get(priceLong);
		}
	}
	
	/**
	 * Makes a new Price object that represents a limit price.
	 * @param value the value in cents that this Price object will represent.
	 * @return a reference to a Price object that represents the value of the input price.
	 */
	public static Price makeLimitPrice(long value) {
		// Return a reference if a Price object with this value has already been created.
		if (prices.containsKey(value)) return prices.get(value);
		// Add the new Price object to the HashMap so it can be reused later.
		else {
			prices.put(value, new Price(value));
			return prices.get(value);
		}
	}
	
	/**
	 * Gives back a reference to this class' MarketPrice member variable market.
	 * @return a reference to the sole MarketPrice object.
	 */
	public static Price makeMarketPrice() {
		return market;
	}
}
