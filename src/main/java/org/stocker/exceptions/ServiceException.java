package org.stocker.exceptions;

public enum ServiceException {

    INTERNAL_SERVER_ERROR("Oops , something went wrong"),
    BAD_REQUEST("Invalid params provided, bad request"),
    REQUEST_IN_INVALID_FORMAT("Provided inputs are in invalid format"),
    ILLEGAL_REQUEST("Current request is illegal"),
    STOCK_INFO_NOT_FOUND("No data found for the symbol");

    private String status;

    private ServiceException(String s) {
        status = s;
    }

    public String getStatus() {
        return status;
    }
}
