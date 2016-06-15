package stockexchange.book;

import java.util.ArrayList;
import java.util.HashMap;

import stockexchange.common.DataValidationException;
import stockexchange.common.GlobalConstants.BookSide;
import stockexchange.common.GlobalConstants.MarketState;
import stockexchange.common.InvalidDataException;
import stockexchange.common.InvalidMarketStateException;
import stockexchange.common.InvalidMarketStateTransition;
import stockexchange.common.NoSuchProductException;
import stockexchange.common.OrderNotFoundException;
import stockexchange.common.ProductAlreadyExistsException;
import stockexchange.messages.MarketMessage;
import stockexchange.publishers.MarketDataDTO;
import stockexchange.publishers.MessagePublisher;
import stockexchange.tradable.Order;
import stockexchange.tradable.Quote;
import stockexchange.tradable.TradableDTO;

/**
 * A facade class where all interaction with product books goes through.
 * @author Scott
 *
 */
public final class ProductService {
	
	/**	The sole instance of the ProductService class */
	private volatile static ProductService instance;
	/**	All of the product books contained in this facade */
	private HashMap<String, ProductBook> allBooks = new HashMap<String, ProductBook>();
	/** The current state of the market */
	private MarketState marketState = MarketState.CLOSED;
	
	/**
	 * Gets the sole instance of the ProductService class
	 * @return the sole instance of the ProductService class
	 */
	public static ProductService getInstance() {
		if (instance == null) {
			synchronized(ProductService.class) {
				if (instance == null) instance = new ProductService();
			}
		}
		return instance;
	}
	
	/**
	 * Retrieves a list of TradableDTO's containing orders with remaining quantities for the user and stock specified
	 * @param userName the user to get orders for
	 * @param product the stock to get orders for
	 * @return a list of all the orders with remaining quantities for specified the user and stock
	 * @throws InvalidDataException if the username or product are empty or null
	 */
	public synchronized ArrayList<TradableDTO> getOrdersWithRemainingQty(String userName, String product) throws InvalidDataException {
		if (userName == null || userName.isEmpty()) throw new InvalidDataException("The username cannot be null or empty.");
		if (product == null || product.isEmpty()) throw new InvalidDataException("The product cannot be null or empty.");
		return getAllBooks().get(product).getOrdersWithRemainingQty(userName);
	}
	
	/**
	 * Retrieves a list of MarketDataDTO with the best buy price and volume and the best sell price and volume
	 * @param product the stock to retrieve data for
	 * @return a MarketDataDTO that contains market data about the specified stock
	 * @throws InvalidDataException if the product is null or empty
	 */
	public synchronized MarketDataDTO getMarketData(String product) throws InvalidDataException {
		if (product == null || product.isEmpty()) throw new InvalidDataException("Product cannot be null or empty.");
		MarketDataDTO data = null;
		if (getAllBooks().containsKey(product)) {
			data = getAllBooks().get(product).getMarketData();
		}
		return data;
	}
	
	/**
	 * Getter method to retrieve the current market state
	 * @return the current market state
	 */
	public synchronized MarketState getMarketState() {
		return marketState;
	}
	
	/**
	 * Retrieves the book depth of the specified product
	 * @param product the stock to get the book depth of
	 * @return a String[][] with the book depth information
	 * @throws InvalidDataException if the product is null or empty
	 * @throws NoSuchProductException if the product does not have a product book
	 */
	public synchronized String[][] getBookDepth(String product) throws InvalidDataException, NoSuchProductException {
		if (product == null || product.isEmpty()) throw new InvalidDataException("Product cannot be null or empty.");
		if (!getAllBooks().containsKey(product)) {
			throw new NoSuchProductException("The product " + product + " does not exist");
		}
		return getAllBooks().get(product).getBookDepth();
	}
	
	/**
	 * Makes an ArrayList of all the products with product books
	 * @return an ArrayList of all the products with product books
	 */
	public synchronized ArrayList<String> getProductList() {
		return new ArrayList<String>(getAllBooks().keySet()); 
	}
	
	/**
	 * Sets the market state to a new state
	 * @param ms the new state to set the market to
	 * @throws InvalidMarketStateTransition if the transition from the current state to the requested state is not valid
	 * @throws InvalidDataException if the state is null
	 * @throws OrderNotFoundException if an attempted cancel order does not exist in a book while closing the market
	 */
	public synchronized void setMarketState(MarketState ms) throws InvalidMarketStateTransition, 
			InvalidDataException, OrderNotFoundException {
		if (ms == null) throw new InvalidDataException("The market state cannot be null");
		validateMarketStateTransition(ms);
		marketState = ms;
		MessagePublisher.getInstance().publishMarketMessage(new MarketMessage(ms));
		if (ms.equals(MarketState.OPEN)) {
			for (ProductBook pb: getAllBooks().values()) pb.openMarket();
		}
		if (ms.equals(MarketState.CLOSED)) {
			for (ProductBook pb: getAllBooks().values()) pb.closeMarket();
		}
	}
	
	/**
	 * Helper method to check the validity of market state transitions
	 * @param ms the new requested market state change
	 * @throws InvalidMarketStateTransition if the state transition is not valid
	 */
	private synchronized void validateMarketStateTransition(MarketState ms) throws InvalidMarketStateTransition {
		switch (ms) {
			case CLOSED:
				if (!getMarketState().equals(MarketState.OPEN)) {
					throw new InvalidMarketStateTransition("Cannot transition from " + getMarketState() + " to CLOSED");
				}
				break;
			case OPEN:
				if (!getMarketState().equals(MarketState.PREOPEN)) {
					throw new InvalidMarketStateTransition("Cannot transition from " + getMarketState() + " to OPEN");
				}
				break;
			case PREOPEN:
				if (!getMarketState().equals(MarketState.CLOSED)) {
					throw new InvalidMarketStateTransition("Cannot transition from " + getMarketState() + " to PREOPEN");
				}
				break;
		}
	}
	
	/**
	 * Creates a new stock product that can be used for trading
	 * @param product the stock to make a new trading product for
	 * @throws ProductAlreadyExistsException if the stock already exists
	 * @throws InvalidDataException if the stock is null or empty
	 */
	public synchronized void createProduct(String product) throws ProductAlreadyExistsException, InvalidDataException {
		if (product == null || product.isEmpty()) throw new InvalidDataException("The product cannot be null or empty.");
		if (getAllBooks().containsKey(product)) throw new ProductAlreadyExistsException("The product " + product + " already exists");
		getAllBooks().put(product, new ProductBook(product));
	}
	
	/**
	 * Forwards the provided quote to the appropriate product book
	 * @param q the quote to forward to the appropriate product book
	 * @throws InvalidMarketStateException if the market is currently closed
	 * @throws NoSuchProductException if the product does not exist
	 * @throws InvalidDataException if the quote is null
	 * @throws DataValidationException if the quote's SELL price is less than or equal to the BUY price or if 
	 * either price in the quote is less than or equal to 0
	 */
	public synchronized void submitQuote(Quote q) throws InvalidMarketStateException, NoSuchProductException, InvalidDataException, DataValidationException {
		if (q == null) throw new InvalidDataException("The quote cannot be null");
		if (getMarketState().equals(MarketState.CLOSED)) throw new InvalidMarketStateException("Cannot submit a quote while the market is closed");
		if (!getAllBooks().containsKey(q.getProduct())) throw new NoSuchProductException("Product " + q.getProduct() + " does not exist");
		getAllBooks().get(q.getProduct()).addToBook(q);
	}
	
	/**
	 * Forwards the provided order to the appropriate product book
	 * @param o the order to forward to the appropriate product book
	 * @return the string ID of the order that was forwarded
	 * @throws InvalidMarketStateException if the market is CLOSED or the market is PREOPEN and the order is a market price
	 * @throws InvalidDataException if the order is null
	 * @throws NoSuchProductException if the product for the order does not exist
	 */
	public synchronized String submitOrder(Order o) throws InvalidMarketStateException, InvalidDataException, NoSuchProductException {
		if (o == null) throw new InvalidDataException("The order cannot be null");
		if (getMarketState().equals(MarketState.CLOSED)) throw new InvalidMarketStateException("Cannot submit an order while the market is closed");
		if (getMarketState().equals(MarketState.PREOPEN) && o.getPrice().isMarket())
			throw new InvalidMarketStateException("Cannot submit a market price order while the market is in PREOPEN state");
		if (!getAllBooks().containsKey(o.getProduct())) throw new NoSuchProductException("Product " + o.getProduct() + " does not exist");
		getAllBooks().get(o.getProduct()).addToBook(o);
		return o.getId();
	}
	
	
	/**
	 * Forwards the provided cancel order to the appropriate product book
	 * @param product the stock that this cancel order refers to
	 * @param side the BUY or SELL side of the order
	 * @param orderId the id of the order to be cancelled
	 * @throws InvalidMarketStateException if the market is currently closed
	 * @throws NoSuchProductException if there are not product books for the given stock
	 * @throws InvalidDataException if the product is null or empty, if the side is null or if the order
	 * id is null or empty
	 * @throws OrderNotFoundException if the order id does not exist in a product book
	 */
	public synchronized void submitOrderCancel(String product, BookSide side, String orderId) throws InvalidMarketStateException, NoSuchProductException, InvalidDataException, OrderNotFoundException {
		if (getMarketState().equals(MarketState.CLOSED)) throw new InvalidMarketStateException("Cannot cancel an order when the market is closed");
		if (!getAllBooks().containsKey(product)) throw new NoSuchProductException("Product " + product + " does not exist");
		if (product == null || product.isEmpty()) throw new InvalidDataException("Product cannot be null or empty");
		if (side == null) throw new InvalidDataException("The side cannot be null");
		if (orderId == null || orderId.isEmpty()) throw new InvalidDataException("The orderId cannot be empty or null");
		getAllBooks().get(product).cancelOrder(side, orderId);
	}
	
	/**
	 * Forwards the provided quote cancel to the appropriate product book
	 * @param userName the username attached to the quote cancel 
	 * @param product the stock attached to the quote cancel
	 * @throws InvalidMarketStateException if the market is currently closed
	 * @throws InvalidDataException if the username or product are null or empty
	 * @throws NoSuchProductException if the product does currently not exist
	 */
	public synchronized void submitQuoteCancel(String userName, String product) throws InvalidMarketStateException, InvalidDataException, NoSuchProductException {
		if (getMarketState().equals(MarketState.CLOSED)) throw new InvalidMarketStateException("Cannot cancel a quote when the market is closed");
		if (userName == null || userName.isEmpty()) throw new InvalidDataException("Username cannot be null or empty");
		if (product == null || product.isEmpty()) throw new InvalidDataException("The product cannot be null or empty");
		if (!getAllBooks().containsKey(product)) throw new NoSuchProductException("Product " + product + " does not exist");
		getAllBooks().get(product).cancelQuote(userName);
	}
	
	/**
	 * Getter method to retrieve all of the product books
	 * @return all of the stocks and their associated product books
	 */
	private HashMap<String, ProductBook> getAllBooks() {
		return allBooks;
	}
	
	/**
	 * Private constructor that can only be called from within this class
	 */
	private ProductService(){}
}
