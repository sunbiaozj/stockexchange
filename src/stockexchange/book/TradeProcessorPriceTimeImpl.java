package stockexchange.book;

import java.util.ArrayList;
import java.util.HashMap;

import stockexchange.common.InvalidDataException;
import stockexchange.messages.FillMessage;
import stockexchange.price.Price;
import stockexchange.tradable.Tradable;

/**
 * A class that contains the functionality needed to execute trades between Tradable objects in this
 * book side using a price-time algorithm
 * @author ScottMores
 *
 */
public class TradeProcessorPriceTimeImpl implements TradeProcessor {
	
	/** A listing of fill messages indexed by trade identifier */
	private HashMap<String, FillMessage> fillMessages = new HashMap<String, FillMessage>(); 
	/** The ProductBookSide that this processor belongs to */
	private ProductBookSide book;
	
	/**
	 * Constructs a new TradeProcessorTimeImpl that implements the TradeProcessor interface using a 
	 * price-time algorithm
	 * @param productBookSideIn the ProductBookSide that this object belongs to
	 * @throws InvalidDataException if the ProductBookSide is null
	 */
	public TradeProcessorPriceTimeImpl(ProductBookSide productBookSideIn) throws InvalidDataException {
		setBook(productBookSideIn);
	}
	
	/**
	 * Creates a key out of the data in the fill message so that the system can tell fill messages apart
	 * @param fm the fill message to make a key for
	 * @return a String that represents the fill message's unique key
	 * @throws InvalidDataException if the fill message is null
	 */
	private String makeFillKey(FillMessage fm) throws InvalidDataException {
		if (fm == null) throw new InvalidDataException("The fill message cannot be null");
		return fm.getUser() + fm.getId() + fm.getPrice();
	}
	
	/**
	 * Checks whether or not the FillMessage passed in is a message for an existing known trade
	 * @param fm the fill message to identify
	 * @return true if this fill message is not for an existing known trade and false otherwise
	 * @throws InvalidDataException if the FillMessage passed in is null
	 */
	private boolean isNewFill(FillMessage fm) throws InvalidDataException {
		if (fm == null) throw new InvalidDataException("The fill message cannot be null");
		String key = makeFillKey(fm);
		if (!getFillMessages().containsKey(key)) return true;
		else {
			FillMessage oldFill = getFillMessages().get(key);
			if (!oldFill.getBookSide().equals(fm.getBookSide())) return true;
			if (!oldFill.getId().equals(fm.getId())) return true;
			return false;
		}
	}
	
	/**
	 * Adds a fill message to the fillMessages HashMap or updates an existing fill message
	 * @param fm the fill message to add or be used to update an existing fill message
	 * @throws InvalidDataException if the fill message is null
	 */
	private void addFillMessage(FillMessage fm) throws InvalidDataException {
		if (fm == null) throw new InvalidDataException("The fill message cannot be null");
		String key;
		if (isNewFill(fm)) {
			key = makeFillKey(fm);
			getFillMessages().put(key, fm);
		}
		else {
			key = makeFillKey(fm);
			FillMessage oldFill = getFillMessages().get(key);
			oldFill.setVolume(oldFill.getVolume() + fm.getVolume());
			oldFill.setDetails(fm.getDetails());
		}
	}
	
	/**
	 * Private getter method that retrieves the fillMessages HashMap
	 * @return the HashMap of fill messages
	 */
	private HashMap<String, FillMessage> getFillMessages() {
		return fillMessages;
	}
	
	/**
	 * Private getter method to retrieve this processor's ProductBookSide
	 * @return the ProductBookSide that this processor belongs to
	 */
	private ProductBookSide getBook() {
		return book;
	}
	
	/**
	 * Private setter method that resets the fillMessages member variable
	 * to a new HashMap
	 */
	private void resetFillMessages() {
		fillMessages = new HashMap<String, FillMessage>();
	}
	
	/**
	 * Private setter method for this object's ProductBookSide data member
	 * @param productBookSideIn the ProductBookSide to set the book to
	 * @throws InvalidDataException if the ProductBookSide is null
	 */
	private void setBook(ProductBookSide productBookSideIn) throws InvalidDataException {
		if (productBookSideIn == null) throw new InvalidDataException("The ProductBookSide cannot be null");
		book = productBookSideIn;
	}

	
	/**
	 * Processes trades against the content of the book using price-time a algorithm
	 * @param trd the trade to process
	 * @throws InvalidDataException if the Tradable passed in is null
	 */
	public HashMap<String, FillMessage> doTrade(Tradable trd) throws InvalidDataException {
		if (trd == null) throw new InvalidDataException("Cannot process a trade on a null Tradable");
		resetFillMessages();
		ArrayList<Tradable> tradedOut = new ArrayList<Tradable>();
		ArrayList<Tradable> entriesAtPrice = getBook().getEntriesAtTopOfBook();
		for (Tradable t: entriesAtPrice) {
			if (trd.getRemainingVolume() != 0) {
				Price tPrice;
				if (trd.getRemainingVolume() >= t.getRemainingVolume()) {
					tradedOut.add(t);
					if (t.getPrice().isMarket()) tPrice = trd.getPrice();
					else tPrice = t.getPrice();
					FillMessage tFill = new FillMessage(t.getUser(), t.getProduct(), tPrice, t.getRemainingVolume(), 
							"leaving 0", t.getSide(), t.getId());
					addFillMessage(tFill);
					FillMessage trdFill = new FillMessage(trd.getUser(), trd.getProduct(), tPrice, t.getRemainingVolume(), 
							"leaving " + (trd.getRemainingVolume() - t.getRemainingVolume()), trd.getSide(), trd.getId());
					addFillMessage(trdFill);
					trd.setRemainingVolume(trd.getRemainingVolume() - t.getRemainingVolume());
					t.setRemainingVolume(0);
					getBook().addOldEntry(t);
				}
				//trd's remaining volume is less than t's remaining volume
				else {
					int remainder = t.getRemainingVolume() - trd.getRemainingVolume();
					if (t.getPrice().isMarket()) tPrice = trd.getPrice();
					else tPrice = t.getPrice();
					FillMessage tFill = new FillMessage(t.getUser(), t.getProduct(), tPrice, trd.getRemainingVolume(), 
							"leaving " + remainder, t.getSide(), t.getId());
					addFillMessage(tFill);
					FillMessage trdFill = new FillMessage(trd.getUser(), trd.getProduct(), tPrice, trd.getRemainingVolume(), 
							"leaving 0", trd.getSide(), trd.getId());
					addFillMessage(trdFill);
					trd.setRemainingVolume(0);
					t.setRemainingVolume(remainder);
					getBook().addOldEntry(trd);
				}
			}
		}
		for (Tradable t: tradedOut) entriesAtPrice.remove(t);
		if (entriesAtPrice.isEmpty()) getBook().clearIfEmpty(getBook().topOfBookPrice());
		return getFillMessages();
	}
}
