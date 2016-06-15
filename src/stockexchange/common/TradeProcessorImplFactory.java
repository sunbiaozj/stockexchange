package stockexchange.common;

import stockexchange.book.ProductBookSide;
import stockexchange.book.TradeProcessor;
import stockexchange.book.TradeProcessorPriceTimeImpl;

/**
 * Factory for creating implementors of the TradeProcessor interface
 * @author ScottMores
 *
 */
public class TradeProcessorImplFactory {
	
	/**
	 * Creates a TradeProcessor implementor based on the description passed in via the constructor
	 * @param implType the type of implementor to return
	 * @return the specified type of TradeProcessor implementor
	 * @throws InvalidDataException if the ProductBookSide is null
	 */
	public static TradeProcessor createTradeProcessor(String implType, ProductBookSide productBookSideIn) throws InvalidDataException {
		
		TradeProcessor implementor = null;
		
		switch (implType) {
			case "pricetime": implementor = new TradeProcessorPriceTimeImpl(productBookSideIn);
		}
		
		return implementor;
	}
}
