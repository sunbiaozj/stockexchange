package stockexchange.messages;

import stockexchange.common.GlobalConstants.BookSide;
import stockexchange.common.InvalidDataException;
import stockexchange.price.Price;

/**
 * An abstract class to represent various kinds of messages that will
 * be passed in the trading system.
 * @author ScottMores
 *
 */
public abstract class Message implements Comparable<Message> {
	//The username of the user attached to this message.
	private String user;
	//The stock symbol (product) attached to this message.
	private String product;
	//The price attached to this message
	private Price price;
	//The quantity attached to this order.
	private int volume;
	//A description of this message
	private String details;
	//The BUY or SELL side attached to this message.
	private BookSide side;
	//The String identifier attached this message
	private String id;
	
	/**
	 * Constructor for Message objects.
	 * @param userIn the user attached to this message.
	 * @param productIn the product/stock symbol attached to this message.
	 * @param priceIn the price attached to this message.
	 * @param volumeIn the volume attached to this message.
	 * @param detailsIn the details attached to this message.
	 * @param sideIn the Buy/Sell side attached to this message.
	 * @param idIn the id attached to this message.
	 * @throws InvalidDataException if any parameters are null or if the volume is negative.
	 */
	protected Message(String userIn, String productIn, Price priceIn, 
			int volumeIn, String detailsIn, BookSide sideIn, String idIn) throws InvalidDataException {
		setUser(userIn);
		setProduct(productIn);
		setPrice(priceIn);
		setVolume(volumeIn);
		setDetails(detailsIn);
		setBookSide(sideIn);
		setId(idIn);
	}
	
	/**
	 * Default constructor that allows subclasses to extend this class properly.
	 */
	protected Message() {}
	
	/**
	 * Sets the username for this message.
	 * @param userIn the username to be set.
	 * @throws InvalidDataException if the username is null or empty.
	 */
	private void setUser(String userIn) throws InvalidDataException {
		if (userIn == null || userIn.equals("")) throw new InvalidDataException("Username cannot be null or empty.");
		user = userIn;
	}
	
	/**
	 * Sets the product for this message.
	 * @param productIn the stock name to be set.
	 * @throws InvalidDataException if the product is null or empty.
	 */
	private void setProduct(String productIn) throws InvalidDataException {
		if (productIn == null || productIn.equals("")) throw new InvalidDataException("Product cannot be null or empty.");
		product = productIn;
	}
	
	/**
	 * Sets the price for this message.
	 * @param priceIn the Price object to be set. 
	 * @throws InvalidDataException if the price is null.
	 */
	private void setPrice(Price priceIn) throws InvalidDataException {
		if (priceIn == null) throw new InvalidDataException("Price cannot be null.");
		price = priceIn;
	}
	
	/**
	 * Sets the volume for this message.
	 * @param volumeIn the volume to be set.
	 * @throws InvalidDataException if the volume is negative.
	 */
	public void setVolume(int volumeIn) throws InvalidDataException {
		if (volumeIn < 0) throw new InvalidDataException("Volume cannot be negative.");
		volume = volumeIn;
	}
	
	/**
	 * Sets the details of this message.
	 * @param detailsIn the details to be set.
	 * @throws InvalidDataException if the details are null.
	 */
	public void setDetails(String detailsIn) throws InvalidDataException {
		if (detailsIn == null) throw new InvalidDataException("Details cannot be null.");
		details = detailsIn;
	}
	
	/**
	 * Sets the BookSide for this message.
	 * @param sideIn the BookSide enum to be set.
	 * @throws InvalidDataException if the BookSide is null.
	 */
	private void setBookSide(BookSide sideIn) throws InvalidDataException {
		if (sideIn == null) throw new InvalidDataException("BookSide cannot be null.");
		side = sideIn;
	}
	
	/**
	 * Sets the ID for this message.
	 * @param idIn the ID to be set.
	 * @throws InvalidDataException if the ID is null.
	 */
	private void setId(String idIn) throws InvalidDataException {
		if (idIn == null) throw new InvalidDataException("ID cannot be null.");
		id = idIn;
	}
	
	/**
	 * Provides access to the user attached to this message.
	 * @return the user String attached to this message.
	 */
	public String getUser() {
		return user;
	}
	
	/**
	 * Provides access to the product attached to this message.
	 * @return the product String attached to this message.
	 */
	public String getProduct() {
		return product;
	}
	
	/**
	 * Provides access to the price attached to this message.
	 * @return the Price object attached to this message.
	 */
	public Price getPrice() {
		return price;
	}
	
	/**
	 * Provides access to the volume attached to this message.
	 * @return the volume int attached to this message.
	 */
	public int getVolume() {
		return volume;
	}
	
	/**
	 * Provides access to the details of this message.
	 * @return the details String attached to this message.
	 */
	public String getDetails() {
		return details;
	}
	
	/**
	 * Provides access to the BookSide side of this message.
	 * @return the BookSide side attached to this message.
	 */
	public BookSide getBookSide() { 
		return side;
	}
	
	/**
	 * Provides access to the ID of this message.
	 * @return the ID attached to this message.
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Compares two CancelMessage objects according to their Price members.
	 * @return 1 if this message's Price is greater than other's Price, -1 if this message's Price is 
	 * less than other's Price and 0 if the two messages' Prices are equal.
	 */
	public int compareTo(Message other) {
		return getPrice().compareTo(other.getPrice());
	}
}
