package stockexchange.publishers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import stockexchange.client.User;
import stockexchange.common.AlreadySubscribedException;
import stockexchange.common.InvalidStockException;
import stockexchange.common.NotSubscribedException;

/**
 * An abstract class that implements the Publisher interface and keeps track
 * of subscribers to specific stocks as well as all of the Users who subscribe
 * for messages.
 * @author ScottMores
 *
 */
public abstract class PublisherAllImpl implements Publisher {
	
	// A mapping of stocks and the Users who are subscribed to those stocks
	private HashMap<String,HashSet<User>> subscribers;
	// A set of all of the message subscribers, regardless of the stock.
	private HashSet<User> allSubscribers;
	
	/**
	 * Constructs a new PublisherAllImpl object and creates a mapping of stocks
	 * to Users and a set of all Users who subscribe for messages.
	 */
	protected PublisherAllImpl() {
		subscribers = new HashMap<String,HashSet<User>>();
		allSubscribers = new HashSet<User>();
	}
	
	/**
	 * Provides an iterator of all of the User subscribers for a 
	 * specific stock.
	 * @param stock the stock to retrieve subscribers for.
	 * @return an iterator of all the Users subscribed to the stock.
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
	 * Provides an iterator of all of the subscribers to messages in the
	 * system.
	 * @return an iterator of all message subscribers.
	 */
	protected Iterator<User> getAllSubscribers() {
		Iterator<User> iterator = allSubscribers.iterator();
		return iterator;
	}
	
	/**
	 * Subscribes Users to receive messages about a specific stock.
	 * @throws AlreadySubscribedException if the user is already subscribed to receive 
	 * messages about that stock.
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
		allSubscribers.add(u);
	}
	
	/**
	 * Unsubscribes Users from receiving messages about a specific stock.
	 * @throws NotSubscribedException if the user is not already subscribed to receive messages
	 * about that stock.
	 * @throws InvalidStockException if the stock does not exist in the message system.
	 */
	public synchronized void unSubscribe(User u, String product) 
			throws NotSubscribedException, InvalidStockException {
		if (!subscribers.containsKey(product)) throw new InvalidStockException("The stock " + product + "is not in the message system.");
		if (!subscribers.get(product).contains(u)) throw new NotSubscribedException("The user " + u + " is not subscribed to " + product);
		subscribers.get(product).remove(u);
		boolean stillSubscriber = false;
		for (HashSet<User> users : subscribers.values()) {
			if (!stillSubscriber) {
				Iterator<User> iterator = users.iterator();
				User nextUser = null;
				while (iterator.hasNext() && !stillSubscriber) {
					nextUser = iterator.next();
					if (nextUser == u) stillSubscriber = true;
				}
			}
		}
		if (!stillSubscriber) allSubscribers.remove(u);
	}
	
}
