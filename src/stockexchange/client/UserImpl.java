package stockexchange.client;

import java.sql.Timestamp;
import java.util.ArrayList;

import stockexchange.common.AlreadyConnectedException;
import stockexchange.common.AlreadySubscribedException;
import stockexchange.common.DataValidationException;
import stockexchange.common.GlobalConstants.BookSide;
import stockexchange.common.InvalidConnectionIdException;
import stockexchange.common.InvalidDataException;
import stockexchange.common.InvalidMarketStateException;
import stockexchange.common.InvalidPriceOperation;
import stockexchange.common.InvalidStockException;
import stockexchange.common.NoSuchProductException;
import stockexchange.common.OrderNotFoundException;
import stockexchange.common.UserNotConnectedException;
import stockexchange.gui.UserDisplayManager;
import stockexchange.messages.CancelMessage;
import stockexchange.messages.FillMessage;
import stockexchange.price.Price;
import stockexchange.tradable.TradableDTO;

/**
 * Class that implements the User interface and represents a real user in the trading system
 * @author ScottMores
 *
 */
public class UserImpl implements User {
	/** The user's username */
	private String userName;
	/** The user's connection Id to the trading system*/
	private long connId;
	/** A list of available stock in the trading system*/
	private ArrayList<String> availableStocks;
	/** A list of orders this user has submitted*/
	private ArrayList<TradableUserData> submittedOrders = new ArrayList<TradableUserData>();
	/** The user's profit and loss information*/
	private Position marketPosition;
	/** The display that the user interacts with*/
	private UserDisplayManager display;
	
	/**
	 * Constructs a new UserImpl with the provided username
	 * @param userNameIn the username to use for this user
	 * @throws InvalidDataException if the username is null or empty
	 */
	public UserImpl(String userNameIn) throws InvalidDataException {
		setUserName(userNameIn);
		setMarketPosition();
	}
	
	/**
	 * Accepts the last sale for this user and updates the user's position and display
	 * @param product the stock associated with the last sale
	 * @param the price of the last sale
	 * @param the volume of the last sale
	 */
	public void acceptLastSale(String product, Price price, int volume) {
		try {
			if (getDisplay() != null) getDisplay().updateLastSale(product, price, volume);
			getPosition().updateLastSale(product, price);
		}
		catch (InvalidDataException e) {
			System.out.println("Encountered error while updating last sale for user's position: " + e.getMessage());
		}
		catch (Exception e) {
			System.out.println("Encountered error while updating last sale for display: " + e.getMessage());
		}
	}
	
	/**
	 * Displays the Fill Message in the market display and forwards the data to the Position object
	 * @param fm the fill message to accept
	 */
	public void acceptMessage(FillMessage fm) {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String summary = "{" + timestamp + "} Fill Message: " + fm.getBookSide() + " " + fm.getVolume() + " " + fm.getProduct() 
		+ " at " + fm.getPrice() + " " + fm.getDetails() + " [Tradable Id: " + fm.getId() + "]\n"; 
		try {
			if (getDisplay() != null) getDisplay().updateMarketActivity(summary);
			getPosition().updatePosition(fm.getProduct(), fm.getPrice(), fm.getBookSide(), fm.getVolume());
		}
		catch (InvalidDataException e){
			System.out.println("Encountered error while updating user's position: " + e.getMessage());
		} 
		catch (InvalidPriceOperation e) {
			System.out.println("Encountered error while updating user's position: " + e.getMessage());
		}
		catch (Exception e) {
			System.out.println("Encountered error while displaying market activity update: " + e.getMessage());
		}
	}
	
	/**
	 * Displays the Cancel Message in the market display
	 * @param cm the cancel message to display
	 */
	public void acceptMessage(CancelMessage cm) {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String summary = "{" + timestamp + "} Cancel Message: " + cm.getBookSide() + " " + cm.getVolume() + " " + cm.getProduct() 
				+ " at " + cm.getPrice() + " " + cm.getDetails() + " [Tradable Id: " + cm.getId() + "]\n"; 
		try {
			if (getDisplay() != null) getDisplay().updateMarketActivity(summary);
		}
		catch (Exception e) {
			System.out.println("Encountered error while displaying market activity update: " + e.getMessage());
		}
	}
	
	/**
	 * Displays the market message in the market display
	 * @param message the market message to display
	 */
	public void acceptMarketMessage(String message) {
		try {
			if (getDisplay() != null) getDisplay().updateMarketState(message);
		}
		catch (Exception e) {
			System.out.println("Encountered error while displaying market state update: " + e.getMessage());
		}
	}
	
	/**
	 * Displays the ticker data in the market display
	 * @param product the stock to display
	 * @param price the price to display
	 * @param direction the direction to display
	 */
	public void acceptTicker(String product, Price price, char direction) {
		try {
			if (getDisplay() != null) getDisplay().updateTicker(product, price, direction);
		}
		catch (Exception e) {
			System.out.println("Encountered error while displaying ticker update: " + e.getMessage());
		}
	}
	
	/**
	 * Displays the current market data to the market display
	 * @param product the stock this update refers to
	 * @param bPrice the buy price of the update
	 * @param bVolume the buy volume of the update
	 * @param sPrice the sell price of the update
	 * @param sVolume the sell volume of the update
	 */
	public void acceptCurrentMarket(String product, Price bPrice, int bVolume, Price sPrice, int sVolume) {
		try {
			if (getDisplay() != null) getDisplay().updateMarketData(product, bPrice, bVolume, sPrice, sVolume);
		}
		catch (Exception e) {
			System.out.println("Encountered error while displaying market data update: " + e.getMessage());
		}
	}
	
	/**
	 * Connects the user to the trading system
	 * @throws InvalidDataException if any data members are empty or null
	 * @throws AlreadyConnectedException if the user is already connected to the system while trying to connect
	 * @throws UserNotConnectedException if the user is not connected to the system while retrieving available stocks
	 * @throws InvalidConnectionIdException if the provided connection id doesn't match the user's actual connection id
	 */
	public void connect() throws InvalidDataException, AlreadyConnectedException, UserNotConnectedException, InvalidConnectionIdException {
		setConnId(UserCommandService.getInstance().connect(this));
		setAvailableStocks(UserCommandService.getInstance().getProducts(getUserName(), getConnId()));
	}
	
	/**
	 * Disconnects the user from the trading system
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the provided connection id doesn't match the user's actual connection id
	 * @throws InvalidDataException if any of the parameters are null or empty
	 */
	public void disConnect() throws UserNotConnectedException, InvalidConnectionIdException, InvalidDataException {
		UserCommandService.getInstance().disConnect(getUserName(), getConnId());
	}
	
	/**
	 * Activates the market display
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 */
	public void showMarketDisplay() throws UserNotConnectedException, Exception {
		if (getProductList() == null) throw new UserNotConnectedException("The user " + userName + " is not connected to the trading system");
		if (getDisplay() == null) setDisplay();
		getDisplay().showMarketDisplay();
	}
	
	/**
	 * Forwards the new order request to the user command service and saves the resulting order id
	 * @param product the stock for the order
	 * @param price the price of the order
	 * @param volume the volume of the order
	 * @param side the BUY or SELL side of the order
	 * @return the orderId of the submitted order
	 * @throws InvalidDataException if any of the paramets are null or empty
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the provided connection id doesn't match the user's actual connection id
	 * @throws InvalidMarketStateException if the market is CLOSED or the market is PREOPEN and the order is a market price
	 * @throws NoSuchProductException if the stock doesn't exist
	 */
	public String submitOrder(String product, Price price, int volume, BookSide side) 
			throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, InvalidMarketStateException, NoSuchProductException {
		String id = UserCommandService.getInstance().submitOrder(getUserName(), getConnId(), product, price, volume, side);
		TradableUserData data = new TradableUserData(getUserName(), product, side, id);
		getOrderIds().add(data);
		return id;
	}
	
	/**
	 * Forwards the order cancel request to the user command service
	 * @param product the stock to cancel
	 * @param side the BUY or SELL side to cancel
	 * @param orderId the id of the order to cancel
	 * @throws InvalidDataException if any of the parameters are null or empty
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the provided connection id doesn't match the user's actual connection id
	 * @throws InvalidMarketStateException if the market is CLOSED or the market is PREOPEN and the order is a market price
	 * @throws NoSuchProductException if the stock doesn't exist
	 * @throws OrderNotFoundException if the order id doesn't exist
	 */
	public void submitOrderCancel(String product, BookSide side, String orderId) 
			throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, InvalidMarketStateException, NoSuchProductException, OrderNotFoundException {
		UserCommandService.getInstance().submitOrderCancel(getUserName(), getConnId(), product, side, orderId);
	}
	
	/**
	 * Forwards the new quote request to the user command service 
	 * @param product the stock for the quote
	 * @param bPrice the buy price of the quote
	 * @param bVolume the buy volume of the quote
	 * @param sPrice the sell price of the quote
	 * @param sVolume the sell volume of the quote
	 * @throws InvalidDataException if any parameters are null or empty
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the provided connection id doesn't match the user's actual connection id
	 * @throws InvalidMarketStateException if the market is currently closed
	 * @throws NoSuchProductException if the stock doesn't exist 
	 * @throws DataValidationException if the quote's SELL price is less than or equal to the BUY price or if either price in the quote is less than or equal to 0
	 */
	public void submitQuote(String product, Price bPrice, int bVolume, Price sPrice, int sVolume) 
			throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, InvalidMarketStateException, NoSuchProductException, DataValidationException {
		UserCommandService.getInstance().submitQuote(getUserName(), getConnId(), product, bPrice, bVolume, sPrice, sVolume);
	}
	
	/**
	 * Forwards the quote cancel request to the user command service
	 * @param product the stock to cancel a quote for
	 * @throws InvalidDataException if the product is null or empty
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the provided connection ID doesn't match the user's actual connection id
	 * @throws InvalidMarketStateException if the market is closed
	 * @throws NoSuchProductException if the product does not exist
	 */
	public void submitQuoteCancel(String product) 
			throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, InvalidMarketStateException, NoSuchProductException {
		UserCommandService.getInstance().submitQuoteCancel(getUserName(), getConnId(), product);
	}
	
	/**
	 * Forwards the current market description to the user command service
	 * @param product the stock to subscribe to
	 * @throws InvalidDataException if the username or product are null or empty
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the provided connection id doesn't match the user's actual connection id
	 * @throws AlreadySubscribedException if the user is already subscribed to receive updates about this stock
	 * @throws InvalidStockException if the product is null
	 */
	public void subscribeCurrentMarket(String product) 
			throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, AlreadySubscribedException, InvalidStockException {
		UserCommandService.getInstance().subscribeCurrentMarket(getUserName(), getConnId(), product);
	}
	
	/**
	 * Forwards the last sale subscription to the user command service
	 * @param product the stock to subscribe to
	 * @throws InvalidDataException if the username or product are null or empty
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the provided connection id doesn't match the user's actual connection id
	 * @throws AlreadySubscribedException if the user is already subscribed to receive last sale updates for this stock
	 * @throws InvalidStockException if the stock is null
	 */
	public void subscribeLastSale(String product) 
			throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, AlreadySubscribedException, InvalidStockException {
		UserCommandService.getInstance().subscribeLastSale(getUserName(), getConnId(), product);
	}
	
	/**
	 * Forwards the message subscription to the user command service
	 * @param product the stock to subscribe to
	 * @throws InvalidDataException if the username or product are null or empty
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the provided connection id doesn't match the user's actual connection id
	 * @throws AlreadySubscribedException if the user is already subscribed to receive updates about this stock
	 * @throws InvalidStockException if the stock is null
	 */
	public void subscribeMessages(String product) 
			throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, AlreadySubscribedException, InvalidStockException {
		UserCommandService.getInstance().subscribeMessages(getUserName(), getConnId(), product);
	}
	
	/**
	 * Forwards the ticker subscription to the user command service
	 * @param product the stock to subscribe to
	 * @throws InvalidDataException if the username or product are null or empty
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the provided connection id doesn't match the user's actual connection id
	 * @throws AlreadySubscribedException if the user is already subscribed to receive ticker messages for this stock
	 * @throws InvalidStockException if the stock is null
	 */
	public void subscribeTicker(String product) 
			throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, AlreadySubscribedException, InvalidStockException {
		UserCommandService.getInstance().subscribeTicker(getUserName(), getConnId(), product);
	}
	
	/**
	 * Returns the value of the all Stock the User has bought and sold
	 * @return the total current value of all stocks this user owns
	 * @throws InvalidPriceOperation if the holdings map contains a market price
	 * @throws InvalidDataException if the holdings map contains a null or empty stock
	 */
	public Price getAllStockValue() throws InvalidPriceOperation, InvalidDataException {
		return getPosition().getAllStockValue();
	}
	
	/**
	 * Returns the difference between cost of all stock purchases and stock sales
	 * @return the difference between cost of all stock purchases and stock sales
	 */
	public Price getAccountCosts() {
		return getPosition().getAccountCosts();
	}
	
	/**
	 * Returns the difference between current value of all stocks owned and the account costs
	 * @return the net value of the user's account
	 * @throws InvalidPriceOperation if the holdings map contains a market price
	 * @throws InvalidDataException if the holdings map contains a null or empty stock
	 */
	public Price getNetAccountValue() throws InvalidPriceOperation, InvalidDataException {
		return getPosition().getNetAccountValue();
	}
	
	/** 
	 * Allows the User object to submit a Book Depth request for the specified stock
	 * @param product the stock the user wants the book depth for
	 * @return the book depth of the provided stock
	 * @throws InvalidDataException if the username or product are null or empty
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the connection ID does not match the user's actual connection ID
	 * @throws NoSuchProductException if the product does not exist in the trading system
	 */
	public String[][] getBookDepth(String product) 
			throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, NoSuchProductException {
		return UserCommandService.getInstance().getBookDepth(getUserName(), getConnId(), product);
	}
	
	/**
	 * Allows the User object to query the market state
	 * @return the current market state
	 * @throws InvalidDataException if the username or connection id are null or empty
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidConnectionIdException if the connection ID does not match the actual connection ID for the user
	 */
	public String getMarketState() throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException {
		return UserCommandService.getInstance().getMarketState(getUserName(), getConnId());
	}
	
	
	/**
	 * Returns the user's submitted orders
	 * @return the user's submitted orders
	 */
	public ArrayList<TradableUserData> getOrderIds() {
		return submittedOrders;
	}
	
	/**
	 * Returns the list of available stocks
	 * @return the list of available stocks
	 */
	public ArrayList<String> getProductList() {
		return availableStocks;
	}
	
	/**
	 * Returns the value of the specified stock that this user owns
	 * @param product the stock to look up
	 * @return the current value of the stock symbol passed in that is owned by the user
	 * @throws InvalidDataException if the product is null or empty
	 * @throws InvalidPriceOperation the the price stored in the holdings map is a market price
	 */
	public Price getStockPositionValue(String product) throws InvalidDataException, InvalidPriceOperation {
		return getPosition().getStockPositionValue(product);
	}
	
	/**
	 * Returns the value of the specified stock that this user owns
	 * @param product the stock to look up
	 * @return the volume of the specified stock this user owns
	 * @throws InvalidDataException if the product is null or empty
	 */
	public int getStockPositionVolume(String product) throws InvalidDataException {
		return getPosition().getStockPositionVolume(product);
	}
	
	/**
	 * Returns a list of all the Stocks the user owns
	 */
	public ArrayList<String> getHoldings() {
		return getPosition().getHoldings();
	}
	
	/**
	 * Gets a list of DTO’s containing information on all Orders for this user for the specified product with remaining volume
	 * @param product the stock to look up
	 * @throws InvalidConnectionIdException if the provided connection ID does not match the actual connection ID for the user
	 * @throws UserNotConnectedException if the user is not connected to the trading system
	 * @throws InvalidDataException if the username or product are null or empty
	 */
	public ArrayList<TradableDTO> getOrdersWithRemainingQty(String product) 
			throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException {
		return UserCommandService.getInstance().getOrdersWithRemainingQty(getUserName(), getConnId(), product);
	}
	
	/**
	 * Retrieves the user's username
	 * @return the user's username
	 */
	public String getUserName() {
		return userName;
	}
	
	/**
	 * Returns the user's connection id
	 * @return the user's connection id
	 */
	private long getConnId() {
		return connId;
	}
	
	/**
	 * Returns the user's market position
	 * @return the user's market position
	 */
	private Position getPosition() {
		return marketPosition;
	}

	/**
	 * Returns the user's display
	 * @return the user's display
	 */
	private UserDisplayManager getDisplay() {
		return display;
	}
	
	/**
	 * Sets the username for this user
	 * @param userNameIn the username to use
	 * @throws InvalidDataException 
	 */
	private void setUserName(String userNameIn) throws InvalidDataException {
		if (userNameIn == null || userNameIn.isEmpty()) throw new InvalidDataException("The username cannot be null or empty");
		userName = userNameIn;
	}
	
	/**
	 * Makes a new Position object for this user
	 */
	private void setMarketPosition() {
		marketPosition = new Position();
	}
	
	/**
	 * Sets the connection id for this user
	 * @param connIdIn the connection ID to use
	 */
	private void setConnId(long connIdIn) {
		connId = connIdIn;
	}
	
	/**
	 * Sets the available stocks list
	 * @param availableStocksIn the stock list to use
	 */
	private void setAvailableStocks(ArrayList<String> availableStocksIn) {
		availableStocks = availableStocksIn;
	}
	
	/**
	 * Sets the display member to a new UserDisplayManager
	 */
	private void setDisplay() {
		display = new UserDisplayManager(this);
	}
	
}
