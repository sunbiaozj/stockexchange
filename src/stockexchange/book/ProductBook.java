package stockexchange.book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import stockexchange.common.DataValidationException;
import stockexchange.common.GlobalConstants.BookSide;
import stockexchange.common.GlobalConstants.MarketState;
import stockexchange.common.InvalidDataException;
import stockexchange.common.OrderNotFoundException;
import stockexchange.messages.CancelMessage;
import stockexchange.messages.FillMessage;
import stockexchange.price.Price;
import stockexchange.price.PriceFactory;
import stockexchange.publishers.CurrentMarketPublisher;
import stockexchange.publishers.LastSalePublisher;
import stockexchange.publishers.MarketDataDTO;
import stockexchange.publishers.MessagePublisher;
import stockexchange.tradable.Order;
import stockexchange.tradable.Quote;
import stockexchange.tradable.Tradable;
import stockexchange.tradable.TradableDTO;

/**
 * A class that maintains the BUY and SELL sides of a stock's book
 * @author ScottMores
 *
 */
public class ProductBook {
	
	/** The stock that this book maintains Orders for*/
	private String stock;
	/** The BUY side of this book*/
	private ProductBookSide buySide;
	/** The SELL side of this book*/
	private ProductBookSide sellSide;
	/** The latest Market Data values*/
	private String latestMarketData = "";
	/** Current quotes for each user*/
	private HashSet<String> userQuotes = new HashSet<>(); 
	/** Tradables that have been completely traded or cancelled*/
	private HashMap<Price, ArrayList<Tradable>> oldEntries = new HashMap<Price, ArrayList<Tradable>>(); 
	
	/**
	 * Constructs a new ProductBook for the specified stock
	 * @param stockIn the stock to create a ProductBOok for
	 * @throws InvalidDataException if the stock is null or empty
	 */
	public ProductBook(String stockIn) throws InvalidDataException {
		setStock(stockIn);
		buySide = new ProductBookSide(this,BookSide.BUY);
		sellSide = new ProductBookSide(this,BookSide.SELL);
	}
	
	/**
	 * Gets all of the orders with remaining quantities for the specified user
	 * @param userName the user to look up orders for
	 * @return an ArrayList of orders with remaining quantities
	 * @throws InvalidDataException if the username is null or empty
	 */
	public synchronized ArrayList<TradableDTO> getOrdersWithRemainingQty(String userName) throws InvalidDataException {
		if (userName == null || userName.isEmpty()) throw new InvalidDataException("The username cannot be null or empty.");
		ArrayList<TradableDTO> remaining = new ArrayList<TradableDTO>();
		remaining.addAll(getBuySide().getOrdersWithRemainingQty(userName));
		remaining.addAll(getSellSide().getOrdersWithRemainingQty(userName));
		return remaining;
	}
	
	/**
	 * Determines whether or not it's too late to cancel an order
	 * @param orderId the id of the order to check on
	 * @throws InvalidDataException if the order id is null or empty
	 * @throws OrderNotFoundException if the order does not exist in the old entries
	 */
	public synchronized void checkTooLateToCancel(String orderId) throws InvalidDataException, OrderNotFoundException {
		if (orderId == null || orderId.isEmpty()) throw new InvalidDataException("The order id cannot be null.");
		Iterator<Price> priceIterator = getOldEntries().keySet().iterator();
		boolean found = false;
		while (priceIterator.hasNext() && !found) {
			ArrayList<Tradable> trades = getOldEntries().get(priceIterator.next());
			Iterator<Tradable> tradeIterator = trades.iterator();
			while (tradeIterator.hasNext() && !found) {
				Tradable nextTrade = tradeIterator.next();
				if (nextTrade.getId().equals(orderId)) {
					found = true;
					CancelMessage cm = makeCancelMessage(nextTrade,"Too Late to Cancel");
					MessagePublisher.getInstance().publishCancel(cm);
				}
			}
		}
		if (!found) throw new OrderNotFoundException("The requested order could not be found.");
	}
	
	/**
	 * Retrieves the prices and volumes at all prices present in the buy and sell sides of the book
	 * @return the prices and volumes at all prices present in the buy and sell sides of the book
	 */
	public synchronized String[][] getBookDepth() {
		String[][] bd = new String[2][];
		bd[0] = getBuySide().getBookDepth();
		bd[1] = getSellSide().getBookDepth();
		return bd;
	}
	
	/**
	 * Creates a MarketDataDTO containing the best buy side price and volume, 
	 * and the best sell side price an volume
	 * @return a MarketDataDTO representing the best buy and sell prices and volumes
	 */
	public synchronized MarketDataDTO getMarketData() {
		Price bestBuyPrice = getBuySide().topOfBookPrice();
		Price bestSellPrice = getSellSide().topOfBookPrice();
		if (bestBuyPrice == null) bestBuyPrice = PriceFactory.makeLimitPrice(0);
		if (bestSellPrice == null) bestSellPrice = PriceFactory.makeLimitPrice(0);
		int bestBuyVolume = getBuySide().topOfBookVolume();
		int bestSellVolume = getSellSide().topOfBookVolume();
		MarketDataDTO marketData = new MarketDataDTO(getStock(), bestBuyPrice, bestBuyVolume, bestSellPrice, bestSellVolume);
		return marketData;
	}
	
	/**
	 * Adds a Tradable item to the old entries list
	 * @param t the Tradable item to add to the old entries list
	 * @throws InvalidDataException if the trade is null
	 */
	public synchronized void addOldEntry(Tradable t) throws InvalidDataException {
		if (t == null) throw new InvalidDataException("The trade cannot be null.");
		if (!getOldEntries().containsKey(t.getPrice())) {
			getOldEntries().put(t.getPrice(), new ArrayList<Tradable>());
		}
		t.setCancelledVolume(t.getRemainingVolume());
		t.setRemainingVolume(0);
		getOldEntries().get(t.getPrice()).add(t);
	}
	
	/**
	 * Opens the book for trading and processes any resting Orders or QuoteSides
	 * @throws InvalidDataException if any data passed to method calls is invalid
	 */
	public synchronized void openMarket() throws InvalidDataException {
		Price buyPrice = getBuySide().topOfBookPrice();
		Price sellPrice = getSellSide().topOfBookPrice();
		if (buyPrice == null || sellPrice == null) return;
		while(buyPrice.greaterOrEqual(sellPrice) || buyPrice.isMarket() || sellPrice.isMarket()) {
			ArrayList<Tradable> topOfBuySide = getBuySide().getEntriesAtPrice(buyPrice);
			HashMap<String, FillMessage> allFills = null;
			ArrayList<Tradable> toRemove = new ArrayList<Tradable>();
			for (Tradable t: topOfBuySide) {
				allFills = getSellSide().tryTrade(t);
				if (t.getRemainingVolume() == 0) toRemove.add(t);
			}
			for (Tradable t: toRemove) {
				getBuySide().removeTradable(t);
			}
			updateCurrentMarket();
			Price lastSalePrice = determineLastSalePrice(allFills);
			int lastSaleVolume = determineLastSaleQuantity(allFills);
			LastSalePublisher.getInstance().publishLastSale(getStock(), lastSalePrice, lastSaleVolume);
			buyPrice = getBuySide().topOfBookPrice();
			sellPrice = getSellSide().topOfBookPrice();
			if (buyPrice == null || sellPrice == null) break;
		}
	}
	
	/**
	 * Closes the book for trading
	 * @throws InvalidDataException if either book's Tradables contain invalid data
	 * @throws OrderNotFoundException if an attempted cancel order does not exist in either book
	 */
	public synchronized void closeMarket() throws InvalidDataException, OrderNotFoundException {
		getBuySide().cancelAll();
		getSellSide().cancelAll();
		updateCurrentMarket();
	}
	
	/**
	 * Cancels the specified order for the specified book side
	 * @param side the side of the book to look for the order
	 * @param orderId the order to cancel
	 * @throws InvalidDataException if side is null or orderId is null or empty
	 * @throws OrderNotFoundException if the order does not exist in the book
	 */
	public synchronized void cancelOrder(BookSide side, String orderId) throws InvalidDataException, OrderNotFoundException {
		if (side == null) throw new InvalidDataException("The side cannot be null.");
		if (orderId == null || orderId.isEmpty()) throw new InvalidDataException("The order ID cannot be null or empty.");
		if (side.equals(BookSide.BUY)) getBuySide().submitOrderCancel(orderId);
		else getSellSide().submitOrderCancel(orderId);
		updateCurrentMarket();
	}
	
	/**
	 * Cancels a specified user's quote on both the BUY and SELL sides
	 * @param userName the user to cancel the quote for
	 * @throws InvalidDataException if the username is null or empty
	 */
	public synchronized void cancelQuote(String userName) throws InvalidDataException {
		if (userName == null || userName.isEmpty()) throw new InvalidDataException("The username cannot be null or empty.");
		getBuySide().submitQuoteCancel(userName);
		getSellSide().submitQuoteCancel(userName);
		updateCurrentMarket();
	}
	
	/**
	 * Adds the Quote's sides to this ProductBook's BUY and SELL books
	 * @param q the quote to be added
	 * @throws InvalidDataException if the quote is null
	 * @throws DataValidationException if the SELL price is less than or equal to the BUY price or if 
	 * either price is less than or equal to 0
	 */
	public synchronized void addToBook(Quote q) throws InvalidDataException, DataValidationException {
		validateData(q);
		if (getUserQuotes().contains(q.getUserName())) {
			getBuySide().removeQuote(q.getUserName());
			getSellSide().removeQuote(q.getUserName());
			updateCurrentMarket();
		}
		addToBook(BookSide.BUY, q.getQuoteSide(BookSide.BUY));
		addToBook(BookSide.SELL, q.getQuoteSide(BookSide.SELL));
		getUserQuotes().add(q.getUserName());
		updateCurrentMarket();
	}
	
	/**
	 * Adds the order to the appropriate ProductSideBOok
	 * @param o the order to add
	 * @throws InvalidDataException if the order is null
	 */
	public synchronized void addToBook(Order o) throws InvalidDataException {
		if (o == null) throw new InvalidDataException("The order cannot be null.");
		addToBook(o.getSide(),o);
		updateCurrentMarket();
	}
	
	/**
	 * Determines if the market for this stock has been updated by some market action
	 * @throws InvalidDataException if the latestMarketData String is left null
	 */
	public synchronized void updateCurrentMarket() throws InvalidDataException {
		Price topBuyPrice = (getBuySide().topOfBookPrice() == null) ? PriceFactory.makeLimitPrice(0) : getBuySide().topOfBookPrice();
		Price topSellPrice = (getSellSide().topOfBookPrice() == null) ? PriceFactory.makeLimitPrice(0) : getSellSide().topOfBookPrice();

		String data = topBuyPrice.toString() + getBuySide().topOfBookVolume()
					+ topSellPrice.toString() + getSellSide().topOfBookVolume();
		if (!getLatestMarketData().equals(data)) {
			MarketDataDTO latestData = new MarketDataDTO(getStock(), topBuyPrice, 
					getBuySide().topOfBookVolume(), topSellPrice, getSellSide().topOfBookVolume());
			CurrentMarketPublisher.getInstance().publishCurrentMarket(latestData);
			setLatestMarketData(data);
		}
	}
	
	/**
	 * Determines the last sale price of the given fill messages
	 * @param fills the fill messages to get the latest price from
	 * @return the latest Price of the given Fill messages
	 * @throws InvalidDataException if fills is null
	 */
	private synchronized Price determineLastSalePrice(HashMap<String, FillMessage> fills) throws InvalidDataException {
		if (fills == null) throw new InvalidDataException("Fills cannot be null");
		ArrayList<FillMessage> msgs = new ArrayList<FillMessage>(fills.values());
		Collections.sort(msgs);
		return msgs.get(0).getPrice();
	}
	
	/**
	 * Determines the last sale quantity of the given fill messages
	 * @param fills the fill messages to get the latest volume from
	 * @return the latest volume of the given Fill messages
	 * @throws InvalidDataException if fills is null
	 */
	private synchronized int determineLastSaleQuantity(HashMap<String, FillMessage> fills) throws InvalidDataException {
		if (fills == null) throw new InvalidDataException("Fills cannot be null.");
		ArrayList<FillMessage> msgs = new ArrayList<FillMessage>(fills.values()); 
		//Collections.sort(msgs);
		Collections.sort(msgs);
		Collections.reverse(msgs);
		return msgs.get(0).getVolume();
	}
	
	/**
	 * Deals with the addition of Tradables to the BUY and SELL ProductBookSides and handles the results
	 * of any trades that result from the addition
	 * @param side the BUY or SELL side of the Tradable
	 * @param trd the Tradable to add to the book
	 * @throws InvalidDataException if the BookSide or Tradable are null
	 */
	private synchronized void addToBook(BookSide side, Tradable trd) throws InvalidDataException {
		if (side == null) throw new InvalidDataException("The side cannot be null.");
		if (trd == null) throw new InvalidDataException("The tradable cannot be null.");
		if (ProductService.getInstance().getMarketState().equals(MarketState.PREOPEN)) {
			if (side.equals(BookSide.BUY)) getBuySide().addToBook(trd);
			else getSellSide().addToBook(trd);
		}
		else {
			HashMap<String, FillMessage> allFills = null;
			if (side.equals(BookSide.BUY)) allFills = getSellSide().tryTrade(trd);
			else allFills = getBuySide().tryTrade(trd);
			if (allFills != null && !allFills.isEmpty()) {
				updateCurrentMarket();
				int traded = trd.getOriginalVolume() - trd.getRemainingVolume();
				Price lastSalePrice = determineLastSalePrice(allFills);
				LastSalePublisher.getInstance().publishLastSale(getStock(), lastSalePrice, traded);
			}
			if (trd.getRemainingVolume() > 0) {
				if (trd.getPrice().isMarket()) {
					CancelMessage cm = new CancelMessage(trd.getUser(), trd.getProduct(), trd.getPrice(), trd.getRemainingVolume(), 
							"Cancelled", trd.getSide(), trd.getId());
					MessagePublisher.getInstance().publishCancel(cm);
				}
				else {
					if (side.equals(BookSide.BUY)) getBuySide().addToBook(trd);
					else getSellSide().addToBook(trd);
				}
			}
		}
	}
	
	/**
	 * Helper function to validate the validity of a Quote
	 * @param q the Quote to validate
	 * @throws DataValidationException if the SELL price is less than or equal to the BUY price or if 
	 * either price is less than or equal to 0
	 * @throws InvalidDataException if the quote is null
	 */
	private synchronized void validateData(Quote q) throws DataValidationException, InvalidDataException {
		if (q == null) throw new InvalidDataException("Quote cannot be null.");
		if (q.getQuoteSide(BookSide.SELL).getPrice().lessOrEqual(q.getQuoteSide(BookSide.BUY).getPrice())) {
			throw new DataValidationException("Sell price: " + q.getQuoteSide(BookSide.SELL).getPrice() +
					" cannot be less than or equal to the buy price: " + q.getQuoteSide(BookSide.BUY).getPrice());
		}
		if (q.getQuoteSide(BookSide.BUY).getPrice().lessOrEqual(PriceFactory.makeLimitPrice(0)) || 
				q.getQuoteSide(BookSide.SELL).getPrice().lessOrEqual(PriceFactory.makeLimitPrice(0))) {
			throw new DataValidationException("The buy and sell prices cannot be less than or equal to 0. "
					+ "Your buy price: " + q.getQuoteSide(BookSide.BUY).getPrice() 
					+ ". Your sell price: " + q.getQuoteSide(BookSide.SELL).getPrice());
		}
		if (q.getQuoteSide(BookSide.BUY).getOriginalVolume() <= 0 || 
				q.getQuoteSide(BookSide.SELL).getOriginalVolume() <= 0) {
			throw new DataValidationException("The original volume of the BUY or SELL side cannot be less than or equal to 0. "
					+ "Your BUY volume: " + q.getQuoteSide(BookSide.BUY).getOriginalVolume()
					+ ". Your SELL volume: " + q.getQuoteSide(BookSide.SELL).getOriginalVolume());
		}
	}
	
	/**
	 * Getter method to retrieve the buy side of this product book
	 * @return the buy side of this product book
	 */
	private ProductBookSide getBuySide() {
		return buySide;
	}
	
	/**
	 * Getter method to retrieve the sell side of this product book
	 * @return the sell side of this product book
	 */
	private ProductBookSide getSellSide() {
		return sellSide;
	}
	
	/**
	 * Getter method to retrieve the list of old entries
	 * @return a list of the old entries
	 */
	private HashMap< Price, ArrayList<Tradable>> getOldEntries() {
		return oldEntries;
	}
	
	/**
	 * Getter method to retrieve this book's associated stock
	 * @return this book's associated stock
	 */
	private String getStock() {
		return stock;
	}
	
	/**
	 * Getter method to retrieve this book's user quotes
	 * @return this book's user quotes
	 */
	private HashSet<String> getUserQuotes() {
		return userQuotes;
	}
	
	/**
	 * Getter method to retrieve this book's latest market data
	 * @return this book's latest market data
	 */
	private String getLatestMarketData() {
		return latestMarketData;
	}
	
	/**
	 * Helper method that makes a CancelMessage with the data in the Tradable object passed in.
	 * @param t the Tradable object to get information from
	 * @return a CancelMessage with the data from the Tradable passed in
	 * @throws InvalidDataException if there was invalid data in the Tradable object (thrown by CancelMessage constructor)
	 */
	private CancelMessage makeCancelMessage(Tradable t, String details) throws InvalidDataException {
		return new CancelMessage(t.getUser(),t.getProduct(),t.getPrice(),
				t.getRemainingVolume(), details, t.getSide(),t.getId());
	}
	
	/**
	 * Setter method for this ProductBook's stock
	 * @param stockIn the stock to associate this ProductBook with
	 * @throws InvalidDataException if the stock is null or empty
	 */
	private void setStock(String stockIn) throws InvalidDataException {
		if (stockIn == null || stockIn.isEmpty()) throw new InvalidDataException("The stock cannot be null.");
		stock = stockIn;
	}
	
	/**
	 * Setter method for the latest market data String
	 * @param latestData the String to set the latest market data to
	 */
	private void setLatestMarketData(String latestData) {
		latestMarketData = latestData;
	}
	
}
