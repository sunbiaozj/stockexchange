package stockexchange.price;

import java.text.NumberFormat;
import stockexchange.common.InvalidPriceOperation;

/**
 * The price class represents prices that are used throughout 
 * the stockexchange application.
 * @author ScottMores
 */
public class Price implements Comparable<Price> {
	
	//holds the numerical value for the price object as a long.
	private long value;
	
	/**
	 * Constructs a Price object that will represent a price and be used
	 * by other objects in the application.
	 * @param inValue the value in cents that this price object will
	 * represent.
	 */
	Price (long inValue) {
		setValue(inValue);
	}
	
	/**
	 * Default constructor used so the MarketPrice class does not need
	 * to implement its own constructor.
	 */
	Price () {
	}
	
	/**
	 * Sets the value for this price object.
	 * @param inValue the value to set this price object to.
	 */
	private void setValue(long inValue) {
		value = inValue;
	}
	
	/**
	 * Retrieves the value that this Price object represents.
	 * @return a long value that represents a monetary value in cents.
	 */
	public long getValue() {
		return value;
	}
	
	/**
	 * Adds the values of two Price objects together.
	 * @param p the Price object that will be added to this object.
	 * @return a new Price object that represents the value of the sum.
	 * @throws InvalidPriceOperation if either Price object is a MarketPrice
	 * or either Price object is null.
	 */
	public Price add(Price p) throws InvalidPriceOperation {
		if (p.isMarket() || isMarket()) {
			throw new InvalidPriceOperation("You cannot perform addition involving market prices");
		}
		if (p==null || this==null) {
			throw new InvalidPriceOperation("Both Price objects must be non-null in order to perform addition");
		}
		long sum = getValue() + p.getValue();
		return PriceFactory.makeLimitPrice(sum);
	}
	
	/**
	 * Subtracts the value of a Price object from the value of this Price object.
	 * @param p the Price object that will be subtracted.
	 * @return a new Price object that represents the difference.
	 * @throws InvalidPriceOperation if either Price object is a MarketPrice or either
	 * Price object is null.
	 */
	public Price subtract(Price p) throws InvalidPriceOperation {
		if (p.isMarket() || isMarket()) {
			throw new InvalidPriceOperation("You cannot perform subtraction involving market prices");
		}
		if (p==null || this==null) {
			throw new InvalidPriceOperation("Both Price objects must be non-null in order to perform subtraction");
		}
		long diff = getValue() - p.getValue();
		return PriceFactory.makeLimitPrice(diff);
	}
	
	/**
	 * Multiplies the value of a Price object by an integer.
	 * @param p the integer that will be multiplied by the Price object's value.
	 * @return a new Price object that represents the value of the product.
	 * @throws InvalidPriceOperation if this object is a MarketPrice.
	 */
	public Price multiply(int p) throws InvalidPriceOperation {
		if (isMarket()) {
			throw new InvalidPriceOperation("You cannot perform multiplication involving market prices");
		}
		long prod = getValue() * p;
		return PriceFactory.makeLimitPrice(prod);
	}
	
	/**
	 * Checks whether or not this object is a MarketPrice.
	 * @return true if this object is a MarketPrice and false otherwise.
	 */
	public boolean isMarket() {
		return false; // This method is overridden in the MarketPrice class to return True
	}
	
	/**
	 * Checks whether or not this Price object represents a negative value.
	 * @return true if this object represents a negative value and false otherwise.
	 */
	public boolean isNegative() {
		if (isMarket()) return false;
		return (getValue() < 0) ? true : false;
	}
	
	/**
	 * Compares the values of two price objects to determine whether this object is greater than, 
	 * less than or equal to the given Price object.
	 * @param p the Price object that this Price object will be checked against.
	 * @return 1 if this value is greater than p, -1 if this value is less than p 
	 * and 0 if the two objects are equal.
	 */
	public int compareTo(Price p) {
		if (getValue() > p.getValue()) return 1;
		else if (getValue() < p.getValue()) return -1;
		else return 0;
	}
	
	/**
	 * Checks whether or not this Price object is greater than or equal to the given 
	 * Price object.
	 * @param p the Price object that will be checked against this Price object.
	 * @return true if this Price object is greater than or equal to p and false otherwise.
	 */
	public boolean greaterOrEqual(Price p) {
		if (isMarket() || p.isMarket()) return false;
		return (compareTo(p) >= 0) ? true : false;
	}
	
	/**
	 * Checks whether or not this Price object is greater than the given 
	 * Price object.
	 * @param p the Price object that will be checked against this Price object.
	 * @return true if this Price object is greater than p and false otherwise.
	 */
	public boolean greaterThan(Price p) {
		if (isMarket() || p.isMarket()) return false;
		return (compareTo(p) > 0) ? true : false;
	}
	
	/**
	 * Checks whether or not this Price object is less than or equal to 
	 * the given Price object.
	 * @param p the Price object that will be checked against this Price object.
	 * @return true if this Price object is less than or equal to p and false otherwise.
	 */
	public boolean lessOrEqual(Price p) {
		if (isMarket() || p.isMarket()) return false;
		return (compareTo(p) <= 0) ? true : false;
	}
	
	/**
	 * Checks whether or not this Price object is less than the given Price object.
	 * @param p the Price object that will be checked against this Price object.
	 * @return true if this Price object is less than p and false otherwise.
	 */
	public boolean lessThan(Price p) {
		if (isMarket() || p.isMarket()) return false;
		return (compareTo(p) < 0) ? true : false;
	}
	
	/**
	 * Checks whether or not this Price object is equal to the given Price object.
	 * @param p the Price object that will be checked against this Price object.
	 * @return true if the two Price objects are equal and false otherwise.
	 */
	public boolean equals(Price p) {
		if (isMarket() || p.isMarket()) return false;
		return (compareTo(p) == 0) ? true : false;
	}
	
	/**
	 * Overrides Java's default toString() implementation and returns a String 
	 * representation of the object.
	 */
	public String toString() {
		if (isMarket()) return "MKT";
		double actualPrice = (double) getValue() / 100;
		if (!isNegative()) return NumberFormat.getCurrencyInstance().format(actualPrice);
		else {
			String positiveValue = NumberFormat.getCurrencyInstance().format(Math.abs(actualPrice));
			return "$-" + positiveValue.substring(1);
		}
	}
}
