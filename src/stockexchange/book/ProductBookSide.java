package stockexchange.book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import stockexchange.common.GlobalConstants.BookSide;
import stockexchange.common.InvalidDataException;
import stockexchange.common.OrderNotFoundException;
import stockexchange.common.TradeProcessorImplFactory;
import stockexchange.messages.CancelMessage;
import stockexchange.messages.FillMessage;
import stockexchange.price.Price;
import stockexchange.publishers.MessagePublisher;
import stockexchange.tradable.Tradable;
import stockexchange.tradable.TradableDTO;

/**
 * Represents the content of the BUY or SELL side of a specific stock's ProductBook
 * @author ScottMores
 */
public class ProductBookSide {
	
	/** The BUY or SELL side of the ProductBook that this object represents */
	private BookSide side;
	/** Collection of book entries for this side of the ProductBook */
	private HashMap<Price, ArrayList<Tradable>> bookEntries = new HashMap<Price, ArrayList<Tradable>>();
	/** A TradeProcessor that executes trades against this side of the book */
	private TradeProcessor processor;
	/** The ProductBook object that this ProductBookSide belongs to */
	private ProductBook parent;
	
	/**
	 * Constructs a new ProductBookSide object to be used as part of a ProductBook
	 * @param parentIn the ProductBook this object will be a part of
	 * @param sideIn the BUY or SELL side of this ProductBookSide object
	 * @throws InvalidDataException if the ProductBook or BookSide are null
	 */
	public ProductBookSide(ProductBook parentIn, BookSide sideIn) throws InvalidDataException {
		setParent(parentIn);
		setSide(sideIn);
		setProcessor("pricetime", this);
	}
	
	/**
	 * Generates an ArrayList of TradableDTO's for the specified user that have a remaining quantity
	 * @param userName the user to look up data for
	 * @return an ArrayList of TradableDTO's that have a remaining quantity for this user
	 * @throws InvalidDataException if the username is null or an empty string
	 */
	public synchronized ArrayList<TradableDTO> getOrdersWithRemainingQty(String userName) throws InvalidDataException {
		if (userName == null || userName.isEmpty()) throw new InvalidDataException("Username cannot be null or blank.");
		ArrayList<TradableDTO> tradables = new ArrayList<TradableDTO>();
		ArrayList<Price> test = new ArrayList<Price>(getBookEntries().keySet());
		Collections.sort(test);
		Collections.reverse(test);
		for (Price p: test) {
			ArrayList<Tradable> entry = getBookEntries().get(p);
			for (Tradable t: entry) {
				if (t.getUser().equals(userName) && t.getRemainingVolume() > 0) {
					tradables.add(makeTradableDTO(t));
				}
			}
		}
		return tradables;
	}
	
	/**
	 * Retrieves the ArrayList of Tradables that are associated with the best price in the product book
	 * @return the ArrayList of Tradables that are associated with the best price in the product book
	 */
	synchronized ArrayList<Tradable> getEntriesAtTopOfBook() {
		if (isEmpty()) return null;
		ArrayList<Price> sorted = sortPrices();
		return getBookEntries().get(sorted.get(0));
	}
	
	/**
	 * Builds a String array that represents the product book's depth
	 * @return a String array that represents the product book's depth
	 */
	public synchronized String[] getBookDepth() {
		if (isEmpty()) return new String[]{"<Empty>"};
		String[] bookDepth = new String[getBookEntries().size()];
		ArrayList<Price> sorted = sortPrices();
		for (int i = 0; i < sorted.size(); i++) {
			ArrayList<Tradable> tradables = getBookEntries().get(sorted.get(i));
			int volume = calcRemainingVolume(tradables);
			bookDepth[i] = sorted.get(i) + " x " + volume;
		}
		return bookDepth;
	}
	
	/**
	 * Retrieves the Tradables in this book associated with the given Price
	 * @param price the Price to find associated Tradables for
	 * @return the ArrayList of Tradables associated with the price
	 * @throws InvalidDataException if the price is null
	 */
	synchronized ArrayList<Tradable> getEntriesAtPrice(Price price) throws InvalidDataException {
		if (price == null) throw new InvalidDataException("The price cannot be null.");
		if (!getBookEntries().containsKey(price)) return null;
		return getBookEntries().get(price);
	}
	
	/**
	 * Checks whether or not the product book contains a MarketPrice
	 * @return true if the product book contains a MarketPrice and false otherwise
	 */
	public synchronized boolean hasMarketPrice() {
		boolean containsMarket = false;
		ArrayList<Price> priceList = makePriceArrayList();
		for (Price p: priceList) {
			if (p.isMarket()) {
				containsMarket = true;
				break;
			}
		}
		return containsMarket;
	}
	
	/**
	 * Checks whether or not there is only one Price in the product book and that Price is a MarketPrice
	 * @return true if there is only one Price in the product book and that Price is a MarketPrice and false otherwise
	 */
	public synchronized boolean hasOnlyMarketPrice() {
		if (getBookEntries().size() > 1) return false;
		else return hasMarketPrice();
	}
	
	/**
	 * Retrieves the best Price in this book side
	 * @return the best Price in this book side
	 */
	public synchronized Price topOfBookPrice() {
		if (isEmpty()) return null;
		ArrayList<Price> sorted = sortPrices();
		return sorted.get(0);
	}
	
	/**
	 * Calculates the total remaining volume associated with the best Price in the book side
	 * @return the sum of the remaining volumes of all the Tradables at the top of the product book
	 */
	public synchronized int topOfBookVolume() {
		if (isEmpty()) return 0;
		ArrayList<Price> sorted = sortPrices();
		Price top = sorted.get(0);
		ArrayList<Tradable> tradables = getBookEntries().get(top);
		return calcRemainingVolume(tradables);
	}
	
	/**
	 * Checks to see whether or not the product book is empty
	 * @return true if the product book is empty and false otherwise
	 */
	public synchronized boolean isEmpty() {
		return getBookEntries().isEmpty();
	}
	
	/**
	 * Cancels every Order or Quoteside at every price in the book
	 * @throws InvalidDataException if any of the book's Tradables contain invalid data
	 * @throws OrderNotFoundException if an order does not exist in the book
	 */
	public synchronized void cancelAll() throws InvalidDataException, OrderNotFoundException {
		ArrayList<Tradable> orderRequestsToCancel = new ArrayList<Tradable>();
		ArrayList<Price> priceList = makePriceArrayList();
		for (Price p: priceList) {
			ArrayList<Tradable> tradables = getBookEntries().get(p);
			Iterator<Tradable> tradeIterator = tradables.iterator();
			while (tradeIterator.hasNext()) {
				Tradable nextTrade = tradeIterator.next();
				orderRequestsToCancel.add(nextTrade);
			}
		}
		for (Tradable t: orderRequestsToCancel) {
			if (t.isQuote()) submitQuoteCancel(t.getUser());
			else submitOrderCancel(t.getId());
		}
	}
	
	/**
	 * Searches for a quote from the specified user and removes the quote from the book if one is found
	 * @param user the user to look up a quote for
	 * @return a TradableDTO of the quote that was found or null if no quote was found
	 * @throws InvalidDataException if the user is null or empty
	 */
	public synchronized TradableDTO removeQuote(String user) throws InvalidDataException {
		if (user == null || user.isEmpty()) throw new InvalidDataException("The user cannot be null or empty.");
		Iterator<Price> priceIterator = makeBookIterator();
		boolean found = false;
		TradableDTO quoteDTO = null;
		ArrayList<Price> pricesToRemove = new ArrayList<Price>();
		while (priceIterator.hasNext() && !found) {
			ArrayList<Tradable> tradables = getBookEntries().get(priceIterator.next());
			Iterator<Tradable> tradeIterator = tradables.iterator();
			ArrayList<Tradable> tradesToRemove = new ArrayList<Tradable>();
			while (tradeIterator.hasNext() && !found) {
				Tradable nextQuote = tradeIterator.next();
				if (nextQuote.isQuote() && nextQuote.getUser().equals(user)) {
					quoteDTO = makeTradableDTO(nextQuote);
					found = true;
					tradesToRemove.add(nextQuote);
				}
			}
			tradables.removeAll(tradesToRemove);
		}
		for (Price p: getBookEntries().keySet()) {
			if (getBookEntries().get(p).isEmpty()) pricesToRemove.add(p);
		}
		for (Price p: pricesToRemove) getBookEntries().remove(p);
		return quoteDTO;
	}
	
	/**
	 * Cancels the order with the specified orderId
	 * @param orderId the id attached to the cancel request
	 * @throws InvalidDataException if the orderId is null or empty
	 * @throws OrderNotFoundException if the order is not found and is not an old entry
	 */
	public synchronized void submitOrderCancel(String orderId) throws InvalidDataException, OrderNotFoundException {
		if (orderId == null || orderId.isEmpty()) throw new InvalidDataException("The order id cannot be null or empty.");
		Iterator<Price> priceIterator = makeBookIterator();
		boolean found = false;
		while (priceIterator.hasNext() && !found) {
			ArrayList<Tradable> tradables = getBookEntries().get(priceIterator.next());
			Iterator<Tradable> tradeIterator = tradables.iterator();
			ArrayList<Tradable> tradesToRemove = new ArrayList<Tradable>();
			while (tradeIterator.hasNext() && !found) {
				Tradable nextTrade = tradeIterator.next();
				if (nextTrade.getId().equals(orderId) && !nextTrade.isQuote()) {
					found = true;
					tradesToRemove.add(nextTrade);
					CancelMessage cm = makeCancelMessage(nextTrade, nextTrade.getSide() + " Order Cancelled");
					MessagePublisher.getInstance().publishCancel(cm);
					addOldEntry(nextTrade);
					if (tradables.size() == 0) priceIterator.remove();
				}
			}
			tradables.removeAll(tradesToRemove);
		}
		ArrayList<Price> pricesToRemove = new ArrayList<Price>();
		for (Price p: getBookEntries().keySet()) {
			if (getBookEntries().get(p).isEmpty()) pricesToRemove.add(p);
		}
		for (Price p: pricesToRemove) getBookEntries().remove(p);
		if (!found) getParent().checkTooLateToCancel(orderId);
	}
	
	/**
	 * Cancels the QuoteSide in the book for the provided user
	 * @param userName the user to cancel the QuoteSide for
	 * @throws InvalidDataException if the username is null
	 */
	public synchronized void submitQuoteCancel(String userName) throws InvalidDataException {
		if (userName == null || userName.isEmpty()) throw new InvalidDataException("Username cannot be null or empty.");
		TradableDTO tradableCopy = removeQuote(userName);
		if (tradableCopy != null) {
			CancelMessage cm = makeCancelMessage(tradableCopy, "Quote " + tradableCopy.side + "-Side Cancelled");
			MessagePublisher.getInstance().publishCancel(cm);
		}
	}
	
	/**
	 * Adds a Tradable to the old entries list of the ProductBook that this ProductBookSide is attached to
	 * @param t that Tradable to add as an old entry
	 * @throws InvalidDataException if the Tradable is null
	 */
	public void addOldEntry(Tradable t) throws InvalidDataException {
		if (t == null) throw new InvalidDataException("Tradable object cannot be null.");
		getParent().addOldEntry(t);
	}
	
	/**
	 * Adds a tradable to the product book
	 * @param trd the tradable to add
	 * @throws InvalidDataException if the tradable is null
	 */
	public synchronized void addToBook(Tradable trd) throws InvalidDataException {
		if (trd == null) throw new InvalidDataException("The Tradable object cannot be null");
		if (!getBookEntries().containsKey(trd.getPrice())) {
			getBookEntries().put(trd.getPrice(), new ArrayList<Tradable>());
		}
		getBookEntries().get(trd.getPrice()).add(trd);
	}
	
	/**
	 * Attempts a trade with the provided Tradable against entries in this side's book
	 * @param trd the trade to attempt
	 * @return a HashMap of all the successful fill messages
	 * @throws InvalidDataException if the trade is null
	 */
	public HashMap<String, FillMessage> tryTrade(Tradable trd) throws InvalidDataException {
		if (trd == null) throw new InvalidDataException("The Tradable cannot be null.");
		HashMap<String, FillMessage> allFills;
		if (getSide() == BookSide.BUY) allFills = trySellAgainstBuySideTrade(trd);
		else allFills = tryBuyAgainstSellSideTrade(trd);
		for (FillMessage fill: allFills.values()) MessagePublisher.getInstance().publishFill(fill);
		return allFills;
	}
	
	/**
	 * Tries to fill the SELL side Tradable passed in against the content of the book
	 * @param trd the tradable to fill
	 * @return a HashMap of fill messages for all the successful fills
	 * @throws InvalidDataException if any of the Message content is invalid
	 */
	public synchronized HashMap<String, FillMessage> trySellAgainstBuySideTrade(Tradable trd) throws InvalidDataException {
		if (trd == null) throw new InvalidDataException("The trade cannot be null.");
		HashMap<String, FillMessage> allFills = new HashMap<String, FillMessage>();
		HashMap<String, FillMessage> fillMsgs = new HashMap<String, FillMessage>(); 
		while ((trd.getRemainingVolume() > 0 && !getBookEntries().isEmpty() && trd.getPrice().lessOrEqual(topOfBookPrice()))
				|| (trd.getRemainingVolume() > 0 && !getBookEntries().isEmpty() && trd.getPrice().isMarket())) {
				HashMap<String, FillMessage> someMsgs = getTradeProcessor().doTrade(trd);
				fillMsgs = mergeFills(fillMsgs,someMsgs);
		}
		allFills.putAll(fillMsgs);
		return allFills;
	}
	
	/**
	 * Merges fill messages into one consistent list
	 * @param existing the existing fill messages
	 * @param newOnes the new fill messages to combine with the existing messages
	 * @return the merged HashMap of the existing and new messages
	 * @throws InvalidDataException if any of the FillMessage data is invalid
	 */
	private HashMap<String, FillMessage> mergeFills(HashMap<String, FillMessage> existing, 
			HashMap<String, FillMessage> newOnes) throws InvalidDataException {
		if (existing.isEmpty()) return new HashMap<String, FillMessage>(newOnes); 
		HashMap<String, FillMessage> results = new HashMap<String, FillMessage>(existing);
		for (String key: newOnes.keySet()) {
			if (!existing.containsKey(key)) results.put(key, newOnes.get(key));
			else {
				FillMessage fm = results.get(key);
				fm.setVolume(newOnes.get(key).getVolume());
				fm.setDetails(newOnes.get(key).getDetails());
			}
		}
		return results;
	}
	
	/**
	 * Tries to fill the BUY side Tradable passed in against the content of the book
	 * @param trd the Tradable to attempt to fill
	 * @return a HashMap of fill messages for all the successful fills
	 * @throws InvalidDataException if any of the Message data is invalid
	 */
	public synchronized HashMap<String, FillMessage> tryBuyAgainstSellSideTrade(Tradable trd) throws InvalidDataException{
		HashMap<String, FillMessage> allFills = new HashMap<String, FillMessage>();
		HashMap<String, FillMessage> fillMsgs = new HashMap<String, FillMessage>(); 
		while ((trd.getRemainingVolume() > 0 && !getBookEntries().isEmpty() && trd.getPrice().greaterOrEqual(topOfBookPrice()))
				|| (trd.getRemainingVolume() > 0 && !getBookEntries().isEmpty() && trd.getPrice().isMarket())) {
				HashMap<String, FillMessage> someMsgs = getTradeProcessor().doTrade(trd);
				fillMsgs = mergeFills(fillMsgs,someMsgs);
		}
		allFills.putAll(fillMsgs);
		return allFills;
	}
	
	/**
	 * Removes any key/value pair from the book if the specified Price has no trades
	 * @param p the price to look up and remove if there are no trades
	 * @throws InvalidDataException if the price is null
	 */
	public synchronized void clearIfEmpty(Price p) throws InvalidDataException {
		if (p == null) throw new InvalidDataException("The price cannot be null");
		if (getBookEntries().containsKey(p)) {
			ArrayList<Tradable> trades = getBookEntries().get(p);
			if (trades.isEmpty()) getBookEntries().remove(p);
		}
	}
	 
	 public synchronized void removeTradable(Tradable t) throws InvalidDataException {
		 if (t == null) throw new InvalidDataException("The trade cannot be null.");
		 if (!getBookEntries().containsKey(t.getPrice())) return;
		 boolean removed = false;
		 ArrayList<Tradable> entries = getBookEntries().get(t.getPrice());
		 if (!entries.isEmpty() && entries != null) removed = entries.remove(t);
		 if (removed) {
			 if (entries.isEmpty()) clearIfEmpty(t.getPrice());
		 }
	 }

	
	/**
	 * Helper method to make a sorted ArrayList of the Prices in the product book
	 * @return a sorted ArrayList<Price> of the Prices in the product book
	 */
	private synchronized ArrayList<Price> sortPrices() {
		ArrayList<Price> sorted = new ArrayList<Price>(getBookEntries().keySet());
		Collections.sort(sorted);
		if (getSide() == BookSide.BUY) Collections.reverse(sorted);
		return sorted;
	}
	
	/**
	 * Helper method to calculate the total remaining volume of an ArrayList of tradables
	 * @param tradables the ArrayList of Tradables to count the remaining values of
	 * @return the sum of the remaining volume of all the Tradables in the ArrayList
	 */
	private synchronized int calcRemainingVolume(ArrayList<Tradable> tradables) {
		int volume = 0;
		for (Tradable t: tradables) {
			volume += t.getRemainingVolume();
		}
		return volume;
	}
	
	/**
	 * Helper method that constructs TradableDTO objects out of the provided Tradable object
	 * @param t the Tradable object to make a TradableDTO out of
	 * @return a TradableDTO with all of the information from the provided Tradable object
	 */
	private synchronized TradableDTO makeTradableDTO(Tradable t) {
		return new TradableDTO(t.getProduct(),t.getPrice(),t.getOriginalVolume(),t.getRemainingVolume(),
				t.getCancelledVolume(),t.getUser(),t.getSide(),t.isQuote(),t.getId());
	}
	
	/**
	 * Helper method that makes an Iterator that iterates through the product book
	 * @return an Iterator that iterates through the product book
	 */
	private synchronized Iterator<Price> makeBookIterator() {
		return getBookEntries().keySet().iterator();
	}
	
	/**
	 * Helper method that makes an ArrayList<Price> out of the keys in the
	 * product book
	 * @return an ArrayList<Price> of the keys in the product book
	 */
	private synchronized ArrayList<Price> makePriceArrayList() {
		ArrayList<Price> priceList = new ArrayList<Price>(getBookEntries().keySet());
		Collections.sort(priceList);
		Collections.reverse(priceList);
		return priceList;
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
	 * Helper method that makes a CancelMessage with the data in the TradableDTO object passed in.
	 * @param t the TradableDTO object to get information from
	 * @return a CancelMessage with the data from the TradableDTO passed in
	 * @throws InvalidDataException if there was invalid data in the TradableDTO object (thrown by CancelMessage constructor)
	 */
	private CancelMessage makeCancelMessage(TradableDTO t, String details) throws InvalidDataException {
		return new CancelMessage(t.user,t.product,t.price,
				t.remainingVolume,details,t.side,t.id);
	}
	
	/**
	 * Retrieves this object's bookEntries HashMap
	 * @return this object's bookEntries HashMap
	 */
	private HashMap<Price, ArrayList<Tradable>> getBookEntries() {
		return bookEntries;
	}
	
	/**
	 * Retrieve's the object's parent
	 * @return the object's parent
	 */
	private ProductBook getParent() {
		return parent;
	}
	
	/**
	 * Retrieves this object's BookSide side
	 * @return this object's BookSide side
	 */
	private BookSide getSide() {
		return side;
	}
	
	/**
	 * Sets the processor according to the algorithm specified in type
	 * @param type the trade processing algorithm to use
	 * @param parent the ProductBookSide that owns the processor
	 * @throws InvalidDataException if the ProductBookSide is null
	 */
	private void setProcessor(String type, ProductBookSide parent) throws InvalidDataException {
		processor = TradeProcessorImplFactory.createTradeProcessor(type, parent);
	}
	
	/**
	 * Retrieves this object's processor
	 * @return this object's processor
	 */
	private TradeProcessor getTradeProcessor() {
		return processor;
	}
	
	/**
	 * Sets this object's ProductBook parent
	 * @param pb the ProductBook that is this object's parent
	 * @throws InvalidDataException if the ProductBook is null
	 */
	private void setParent(ProductBook pb) throws InvalidDataException {
		if (pb == null) throw new InvalidDataException("The ProductBook cannot be null.");
		parent = pb;
	}
	
	/**
	 * Sets this object's BookSide side
	 * @param bs the BookSide that this object represents
	 * @throws InvalidDataException if the BookSide is null
	 */
	private void setSide(BookSide bs) throws InvalidDataException {
		if (bs == null) throw new InvalidDataException("The BookSide cannot be null.");
		side = bs;
	}
}
