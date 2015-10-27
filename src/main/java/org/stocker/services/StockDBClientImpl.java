package org.stocker.services;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.WriteResult;
import org.stocker.nseData.NseDataObj;

import java.util.List;
import java.util.Map;

import static org.stocker.nseData.NseDataRow.SYMBOL;

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

    public List<NseDataObj> retrieve(){
        return null;
    }

}
