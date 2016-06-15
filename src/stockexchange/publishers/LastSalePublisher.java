package stockexchange.publishers;

import java.util.Iterator;

import stockexchange.client.User;
import stockexchange.common.InvalidDataException;
import stockexchange.price.Price;
import stockexchange.price.PriceFactory;

/**
 * A Publisher that delivers information about the sale for a specific stock in
 * the market.
 * @author ScottMores
 */
public final class LastSalePublisher extends PublisherImpl implements Publisher {
	
	// The sole instance of the LastSalePublisher object.
	private volatile static LastSalePublisher instance;
	
	/**
	 * Constructs a new LastSalePublisher object
	 */
	private LastSalePublisher() {
		super();
	}
	
	/**
	 * Returns the sole instance of the LastSalePublisher class. Checks to see
	 * if an object has been created yet. If it has, it returns a reference to that
	 * object. If it has not, it instantiates an instance of the class.
	 * @return
	 */
	public static LastSalePublisher getInstance() {
		if (instance == null) {
			synchronized (LastSalePublisher.class) {
				if (instance == null) instance = new LastSalePublisher();
			}
		}
		return instance;
	}
	
	/**
	 * Sends out information about the last sale of a particular stock on the market.
	 * @param product the stock that this message refers to.
	 * @param p the last sale price of the stock.
	 * @param v the volume of the last sale of the stock.
	 * @throws InvalidDataException if the product is null or the volume is negative.
	 */
	public synchronized void publishLastSale(String product, Price p, int v) throws InvalidDataException {
		if (product == null) throw new InvalidDataException("The stock cannot be null.");
		if (v < 0) throw new InvalidDataException("The volume cannot be negative.");
		String stock = product;
		Price lastSalePrice = p;
		int lastSaleVolume = v;
		
		if (lastSalePrice == null) lastSalePrice = PriceFactory.makeLimitPrice(0);
		
		Iterator<User> iterator = getSubscribers(stock);
		if (iterator != null) {
			while (iterator.hasNext()) {
				iterator.next().acceptLastSale(stock,lastSalePrice,lastSaleVolume);
			}
		}
		TickerPublisher.getInstance().publishTicker(stock, lastSalePrice);
	}
}
