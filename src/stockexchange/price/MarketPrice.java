package stockexchange.price;

/**
 * A class that represents a MarketPrice.
 * @author ScottMores
 */
public class MarketPrice extends Price {
	
	/**
	 * Checks whether or not this object is a MarketPrice object.
	 * @return true if this object is a MarketPrice and false otherwise.
	 */
	public boolean isMarket() {
		return true;
	}
}
