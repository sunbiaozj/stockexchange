package stockexchange.book;

import java.util.HashMap;

import stockexchange.common.InvalidDataException;
import stockexchange.messages.FillMessage;
import stockexchange.tradable.Tradable;

/**
 * Interface that defines the functionality needed to execute trades 
 * between tradeable objects in a book side.
 * @author ScottMores
 *
 */
public interface TradeProcessor {
	public HashMap<String, FillMessage> doTrade(Tradable trd) throws InvalidDataException ;
}
