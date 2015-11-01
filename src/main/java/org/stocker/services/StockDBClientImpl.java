package org.stocker.services;

import com.mongodb.*;
import com.sun.istack.internal.NotNull;
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
    public JsonObject retrieve(@NotNull String symbol) {
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put(NseDataRow.SYMBOL.name(), symbol);

        DBCursor cursor = stockCollection.find(searchQuery);
        JsonObject jsonObject = new JsonObject();
        if (cursor.hasNext()) {
            DBObject object = cursor.next();
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
