package stockexchange.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import stockexchange.book.ProductService;
import stockexchange.common.AlreadyConnectedException;
import stockexchange.common.AlreadySubscribedException;
import stockexchange.common.DataValidationException;
import stockexchange.common.GlobalConstants;
import stockexchange.common.GlobalConstants.BookSide;
import stockexchange.common.InvalidConnectionIdException;
import stockexchange.common.InvalidDataException;
import stockexchange.common.InvalidMarketStateException;
import stockexchange.common.InvalidStockException;
import stockexchange.common.NoSuchProductException;
import stockexchange.common.NotSubscribedException;
import stockexchange.common.OrderNotFoundException;
import stockexchange.common.UserNotConnectedException;
import stockexchange.price.Price;
import stockexchange.publishers.CurrentMarketPublisher;
import stockexchange.publishers.LastSalePublisher;
import stockexchange.publishers.MessagePublisher;
import stockexchange.publishers.TickerPublisher;
import stockexchange.tradable.Order;
import stockexchange.tradable.Quote;
import stockexchange.tradable.TradableDTO;

/**
 * A class that acts as a façade between a user and the trading system
 * @author ScottMores
 *
 */
public final class UserCommandService {
	
	/**	The sole instance of the UserCommandService class */
	private volatile static UserCommandService instance;
	/** Holds user name and connection id pairs */
	private HashMap<String, Long> connectedUserIds = new HashMap<String, Long>();
	/** Holds username and user object pairs */
	private HashMap<String, User> connectedUsers = new HashMap<String, User>();
	/** Holds user name and connection-time pairs */
	private HashMap<String, Long> connectedTime = new HashMap<String, Long>();
	
	/**
	 * Retrieves the sole instance of the UserCommandService class
	 * @return the sole instance of the UserCommandService class
	 */
	public static UserCommandService getInstance() {
		if (instance == null) {
			synchronized(UserCommandService.class) {
				if (instance == null) instance = new UserCommandService();
			}
		}
		return instance;
	}
	
	/**
	 * Verifies the integrity of the username and connection id passed in by many of the method calls in this class
	 * @param userName the username to verify
	 * @param connId the connection id to verify
	 * @throws UserNotConnectedException if the user is not currently connected
	 * @throws InvalidConnectionIdException if ID does not match the connection ID for the user
	 * @throws InvalidDataException if the username is empty or null
	 */
	private void verifyUser(String userName, long connId) throws UserNotConnectedException, InvalidConnectionIdException, InvalidDataException {
		if (userName == null || userName.isEmpty()) throw new InvalidDataException("The username cannot be null or empty");
		if (!getConnectedUserIds().containsKey(userName)) 
			throw new UserNotConnectedException("The user " + userName + " is not connected");
		if (getConnectedUserIds().get(userName) != connId) 
			throw new InvalidConnectionIdException("The connection ID " + connId + " does not match the ID for " + userName);
	}
	
	/**
	 * Connects the user to the trading system
	 * @param user the user to connect to the trading system
	 * @return the connection id for the user
	 * @throws InvalidDataException if the user is null
	 * @throws AlreadyConnectedException if the user is already connected to the trading system
	 */
	public synchronized long connect(User user) throws InvalidDataException, AlreadyConnectedException {
		if (user == null) throw new InvalidDataException("The user cannot be null");
		if (getConnectedUsers().containsKey(user.getUserName())) throw new AlreadyConnectedException("The user " + user.getUserName() + " is already connected");
		long time = System.nanoTime();
		getConnectedUserIds().put(user.getUserName(), time);
		getConnectedUsers().put(user.getUserName(), user);
		getConnectedTime().put(user.getUserName(), System.currentTimeMillis());
		return time;
	}
	
	/**
	 * Disconnects the user from the trading system
	 * @param userName the user to disconnect
	 * @param connId the connection id of the user to disconnect
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the connection ID does not match the user's actual connection ID
	 * @throws InvalidDataException if the username is null or empty
	 */
	public synchronized void disConnect(String userName, long connId) throws UserNotConnectedException, InvalidConnectionIdException, InvalidDataException {
		if (userName == null || userName.isEmpty()) throw new InvalidDataException("The username cannot be null or empty");
		verifyUser(userName, connId);
		getConnectedUserIds().remove(userName);
		getConnectedUsers().remove(userName);
		getConnectedTime().remove(userName);
	}
	
	/**
	 * Forwards the call of “getBookDepth” to the ProductService
	 * @param userName the username to get the book depth for
	 * @param connId the connection id of the user 
	 * @param product the stock to get the book depth of
	 * @return the book depth of the provided stock
	 * @throws InvalidDataException if the username or product are null or empty
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the connection ID does not match the user's actual connection ID
	 * @throws NoSuchProductException if the product does not exist in the trading system
	 */
	public String[][] getBookDepth(String userName, long connId, String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, NoSuchProductException {
		if (userName == null  || userName.isEmpty()) throw new InvalidDataException("The username cannot be null or empty");
		if (product == null || product.isEmpty()) throw new InvalidDataException("The product cannot be null or empty");
		verifyUser(userName, connId);
		return ProductService.getInstance().getBookDepth(product);
	}
	
	/**
	 * Forwards the call of “getMarketState” to the ProductService
	 * @param userName the use who wants the market state
	 * @param connId the connection id of the user
	 * @return the current market state
	 * @throws InvalidDataException if the username or connection id are null or empty
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the connection ID does not match the actual connection ID for the user
	 */
	public String getMarketState(String userName, long connId) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException {
		if (userName == null || userName.isEmpty()) throw new InvalidDataException("The username cannot be null or empty");
		verifyUser(userName, connId);
		return ProductService.getInstance().getMarketState().toString();
	}
	
	/**
	 * Forwards the call of “getOrdersWithRemainingQty” to the ProductService
	 * @param userName ther user who watns the orders with remaining quantity
	 * @param connId the connection ID of the user
	 * @param product the stock to get the remaining quantity for
	 * @return a list of all the orders with remaining quantities for specified the user and stock
	 * @throws InvalidDataException if the username or product are null or empty
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the provided connection ID does not match the actual connection ID for the user
	 */
	public synchronized ArrayList<TradableDTO> getOrdersWithRemainingQty(String userName, 
			long connId, String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException {
		if (userName == null || userName.isEmpty()) throw new InvalidDataException("The username cannot be null or empty");
		if (product == null || product.isEmpty()) throw new InvalidDataException("The product cannot be null or empty");
		verifyUser(userName, connId);
		return ProductService.getInstance().getOrdersWithRemainingQty(userName, product);
	}
	
	/**
	 * Returns a sorted list of the available stocks on this system, received from the ProductService
	 * @param userName the user who wants the available stocks
	 * @param connId the connection ID of the user
	 * @return an ArrayList of all the products with product books
	 * @throws InvalidDataException if the username is null or empty
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the provided connection id does not match the actual connection id for the user
	 */
	public ArrayList<String> getProducts(String userName, long connId) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException {
		if (userName == null || userName.isEmpty()) throw new InvalidDataException("The username cannot be null or empty");
		verifyUser(userName, connId);
		ArrayList<String> products = ProductService.getInstance().getProductList();
		Collections.sort(products);
		return products;
	}
	
	/**
	 * Creates an order object using the data passed in, and forwards the order to the 
	 * ProductService’s “submitOrder” method
	 * @param userName the user who wants to submit an order
	 * @param connId the connection id for the user
	 * @param product the stock the user wants to submit an order for
	 * @param price the price of the order
	 * @param volume the volume of the order
	 * @param side the BUY or SELL side of the order
	 * @return the orderID of the submitted order
	 * @throws InvalidDataException if the username, product, price or side are null or empty
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the provided connection id does not match the user's actual connection id
	 * @throws InvalidMarketStateException if the market is CLOSED or the market is PREOPEN and the order is a market price
	 * @throws NoSuchProductException if the product for the order does not exist
	 */
	public String submitOrder(String userName, long connId, String product, Price price, int volume,
			GlobalConstants.BookSide side) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, InvalidMarketStateException, NoSuchProductException {
		if (userName == null || userName.isEmpty()) throw new InvalidDataException("The userName cannot be null or empty");
		if (product == null || product.isEmpty()) throw new InvalidDataException("The product cannot be null or empty");
		if (price == null) throw new InvalidDataException("The price cannot be null");
		if (side == null) throw new InvalidDataException("The side cannot be null");
		verifyUser(userName, connId);
		Order order = new Order(userName, product, price, volume, side);
		return ProductService.getInstance().submitOrder(order);
	}
	
	/**
	 * Forwards the provided information to the ProductService’s “submitOrderCancel” method
	 * @param userName the user who wants to cancel an order
	 * @param connId the connection id of the user
	 * @param product the stock to cancel the order for
	 * @param side the BUY or SELL side of the cancel request
	 * @param orderId the order id of the order to cancel
	 * @throws InvalidDataException if the username, product, side or order id are null or empty
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the provided connection id does not match the user's actual connection id
	 * @throws InvalidMarketStateException if the market is CLOSED or the market is PREOPEN and the order is a market price
	 * @throws NoSuchProductException if the product for the order does not exist
	 * @throws OrderNotFoundException if the order id does not exist in a product book
	 */
	public void submitOrderCancel(String userName, long connId, String product, BookSide side, String orderId) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, InvalidMarketStateException, NoSuchProductException, OrderNotFoundException {
		if (userName == null || userName.isEmpty()) throw new InvalidDataException("The userName cannot be null or empty");
		if (product == null || product.isEmpty()) throw new InvalidDataException("The product cannot be null or empty");
		if (side == null) throw new InvalidDataException("The side cannot be null");
		if (orderId == null || orderId.isEmpty()) throw new InvalidDataException("The userName cannot be null or empty");
		verifyUser(userName, connId);
		ProductService.getInstance().submitOrderCancel(product, side, orderId);
	}
	
	/**
	 * Creates a quote object using the data passed in, and forwards the quote to the ProductService’s “submitQuote” method
	 * @param userName the user who wants to submit a quote
	 * @param connId the connection ID of the user
	 * @param product the stock to make a quote for
	 * @param bPrice the buy price of the quote
	 * @param bVolume the buy volume of the quote
	 * @param sPrice the sell price of the quote
	 * @param sVolume the sell volume of the quote
	 * @throws InvalidDataException if the username, product, buy price or sell price are null or empty
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the provided connection ID doesn't match the user's actual connection id
	 * @throws InvalidMarketStateException if the market is currently closed
	 * @throws NoSuchProductException if the product does not exist
	 * @throws DataValidationException  if the quote's SELL price is less than or equal to the BUY price or if either price in the quote is less than or equal to 0
	 */
	public void submitQuote(String userName, long connId, String product, Price bPrice, 
			int bVolume, Price sPrice, int sVolume) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, InvalidMarketStateException, NoSuchProductException, DataValidationException {
		if (userName == null || userName.isEmpty()) throw new InvalidDataException("The userName cannot be null or empty");
		if (product == null || product.isEmpty()) throw new InvalidDataException("The product cannot be null or empty");
		if (bPrice == null) throw new InvalidDataException("The buy price cannot be null");
		if (sPrice == null) throw new InvalidDataException("The sell price cannot be null");
		verifyUser(userName, connId);
		Quote q = new Quote(userName, product, bPrice, bVolume, sPrice, sVolume);
		ProductService.getInstance().submitQuote(q);
	}
	
	/**
	 * Forwards the provided data to the ProductService’s “submitQuoteCancel” method
	 * @param userName the user who wants to cancel a quote
	 * @param connId the connection id of the user
	 * @param product the stock to cancel a qute for
	 * @throws InvalidDataException if the username or product are null or empty
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the prodived connection ID doesn't match the user's actual connection id
	 * @throws InvalidMarketStateException if the market is closed
	 * @throws NoSuchProductException if the product does not exist
	 */
	public void submitQuoteCancel(String userName, long connId, String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, InvalidMarketStateException, NoSuchProductException {
		if (userName == null || userName.isEmpty()) throw new InvalidDataException("The userName cannot be null or empty");
		if (product == null || product.isEmpty()) throw new InvalidDataException("The product cannot be null or empty");
		verifyUser(userName, connId);
		ProductService.getInstance().submitQuoteCancel(userName, product);
	}
	
	/**
	 * Forwards the subscription request to the CurrentMarketPublisher
	 * @param userName the user who wants to subscribe to current market updates
	 * @param connId the connection id of the user
	 * @param product the stock to receive current market updates for
	 * @throws InvalidDataException if the username or product are null or empty
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the provided connection id doesn't match the user's actual connection id
	 * @throws AlreadySubscribedException if the user is already subscribed to receive updates about this stock
	 * @throws InvalidStockException if the product is null
	 */
	public void subscribeCurrentMarket(String userName, long connId, String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, AlreadySubscribedException, InvalidStockException {
		if (userName == null || userName.isEmpty()) throw new InvalidDataException("The userName cannot be null or empty");
		if (product == null || product.isEmpty()) throw new InvalidDataException("The product cannot be null or empty");
		verifyUser(userName, connId);
		CurrentMarketPublisher.getInstance().subscribe(getConnectedUsers().get(userName), product);
	}
	
	/**
	 * Forwards the subscription request to the LastSalePublisher
	 * @param userName the user who wants to subscribe to the last sale publisher
	 * @param connId the connection id of the user
	 * @param product the stock to receive last sale messages about
	 * @throws InvalidDataException if the username or product are null or empty
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the provided connection id doesn't match the user's actual connection id
	 * @throws AlreadySubscribedException if the user is already subscribed to receive last sale updates for this stock
	 * @throws InvalidStockException if the stock is null
	 */
	public void subscribeLastSale(String userName, long connId, String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, AlreadySubscribedException, InvalidStockException {
		if (userName == null || userName.isEmpty()) throw new InvalidDataException("The userName cannot be null or empty");
		if (product == null || product.isEmpty()) throw new InvalidDataException("The product cannot be null or empty");
		verifyUser(userName, connId);
		LastSalePublisher.getInstance().subscribe(getConnectedUsers().get(userName), product);
	}
	
	/**
	 * Forwards the subscription request to the MessagePublisher
	 * @param userName the user who wants to subscribe
	 * @param connId the connection ID of the user
	 * @param product the stock to receive messages about
	 * @throws InvalidDataException if the username or product are null or empty
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the provided connection id doesn't match the user's actual connection id
	 * @throws AlreadySubscribedException if the user is already subscribed to receive updates about this stock
	 * @throws InvalidStockException if the stock is null
	 */
	public void subscribeMessages(String userName, long connId, String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, AlreadySubscribedException, InvalidStockException {
		if (userName == null || userName.isEmpty()) throw new InvalidDataException("The userName cannot be null or empty");
		if (product == null || product.isEmpty()) throw new InvalidDataException("The product cannot be null or empty");
		verifyUser(userName, connId);
		MessagePublisher.getInstance().subscribe(getConnectedUsers().get(userName), product);
	}
	
	/**
	 * Forwards the subscription request to the TickerPublisher
	 * @param userName the user who wants to subscribe to the ticker publisher
	 * @param connId the connection id of the user
	 * @param product the stock to receive ticker updates about
	 * @throws InvalidDataException if the username or product are null or empty
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the provided connection id doesn't match the user's actual connection id
	 * @throws AlreadySubscribedException if the user is already subscribed to receive ticker messages for this stock
	 * @throws InvalidStockException if the stock is null
	 */
	public void subscribeTicker(String userName, long connId, String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, AlreadySubscribedException, InvalidStockException {
		if (userName == null || userName.isEmpty()) throw new InvalidDataException("The userName cannot be null or empty");
		if (product == null || product.isEmpty()) throw new InvalidDataException("The product cannot be null or empty");
		verifyUser(userName, connId);
		TickerPublisher.getInstance().subscribe(getConnectedUsers().get(userName), product);
	}
	
	/**
	 * Forwards the unsubscribe request to the CurrentMarketPublisher
	 * @param userName the user who wants to unsubscribe
	 * @param connId the connection id of the user
	 * @param product the stock to unsuscribe from
	 * @throws InvalidDataException if the username or product are null or empty
	 * @throws NotSubscribedException if the user is not already subscribed to receive these messages
	 * @throws InvalidStockException if the stock is null
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the provided connection id doesn't match the user's actual connection id
	 */
	public void unSubscribeCurrentMarket(String userName, long connId, String product) throws InvalidDataException, NotSubscribedException, InvalidStockException, UserNotConnectedException, InvalidConnectionIdException {
		if (userName == null || userName.isEmpty()) throw new InvalidDataException("The userName cannot be null or empty");
		if (product == null || product.isEmpty()) throw new InvalidDataException("The product cannot be null or empty");
		verifyUser(userName, connId);
		CurrentMarketPublisher.getInstance().unSubscribe(getConnectedUsers().get(userName), product);
	}
	
	/**
	 * Forwards the unsubscribe request to the LastSalePublisher
	 * @param userName the user who wants to unsubscribe
	 * @param connId the connection id of the user
	 * @param product the stock to unsubscribe from
	 * @throws InvalidDataException if the username or product are null or empty
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the provided connection id does not match the user's actual connection id
	 * @throws NotSubscribedException if the user is not already subscribed to receive these messages
	 * @throws InvalidStockException if the stock is null
	 */
	public void unSubscribeLastSale(String userName, long connId, String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, NotSubscribedException, InvalidStockException {
		if (userName == null || userName.isEmpty()) throw new InvalidDataException("The userName cannot be null or empty");
		if (product == null || product.isEmpty()) throw new InvalidDataException("The product cannot be null or empty");
		verifyUser(userName, connId);
		LastSalePublisher.getInstance().unSubscribe(getConnectedUsers().get(userName), product);
	}
	
	/**
	 * Forwards the unsubscribe request to the TickerPublisher
	 * @param userName the user who wants to unsubscribe
	 * @param connId the connection id of the user
	 * @param product the stock to unsubscribe from
	 * @throws InvalidDataException if the username or product are empty or null
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the provided connection id does not match the user's actual connection id
	 * @throws NotSubscribedException if the user is not currently subscribed to receive these messages
	 * @throws InvalidStockException if the stock is null
	 */
	public void unSubscribeTicker(String userName, long connId, String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, NotSubscribedException, InvalidStockException {
		if (userName == null || userName.isEmpty()) throw new InvalidDataException("The userName cannot be null or empty");
		if (product == null || product.isEmpty()) throw new InvalidDataException("The product cannot be null or empty");
		verifyUser(userName, connId);
		TickerPublisher.getInstance().unSubscribe(getConnectedUsers().get(userName), product);
	}
	
	/**
	 * Forwards the unsubscribe request to the MessagePublisher
	 * @param userName the user who wants to unsubscribe
	 * @param connId the connection id of the user
	 * @param product the stock to unsubscribe from
	 * @throws InvalidDataException if the username or product are null or empty
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the provided connection id doesn't match the user's actual connection id
	 * @throws NotSubscribedException if the user is not currently subscribed to receive these messages
	 * @throws InvalidStockException if the stock is null
	 */
	public void unSubscribeMessages(String userName, long connId, String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, NotSubscribedException, InvalidStockException {
		if (userName == null || userName.isEmpty()) throw new InvalidDataException("The userName cannot be null or empty");
		if (product == null || product.isEmpty()) throw new InvalidDataException("The product cannot be null or empty");
		verifyUser(userName, connId);
		MessagePublisher.getInstance().unSubscribe(getConnectedUsers().get(userName), product);
	}
	
	/**
	 * Returns the connectedUserIds map
	 * @return the connectedUserIds map
	 */
	private HashMap<String, Long> getConnectedUserIds() {
		return connectedUserIds;
	}
	
	/**
	 * Returns the connectedUsers map
	 * @return the connectedUsers map
	 */
	private HashMap<String, User> getConnectedUsers() {
		return connectedUsers;
	}
	
	/**
	 * Returns the connectedTime map
	 * @return the connectedTime map
	 */
	private HashMap<String, Long> getConnectedTime() {
		return connectedTime;
	}
	
	/**
	 * Private constructor that can only be called from within this class
	 */
	private UserCommandService(){}
}
