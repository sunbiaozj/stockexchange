package stockexchange.tradable;

import stockexchange.common.GlobalConstants.BookSide;
import stockexchange.common.InvalidDataException;
import stockexchange.price.Price;

/**
 * A class that represents the prices and volumes of stocks that the user is willing to 
 * "BUY" or "SELL" shares of.
 * @author ScottMores
 */
public class Quote {
	
	// The user name attached to this Quote.
	private String userName;
	// The stock symbol (product) attached to this Quote.
	private String stockSymbol;
	// The buy side of this Quote.
	private QuoteSide buy;
	// The sell side of this Quote.
	private QuoteSide sell;
	
	/**
	 * Constructs a new Quote object using the provided input values.
	 * @param inUserName the user name to attach to this Quote.
	 * @param inStockSymbol the stock symbol (product) to attach to this Quote.
	 * @param inBuyPrice the buy price to attach to this Quote.
	 * @param inBuyVolume the buy volume to attach to this Quote.
	 * @param inSellPrice the sell price to attach to this Quote.
	 * @param inSellVolume the sell volume to attach to this Quote.
	 * @throws InvalidDataException if the buy or sell volumes are less than or equal to 0 or 
	 * if the buy or sell prices are less than or equal to 0.
	 */
	public Quote(String inUserName, String inStockSymbol, Price inBuyPrice, int inBuyVolume, 
			Price inSellPrice, int inSellVolume) throws InvalidDataException {
		userName = inUserName;
		stockSymbol = inStockSymbol;
		buy = new QuoteSide(userName, stockSymbol, inBuyPrice, inBuyVolume, BookSide.BUY);
		sell = new QuoteSide(userName, stockSymbol, inSellPrice, inSellVolume, BookSide.SELL);
	}
	
	/**
	 * Gets the user name associated with this object.
	 * @return the user name for this Quote.
	 */
	public String getUserName() {
		return userName;
	}
	
	/**
	 * Gets the stock symbol (product) associated with this object.
	 * @return the stock symbol (product) for this Quote.
	 */
	public String getProduct() {
		return stockSymbol;
	}
	
	/**
	 * Gets a copy of the "BUY" or "SELL" QuoteSide associated with this object.
	 * @param sideIn the "BUY" or "SELL" side of the Quote that is being requested.
	 * @return a copy of the requested QuoteSide object.
	 */
	public QuoteSide getQuoteSide(BookSide sideIn) {
		QuoteSide quoteSideCopy = null;
		quoteSideCopy = (sideIn.equals(BookSide.BUY)) ? new QuoteSide(buy) : new QuoteSide(sell);
		return quoteSideCopy;
	}
	
	/**
	 * Overrides Java's default toString() implementation and constructs a String representation of the object.
	 */
	public String toString() {
		return getUserName() + " quote: " + getProduct() + " " + buy.getPrice() + " x " + buy.getRemainingVolume() 
				+ " (Original Vol: " + buy.getOriginalVolume() + ", CXL'd Vol: " + buy.getCancelledVolume()
				+ ") [" + buy.getId() + "]"+ " - " + sell.getPrice() + " x " + sell.getRemainingVolume()
				+ " (Original Vol: " + sell.getOriginalVolume() + ", CXL'd Vol: " + sell.getCancelledVolume()
				+ ") [" + sell.getId() + "]";
	}
}
