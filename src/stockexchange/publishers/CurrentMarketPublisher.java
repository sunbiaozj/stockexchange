package stockexchange.publishers;
import java.util.Iterator;

import stockexchange.client.User;
import stockexchange.common.InvalidDataException;
import stockexchange.price.Price;
import stockexchange.price.PriceFactory;

/**
 * A Publisher that sends out information about the current market conditions
 * to User subscribers.
 * @author ScottMores
 */
public final class CurrentMarketPublisher extends PublisherImpl implements Publisher {
	
	// The sole instance of the CurrentMarketPublisher object.
	private volatile static CurrentMarketPublisher instance;
	
	/**
	 * Constructs a new CurrentMarketPublisher object
	 */
	private CurrentMarketPublisher() {
		super();
	}
	
	/**
	 * Returns the sole instance of the CurrentMarketPublisher class. Checks to see
	 * if an object has been created yet. If it has, it returns a reference to that
	 * object. If it has not, it instantiates an instance of the class.
	 * @return
	 */
	public static CurrentMarketPublisher getInstance() {
		if (instance == null) {
			synchronized (CurrentMarketPublisher.class) {
				if (instance == null) instance = new CurrentMarketPublisher();
			}
		}
		return instance;
	}
	
	/**
	 * Sends out information about the current market conditions to all subscribers.
	 * @param md a MarketDataDTO that contains information about the current market conditions.
	 * @throws InvalidDataException if the MarketDataDTO is null.
	 */
	public synchronized void publishCurrentMarket(MarketDataDTO md) throws InvalidDataException {
		if (md == null) throw new InvalidDataException("Input DTO cannot be null.");
		String stock = md.product;
		Price buyPrice = md.buyPrice;
		int buyVol = md.buyVolume;
		Price sellPrice = md.sellPrice;
		int sellVol = md.sellVolume;
		
		if (buyPrice == null) buyPrice = PriceFactory.makeLimitPrice(0);
		if (sellPrice == null) sellPrice = PriceFactory.makeLimitPrice(0);
		
		Iterator<User> iterator = getSubscribers(stock);
		if (iterator != null) {
			while (iterator.hasNext()) {
				iterator.next().acceptCurrentMarket(stock, buyPrice, buyVol, sellPrice, sellVol);
			}
		}
	}
}
