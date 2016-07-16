package org.stocker.services;

import com.mongodb.BasicDBObject;
import org.stocker.exceptions.StockReadException;
import org.stocker.nasdaqData.NasdaqDataObj;
import org.stocker.nseData.NseDataObj;
import org.vertx.java.core.json.JsonArray;

public interface StockDBClient {
    boolean insert(NseDataObj dataObj);
    JsonArray retrieve(String symbol) throws StockReadException; // TODO: Consider changing return type to NseDataObj
    JsonArray retrieve(BasicDBObject queryConditions) throws StockReadException;

    boolean insert(NasdaqDataObj nasdaqDataObj);
}
