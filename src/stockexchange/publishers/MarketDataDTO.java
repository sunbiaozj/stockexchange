package stockexchange.publishers;

import stockexchange.price.Price;

/**
 * Encapsulates a set of data elements that detail the values that make up the current market.
 * @author ScottMores
 *
 */
public class MarketDataDTO {
	
	// The stock product that these market data elements refer to.
	public String product;
	
	// The current BUY side price of the stock.
	public Price buyPrice;
	
	// The current BUY side volume of the stock.
	public int buyVolume;
	
	// The current SELL side price of the stock
	public Price sellPrice; 
	
	// The current SELL side volume (quantity) of the stock 
	public int sellVolume; 
	
	/**
	 * Constructs a MarketDataDTO with the given inputs.
	 * @param inProduct The stock product that these market data elements refer to.
	 * @param inBuyPrice The current BUY side price of the stock.
	 * @param inBuyVolume The current BUY side volume of the stock.
	 * @param inSellPrice The current SELL side price of the stock
	 * @param inSellVolume The current SELL side volume (quantity) of the stock 
	 */
	public MarketDataDTO(String inProduct, Price inBuyPrice, int inBuyVolume, Price inSellPrice, int inSellVolume) {
		product = inProduct;
		buyPrice = inBuyPrice;
		buyVolume = inBuyVolume;
		sellPrice = inSellPrice;
		sellVolume = inSellVolume;
	}
	
	/**
	 * Overrides Java's default implementation of toString() to provide a String
	 * representation of this object.
	 * @return a String representation of this object.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(product);
		sb.append(" " + buyVolume + "@" + buyPrice + " x " + sellVolume + "@" + sellPrice);
		return sb.toString();
	}
}
