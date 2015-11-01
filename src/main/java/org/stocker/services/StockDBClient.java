package org.stocker.services;

import com.mongodb.BasicDBObject;
import org.stocker.exceptions.StockReadException;
import org.stocker.nseData.NseDataObj;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 *
 */
public interface StockDBClient {
    boolean insert(NseDataObj dataObj);
    JsonArray retrieve(String symbol) throws StockReadException; // TODO: Consider changing return type to NseDataObj
    JsonArray retrieve(BasicDBObject queryConditions) throws StockReadException;
}
