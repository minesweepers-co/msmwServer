package org.stocker.services;

import com.google.common.base.Strings;
import com.mongodb.*;
import org.stocker.exceptions.StockReadException;
import org.stocker.nasdaqData.NasdaqDataObj;
import org.stocker.nseData.NseDataObj;
import org.stocker.nseData.NseDataRow;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Map;

/**
 *
 */
public class StockDBClientImpl implements StockDBClient {

    protected DBCollection stockCollection;

    public StockDBClientImpl(DBCollection stockCollection) {
        this.stockCollection = stockCollection;
    }

    @Override
    public boolean insert(NseDataObj dataObj){
        BasicDBObject document = new BasicDBObject();
        for(Map.Entry dataEntry : dataObj.rowData.entrySet()){
            document.put(dataEntry.getKey().toString(), dataEntry.getValue().toString());
        }
        document.put(NseDataRow.EXPIRY_DT.name(), dataObj.expiryDate);
        document.put(NseDataRow.TIMESTAMP.name(), dataObj.timestamp);
        WriteResult result = stockCollection.insert(document);
        return result.getLastError().ok();
    }

    @Override
    public JsonArray retrieve(String symbol) throws StockReadException {
        if(Strings.isNullOrEmpty(symbol)){
            throw new StockReadException("empty symbol");
        }
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put(NseDataRow.SYMBOL.name(), symbol);
        DBCursor cursor = stockCollection.find(searchQuery);
        JsonArray response = new JsonArray();
        while (cursor.hasNext()) {
            DBObject object = cursor.next();
            JsonObject jsonObject = new JsonObject();
            // TODO : change this to auto mapping
            jsonObject.putString(NseDataRow.SYMBOL.toString(), object.get(NseDataRow.SYMBOL.toString()).toString());
            jsonObject.putString(NseDataRow.LOW.toString(), object.get(NseDataRow.LOW.toString()).toString());
            jsonObject.putString(NseDataRow.HIGH.toString(), object.get(NseDataRow.HIGH.toString()).toString());
            jsonObject.putString(NseDataRow.CLOSE.toString(), object.get(NseDataRow.CLOSE.toString()).toString());
            jsonObject.putString(NseDataRow.TIMESTAMP.toString(), object.get(NseDataRow.TIMESTAMP.toString()).toString());
            response.add(jsonObject);
        }
        return response;
    }

    @Override
    public JsonArray retrieve(BasicDBObject queryConditions) throws StockReadException {
        if(queryConditions == null || queryConditions.isEmpty()){
            throw new StockReadException("null/ empty queryConditions");
        }
        DBCursor cursor = stockCollection.find(queryConditions);
        JsonArray response = new JsonArray();
        while (cursor.hasNext()) {
            DBObject object = cursor.next();
            JsonObject jsonObject = new JsonObject();
            // TODO : change this to auto mapping
            jsonObject.putString(NseDataRow.LOW.toString(), object.get(NseDataRow.LOW.toString()).toString());
            jsonObject.putString(NseDataRow.HIGH.toString(), object.get(NseDataRow.HIGH.toString()).toString());
            jsonObject.putString(NseDataRow.CLOSE.toString(), object.get(NseDataRow.CLOSE.toString()).toString());
            jsonObject.putString(NseDataRow.TIMESTAMP.toString(), object.get(NseDataRow.TIMESTAMP.toString()).toString());
            response.add(jsonObject);
        }
        return response;
    }

    @Override
    public boolean insert(NasdaqDataObj dataObj) {
        BasicDBObject document = new BasicDBObject();
        for(Map.Entry dataEntry : dataObj.rowData.entrySet()){
            document.put(dataEntry.getKey().toString(), dataEntry.getValue().toString());
        }

        document.put(NseDataRow.TIMESTAMP.name(), dataObj.timestamp);
        WriteResult result = stockCollection.insert(document);
        return result.getLastError().ok();
    }
}
