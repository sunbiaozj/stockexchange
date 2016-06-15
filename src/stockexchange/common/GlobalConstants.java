package stockexchange.common;

/**
 * A class that holds global constants for the stockmarket application.
 * @author ScottMores
 *
 */
public class GlobalConstants {
	
	/**
	 * An enumerated type that represents the market state as either CLOSED, PREOPEN or OPEN
	 * @author ScottMores
	 *
	 */
	public enum MarketState {
		CLOSED, PREOPEN, OPEN
	}
	
	/**
	 * An enumerated type that represents the BUY or SELL side of a quote
	 * @author ScottMores
	 *
	 */
	public enum BookSide {
		BUY, SELL
	}
}
