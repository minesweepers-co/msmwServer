package org.stocker.exceptions;

/**
 *
 */
public class StockDataNotFoundForGivenDateException extends Exception {
    public StockDataNotFoundForGivenDateException() {
        super("No stock data found for the given date");
    }

    public StockDataNotFoundForGivenDateException(Exception e) {
        super(e);
    }
}
