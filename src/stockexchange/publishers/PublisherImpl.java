package stockexchange.publishers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import stockexchange.client.User;
import stockexchange.common.AlreadySubscribedException;
import stockexchange.common.InvalidStockException;
import stockexchange.common.NotSubscribedException;
import stockexchange.publishers.Publisher;

/**
 * An abstract class that implements the Publisher interface and keeps track
 * of subscribers to specific stocks.
 * @author ScottMores
 *
 */
public abstract class PublisherImpl implements Publisher {
	

	// A mapping of stocks and the Users who are subscribed to those stocks
	private HashMap<String,HashSet<User>> subscribers;
	
	/**
	 * Constructs a new PublisherImpl and creates a HashMap for subscribers
	 * of stocks.
	 */
	protected PublisherImpl() {
		subscribers = new HashMap<String,HashSet<User>>();
	}
	
	/**
	 * Provides access to an iterator of User subscribers for a given stock.
	 * @param stock the stock to provide the subscriber iterator for.
	 * @return
	 */
	protected Iterator<User> getSubscribers(String stock) {
		Iterator<User> iterator = null;
		if (subscribers.containsKey(stock)) {
			iterator = subscribers.get(stock).iterator();
			return iterator;
		}
		return iterator;
	}
	
	/**
	 * Subscribes a User to receive updates about a specific stock.
	 * @param u the User who wants to subscribe.
	 * @param product the stock the User wants to subscribe to receive messages about.
	 * @throws AlreadySubscribedException if the user is already subscribed to
	 * receive updates about the stock.
	 * @throws InvalidStockException if the product String is null.
	 */
	public synchronized void subscribe(User u, String product) 
			throws AlreadySubscribedException, InvalidStockException {
		if (product == null) throw new InvalidStockException("You cannot subscribe to a null stock.");
		if (subscribers.containsKey(product)) {
			if (subscribers.get(product).contains(u)) throw new AlreadySubscribedException("The user " + u + " is already subscribed to " + product);
			subscribers.get(product).add(u);
		}
		else {
			subscribers.put(product, new HashSet<User>());
			subscribers.get(product).add(u);
		}
	}
	
	/**
	 * Unsubscribes a User from receiving updates about a specific stock.
	 * @param u the User who wants to unsubscribe.
	 * @param product the stock the User wants to stop receiving messages about.
	 * @throws NotSubscribedException if the user is not subscribed to
	 * receive updates about the stock.
	 * @throws InvalidStockException if the stock is not in the message system.
	 */
	public synchronized void unSubscribe(User u, String product) 
			throws NotSubscribedException, InvalidStockException {
		if (!subscribers.containsKey(product)) throw new InvalidStockException("The stock " + product + "is not in the message system.");
		if (!subscribers.get(product).contains(u)) throw new NotSubscribedException("The user " + u + " is not subscribed to " + product);
		subscribers.get(product).remove(u);
	}
}

