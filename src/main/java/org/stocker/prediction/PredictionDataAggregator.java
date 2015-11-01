package org.stocker.prediction;

import com.mongodb.BasicDBObject;
import org.stocker.exceptions.StockReadException;
import org.stocker.services.StockDBClient;
import org.vertx.java.core.json.JsonObject;

import java.util.Date;

/**
 *  Aggregates data for prediction over specified date range
 */
public class PredictionDataAggregator {
    protected StockDBClient stockDBClient;

    public PredictionDataAggregator(StockDBClient stockDBClient) {
        this.stockDBClient = stockDBClient;
    }

    public DataAggregatorResponseObj getAggregatedDataOverPeriod(Date from, Date to) throws StockReadException {
        BasicDBObject queryCondition = new BasicDBObject();
        JsonObject dateQuery = new JsonObject();
        dateQuery.putString("$lte", to.toString());
        dateQuery.putString("$gte", from.toString());
        queryCondition.put("TIMESTAMP", dateQuery);
        stockDBClient.retrieve(queryCondition);
        return null;
    }
}
