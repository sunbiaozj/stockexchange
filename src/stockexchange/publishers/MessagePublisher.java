package stockexchange.publishers;

import java.util.Iterator;

import stockexchange.client.User;
import stockexchange.common.InvalidDataException;
import stockexchange.messages.CancelMessage;
import stockexchange.messages.FillMessage;
import stockexchange.messages.MarketMessage;

/**
 * A Publisher that sends information to subscribers related to cancel orders 
 * and fill orders for specific stocks, in addition to sending market messages 
 * to all Users.
 * @author ScottMores
 *
 */
public class MessagePublisher extends PublisherAllImpl implements Publisher {
	
	// The sole instance of the MessagePublisher object.
	private volatile static MessagePublisher instance;
	
	/**
	 * Constructs a new MessagePublisher object
	 */
	private MessagePublisher() {
		super();
	}
	
	/**
	 * Returns the sole instance of the MessagePublisher class. Checks to see
	 * if an object has been created yet. If it has, it returns a reference to that
	 * object. If it has not, it instantiates an instance of the class.
	 * @return
	 */
	public static MessagePublisher getInstance() {
		if (instance == null) {
			synchronized (MessagePublisher.class) {
				if (instance == null) instance = new MessagePublisher();
			}
		}
		return instance;
	}
	
	/**
	 * Sends information about cancelled orders for specific stocks to
	 * Users who are subscribed to receive information about those stocks.
	 * @param cm the cancel message with information about the cancelled order.
	 * @throws InvalidDataException if the cancel message is null.
	 */
	public synchronized void publishCancel(CancelMessage cm) throws InvalidDataException {
		if (cm == null) throw new InvalidDataException("The cancel message cannot be null.");
		String stock = cm.getProduct();
		String username = cm.getUser();
		User user = null;
		
		Iterator<User> iterator = getSubscribers(stock);
		if (iterator != null) {
			User nextUser = iterator.next();
			while (iterator.hasNext() && !nextUser.getUserName().equals(username)) {
				nextUser = iterator.next();
			}
			user = nextUser;
		}
		if (user != null) {
			user.acceptMessage(cm);
		}
	}
	
	/**
	 * Sends information about filled orders for specific stocks to
	 * Users who are subscribed to receive information about those stocks.
	 * @param fm the fill message with information about the filled order.
	 * @throws InvalidDataException if the fill message is null.
	 */
	public synchronized void publishFill(FillMessage fm) throws InvalidDataException {
		if (fm == null) throw new InvalidDataException("The fill message cannot be null.");
		String stock = fm.getProduct();
		String username = fm.getUser();
		User user = null;
		
		Iterator<User> iterator = getSubscribers(stock);
		if (iterator != null) {
			User nextUser = iterator.next();
			while (iterator.hasNext() && !nextUser.getUserName().equals(username)) {
				nextUser = iterator.next();
			}
			user = nextUser;
		}
		if (user != null) {
			user.acceptMessage(fm);
		}
	}
	
	/**
	 * Publishes market messages (OPEN, PREOPEN, CLOSED) to all users.
	 * @param mm the market message to send to all users.
	 * @throws InvalidDataException if the market message is null.
	 */
	public synchronized void publishMarketMessage(MarketMessage mm) throws InvalidDataException {
		if (mm == null) throw new InvalidDataException("The market message cannot be null");
		Iterator<User> iterator = getAllSubscribers();
		while (iterator.hasNext()) {
			iterator.next().acceptMarketMessage(mm.toString());
		}
	}
}
