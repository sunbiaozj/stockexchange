package stockexchange.messages;

import stockexchange.common.GlobalConstants.MarketState;
import stockexchange.common.InvalidDataException;

/**
 * Encapsulates data related to the state of the market.
 * @author ScottMores
 *
 */
public class MarketMessage {
	private MarketState state;
	
	/**
	 * Constructs a new MarketMessage.
	 * @param inState the state of this message as a MarketState object.
	 * @throws InvalidDataException if the state is null.
	 */
	public MarketMessage(MarketState inState) throws InvalidDataException {
		setState(inState);
	}
	
	/**
	 * Sets the state of this message.
	 * @param inState the state used to set this message's state.
	 * @throws InvalidDataException if the state is null.
	 */
	private void setState(MarketState inState) throws InvalidDataException {
		if (inState == null) throw new InvalidDataException("The state cannot be null.");
		state = inState;
	}
	
	/**
	 * Provides public access to this message's state.
	 * @return a MarketState object with a value of CLOSED, PROPEN or OPEN
	 */
	public MarketState getState() {
		return state;
	}
	
	/**
	 * Overrides Java's default implementation of toString() to produce a String
	 * representation of this object.
	 * @return a String representation of a MarketMessage
	 */
	public String toString() {
		return getState().toString();
	}
}
