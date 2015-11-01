package org.stocker.services;

import com.google.common.base.Strings;
import com.mongodb.*;
import org.stocker.nseData.NseDataObj;
import org.stocker.nseData.NseDataRow;
import org.vertx.java.core.json.JsonObject;

import java.util.List;
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
        WriteResult result = stockCollection.insert(document);
        return result.getLastError().ok();
    }

    @Override
    public JsonObject retrieve(String symbol) {
        JsonObject jsonObject = new JsonObject();
        if (Strings.isNullOrEmpty(symbol)) {
            return jsonObject.putString("error", "symbol should never be empty or null");
        }

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put(NseDataRow.SYMBOL.name(), symbol);

        DBCursor cursor = stockCollection.find(searchQuery);
        if (cursor.hasNext()) {
            DBObject object = cursor.next();
            jsonObject.putString(NseDataRow.SYMBOL.toString(), object.get(NseDataRow.SYMBOL.toString()).toString());
            jsonObject.putString(NseDataRow.LOW.toString(), object.get(NseDataRow.LOW.toString()).toString());
            jsonObject.putString(NseDataRow.HIGH.toString(), object.get(NseDataRow.HIGH.toString()).toString());
            jsonObject.putString(NseDataRow.CLOSE.toString(), object.get(NseDataRow.CLOSE.toString()).toString());
        }
        return jsonObject;
    }

    public List<NseDataObj> retrieve(){
        return null;
    }
}
