package stockexchange.messages;

import stockexchange.common.GlobalConstants.BookSide;
import stockexchange.common.InvalidDataException;
import stockexchange.price.Price;

/**
 * Encapsulates data related to the fill (trade) of an order or quote-side. This
 * is an immutable class.
 * @author ScottMores
 *
 */
public final class FillMessage extends Message implements Comparable<Message>{
	
	/**
	 * Constructs a new FillMessage.
	 * @param userIn the user attached to this message.
	 * @param productIn the product/stock symbol attached to this message.
	 * @param priceIn the price attached to this message.
	 * @param volumeIn the volume attached to this message.
	 * @param detailsIn the details attached to this message.
	 * @param sideIn the side attached to this message.
	 * @param idIn the ID attached to this message.
	 * @throws InvalidDataException if any parameters are null or if the volume is negative.
	 */
	public FillMessage (String userIn, String productIn, Price priceIn, 
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
		return sb.toString();
	}

}
