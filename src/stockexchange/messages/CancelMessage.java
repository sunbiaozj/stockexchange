package stockexchange.messages;

import stockexchange.common.GlobalConstants.BookSide;
import stockexchange.common.InvalidDataException;
import stockexchange.price.Price;

/**
 * Encapsulates data related to the cancellation of an order or quote-side by a
 * user, or by the trading system. This is an immutable class.
 * @author ScottMores
 *
 */
public final class CancelMessage extends Message implements Comparable<Message> {
	
	/**
	 * Constructs a new CancelMessage.
	 * @param userIn the user attached to this message.
	 * @param productIn the product/stock symbol attached to this message.
	 * @param priceIn the price attached to this message.
	 * @param volumeIn the volume attached to this message.
	 * @param detailsIn the details attached to this message.
	 * @param sideIn the side attached to this message.
	 * @param idIn the ID attached to this message.
	 * @throws InvalidDataException if any parameters are null or if the volume is negative.
	 */
	public CancelMessage (String userIn, String productIn, Price priceIn, 
			int volumeIn, String detailsIn, BookSide sideIn, String idIn) throws InvalidDataException {
		super(userIn,productIn,priceIn,volumeIn,detailsIn,sideIn,idIn);
	}
	
	
	/**
	 * Overrides Java's default toString() implementation to construct a String representation of this object.
	 * @return a String representation of this object.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("User: ");
		sb.append(getUser());
		sb.append(", Product: ");
		sb.append(getProduct());
		sb.append(", Price: ");
		sb.append(getPrice());
		sb.append(", Volume: ");
		sb.append(getVolume());
		sb.append(", Details: ");
		sb.append(getDetails());
		sb.append(", Side: ");
		sb.append(getBookSide());
		sb.append(", Id: ");
		sb.append(getId());
		return sb.toString();
	}
}
