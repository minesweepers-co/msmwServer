package org.stocker.services;

import org.stocker.nseData.NseDataObj;
import org.vertx.java.core.json.JsonObject;

/**
 *
 */
public interface StockDBClient {
    boolean insert(NseDataObj dataObj);
    JsonObject retrieve(String symbol); // TODO: Consider changing return type to NseDataObj
}
