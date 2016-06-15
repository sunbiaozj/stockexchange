package stockexchange.client;

import java.util.ArrayList;

import stockexchange.common.AlreadyConnectedException;
import stockexchange.common.AlreadySubscribedException;
import stockexchange.common.DataValidationException;
import stockexchange.common.InvalidConnectionIdException;
import stockexchange.common.InvalidDataException;
import stockexchange.common.InvalidMarketStateException;
import stockexchange.common.InvalidPriceOperation;
import stockexchange.common.InvalidStockException;
import stockexchange.common.NoSuchProductException;
import stockexchange.common.OrderNotFoundException;
import stockexchange.common.UserNotConnectedException;
import stockexchange.common.GlobalConstants.BookSide;
import stockexchange.messages.CancelMessage;
import stockexchange.messages.FillMessage;
import stockexchange.price.Price;
import stockexchange.tradable.TradableDTO;

/**
 * An interface for User objects of the DSX trading system.
 * @author ScottMores
 *
 */
public interface User {
	
	/**
	 * Provides public access to the username of this user.
	 * @return this user's username as a String.
	 */
	String getUserName();
	
	/**
	 * Provides information to Users to track stock sales and volumes.
	 * @param product a stock symbol.
	 * @param p the value of the last trade of the product.
	 * @param v the quantity of the last sale of the product.
	 */
	void acceptLastSale(String product, Price p, int v);
	
	/**
	 * A receipt sent to the user to document the details when an order
	 * or quote-side of his/hers trades.
	 * @param fm information related to an order or quote trade.
	 */
	void acceptMessage(FillMessage fm);
	
	/**
	 * A receipt sent to the user to document the details when an order
	 * or quote-side of his/hers is cancelled.
	 * @param cm information related to an order or quote trade.
	 */
	void acceptMessage(CancelMessage cm);
	
	/**
	 * Provides information to a user about a product he/she is interested in.
	 * @param message contains market information related to a stock symbol
	 */
	void acceptMarketMessage(String message);
	
	/**
	 * Provides information to a User about stock movement.
	 * @param product a stock symbol.
	 * @param p the value of the last trade of the stock.
	 * @param direction increase or decrease in the stock's price.
	 */
	void acceptTicker(String product, Price p, char direction);
	
	/**
	 * Provides information to a user about the current market for a stock.
	 * @param product a stock symbol.
	 * @param bp the current BUY side for the stock.
	 * @param bv the current BUY side volume for the stock.
	 * @param sp the current SELL side for the stock.
	 * @param sv the current SELL side volume for the stock.
	 */
	void acceptCurrentMarket(String product, Price bp, int bv, Price sp, int sv);
	
	/**
	 * Instructs a User object to connect to the trading system
	 */
	void connect() throws InvalidDataException, AlreadyConnectedException, UserNotConnectedException, InvalidConnectionIdException;
	
	/**
	 * Instructs a User object to disconnect from the trading system
	 */
	void disConnect() throws UserNotConnectedException, InvalidConnectionIdException, InvalidDataException;
	
	/**
	 * Requests the opening of the market display if the user is connected
	 */
	void showMarketDisplay() throws UserNotConnectedException, Exception;
	
	/**
	 * Allows the User object to submit a new Order request
	 * @param product the stock for the order request
	 * @param price the price for the order request
	 * @param volume the volume of the order request
	 * @param side the BUY or SELL side of the order request
	 * @return the orderId for the order requeset
	 */
	String submitOrder(String product, Price price, int volume, BookSide side) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, InvalidMarketStateException, NoSuchProductException;
	
	/**
	 * Allows the User object to submit a new Order Cancel request
	 * @param product the stock to cancel the order for
	 * @param side the side of the cancelled order
	 * @param orderId the ID of the order to cancel
	 */
	void submitOrderCancel(String product, BookSide side, String orderId) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, InvalidMarketStateException, NoSuchProductException, OrderNotFoundException;
	
	/**
	 * Allows the User object to submit a new Quote request
	 * @param product the stock for the Quote request
	 * @param buyPrice the buy price of the Quote request
	 * @param buyVolume the buy volume of the Quote request
	 * @param sellPrice the sell price of the Quote request
	 * @param sellVolume the sell volume of the Quote request
	 */
	void submitQuote(String product, Price buyPrice, int buyVolume, Price sellPrice, int sellVolume) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, InvalidMarketStateException, NoSuchProductException, DataValidationException;
	
	/**
	 * Allows the User object to submit a new Quote Cancel request
	 * @param product the stock of the quote to cancel
	 */
	void submitQuoteCancel(String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, InvalidMarketStateException, NoSuchProductException;
	
	/**
	 * Allows the User object to subscribe for Current Market for the specified Stock
	 * @param product the stock to subscribe to updates for
	 */
	void subscribeCurrentMarket(String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, AlreadySubscribedException, InvalidStockException;
	
	/**
	 * Allows the User object to subscribe for Last Sale for the specified Stock
	 * @param product the stock to subscribe to receive last sale updates for
	 */
	void subscribeLastSale(String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, AlreadySubscribedException, InvalidStockException;
	
	/**
	 * Allows the User object to subscribe for Messages for the specified Stock
	 * @param product the stock the user would like to receive messages about
	 */
	void subscribeMessages(String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, AlreadySubscribedException, InvalidStockException;
	
	/**
	 * Allows the User object to subscribe to Ticker for the specified Stock
	 * @param product the stock to subscribe to receive Tickers for
	 */
	void subscribeTicker(String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, AlreadySubscribedException, InvalidStockException;
	
	/**
	 * Returns the value of the all Stock the User owns (has bought but not sold)
	 * @return teh value of all the stocks the user owns
	 */
	Price getAllStockValue() throws InvalidPriceOperation, InvalidDataException;
	
	/**
	 * Returns the difference between cost of all stock purchases and stock sales
	 * @return the difference between the cost of all stock purchases and the stock sales
	 */
	Price getAccountCosts();
	
	/**
	 * Returns the difference between current value of all stocks owned and the account costs
	 * @return the difference between the current value of all stocks owned and the account costs
	 */
	Price getNetAccountValue() throws InvalidPriceOperation, InvalidDataException;
	
	/**
	 * Allows the User object to submit a Book Depth request for the specified stock
	 * @param product the stock the user would like to request a book depth for
	 * @return the book depth of the provided stock
	 */
	String[][] getBookDepth(String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException, NoSuchProductException;
	
	/**
	 * Allows the User object to query the market state (OPEN, PREOPEN, CLOSED)
	 * @return the current market state
	 */
	String getMarketState() throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException;
	
	/**
	 * Returns a list of order id’s for the orders this user has submitted
	 * @return a list of order id's for the orders this user has submitted
	 */
	ArrayList<TradableUserData> getOrderIds();
	
	/**
	 * Returns a list of the stock products available in the trading system
	 * @return a list of the stock products available in the trading system
	 */
	ArrayList<String> getProductList();
	
	/**
	 * Returns the value of the specified stock that this user owns
	 * @param sym the stock symbol to look up data for
	 * @return the value of the specified stock that this user owns
	 */
	Price getStockPositionValue(String sym) throws InvalidDataException, InvalidPriceOperation;
	
	/**
	 * Returns the volume of the specified stock that this user owns
	 * @param product the stock to look up the volume for
	 * @return the volume of the specified stock that this user owns
	 */
	int getStockPositionVolume(String product) throws InvalidDataException;
	
	/**
	 * Returns a list of all the Stocks the user owns 
	 * @return a list of all the stocks the user owns
	 */
	ArrayList<String> getHoldings();
	
	/**
	 * Gets a list of DTO’s containing information on all Orders for this 
	 * user for the specified product with remaining volume
	 * @param product the stock to retrieve a list of DTO's for
	 * @return a list of DTO's containing information on all Orders for this
	 * user for the specified stock with remaining volume
	 */
	ArrayList<TradableDTO> getOrdersWithRemainingQty(String product) throws InvalidDataException, UserNotConnectedException, InvalidConnectionIdException;
	
}
