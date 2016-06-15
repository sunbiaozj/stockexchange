package stockexchange.publishers;

import java.util.HashMap;
import java.util.Iterator;

import stockexchange.client.User;
import stockexchange.common.InvalidDataException;
import stockexchange.price.Price;
import stockexchange.price.PriceFactory;

/**
 * A publisher of ticker messages for stocks.
 * @author ScottMores
 *
 */
public class TickerPublisher extends PublisherImpl implements Publisher {

	// The sole instance of the TickerPublisher object.
	private volatile static TickerPublisher instance;
	// The last known prices of every stock.
	private HashMap<String,Price> lastPrices;
	
	/**
	 * Constructs a new TickerPublisher object
	 */
	private TickerPublisher() {
		super();
		lastPrices = new HashMap<String,Price>();
	}
	
	/**
	 * Returns the sole instance of the TickerPublisher class. Checks to see
	 * if an object has been created yet. If it has, it returns a reference to that
	 * object. If it has not, it instantiates an instance of the class.
	 * @return
	 */
	public static TickerPublisher getInstance() {
		if (instance == null) {
			synchronized (TickerPublisher.class) {
				if (instance == null) instance = new TickerPublisher();
			}
		}
		return instance;
	}
	
	/**
	 * Publishes ticker messages about specific stocks.
	 * @param product the stock to publish a ticker about.
	 * @param p the latest sale price of the stock.
	 * @throws InvalidDataException if the product is null.
	 */
	public synchronized void publishTicker(String product, Price p) throws InvalidDataException {
		if (product == null) throw new InvalidDataException("The product cannot be null.");
		String stock = product;
		Price lastSalePrice = p;
		char arrow;
		if (p == null) lastSalePrice = PriceFactory.makeLimitPrice(0);
		if (lastPrices.containsKey(stock)) {
			Price last = lastPrices.get(stock);
			if (last.compareTo(lastSalePrice) < 0) arrow = (char) 8593;
			else if (last.compareTo(lastSalePrice) > 0) arrow = (char) 8595;
			else arrow = '=';
			lastPrices.put(stock, lastSalePrice);
		}
		else {
			lastPrices.put(stock, lastSalePrice);
			arrow = ' ';
		}
		
		Iterator<User> iterator = getSubscribers(stock);
		if (iterator != null) {
			while (iterator.hasNext()) {
				iterator.next().acceptTicker(stock,lastSalePrice,arrow);
			}
		}
	}
}
