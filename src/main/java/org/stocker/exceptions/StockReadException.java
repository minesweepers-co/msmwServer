package org.stocker.exceptions;

public class StockReadException extends Exception{

    public StockReadException() {
        super("Could not retrieve stock data");
    }

    public StockReadException(String err){
        super(err);
    }

    public StockReadException(Exception e) {
        super(e);
    }
}
