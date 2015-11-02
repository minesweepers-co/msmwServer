package org.stocker.exceptions;


public class NseDataObjParseException extends Exception{

    public NseDataObjParseException() {
        super("Could not parse data into NseDataObj");
    }

    public NseDataObjParseException(Exception e) {
        super(e);
    }
}

