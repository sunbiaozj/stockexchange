package stockexchange.publishers;

import stockexchange.client.User;
import stockexchange.common.AlreadySubscribedException;
import stockexchange.common.InvalidStockException;
import stockexchange.common.NotSubscribedException;

/**
 * An interface to be implemented by any publisher class that requires
 * subscribing and unsubscribing.
 * @author ScottMores
 *
 */
public interface Publisher {
	
	/**
	 * Adds the User as a subscriber to the specified stock.
	 * @param u the user to subscribe.
	 * @param product the stock the user would like information about.
	 * @throws AlreadySubscribedException if the user is already subscribed to receive information
	 * about this stock.
	 */
	void subscribe(User u, String product) throws AlreadySubscribedException, InvalidStockException;
	
	/**
	 * Unsubscribes a User from receiving information about the specified stock.
	 * @param u the user to unsubscribe.
	 * @param product the stock the user would like to unsubscribe from.
	 * @throws NotSubscribedException if the user is not subscribed to receive information about
	 * this stock.
	 */
	void unSubscribe(User u, String product) throws NotSubscribedException, InvalidStockException;
	
}
