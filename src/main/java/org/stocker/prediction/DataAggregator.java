package org.stocker.prediction;

import com.google.common.base.Strings;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import org.stocker.exceptions.StockReadException;
import org.stocker.nseData.NseDataRow;
import org.stocker.services.StockDBClient;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Date;

import static org.joda.time.LocalDate.now;
import static org.stocker.nseData.NseDataRow.*;

/**
 *  Aggregates data for prediction over specified date range
 */
public class DataAggregator {
    protected StockDBClient stockDBClient;

    public DataAggregator(StockDBClient stockDBClient) {
        this.stockDBClient = stockDBClient;
    }

    public DataAggregatorResponseObj getAggregatedDataOverLastDay(String symbol) throws StockReadException {
        Date today = now().toDate();
        Date lastDay = now().minusDays(1).toDate();
        return this.getAggregatedDataOverPeriod(symbol, lastDay, today);
    }

    public DataAggregatorResponseObj getAggregatedDataOverLastWeek(String symbol) throws StockReadException {
        Date today = now().toDate();
        Date lastWeek = now().minusWeeks(1).toDate();
        return this.getAggregatedDataOverPeriod(symbol, lastWeek, today);
    }

    public DataAggregatorResponseObj getAggregatedDataOverLastMonth(String symbol) throws StockReadException {
        Date today = now().toDate();
        Date lastMonth = now().minusMonths(1).toDate();
        return this.getAggregatedDataOverPeriod(symbol, lastMonth, today);
    }


    public DataAggregatorResponseObj getAggregatedDataOverPeriod(String symbol, Date from, Date to) throws StockReadException {
        if(Strings.isNullOrEmpty(symbol)){
            throw new StockReadException("empty stock symbol");
        }
        BasicDBObject queryCondition = new BasicDBObject();
        queryCondition.put(SYMBOL.name(), symbol);
        queryCondition.put(TIMESTAMP.name(), BasicDBObjectBuilder.start("$lte", to)
                                                                 .add("$gte", from)
                                                                 .get());
        JsonArray result = stockDBClient.retrieve(queryCondition);
        return aggredateData(result);
    }

    protected DataAggregatorResponseObj aggredateData(JsonArray data){
        final DataAggregatorResponseObj responseObj = new DataAggregatorResponseObj();
        data.forEach(o -> {
            JsonObject obj = (JsonObject) o ;
            if(Double.valueOf(obj.getString(HIGH.name())) > responseObj.high){
                responseObj.high = Double.valueOf( obj.getString(HIGH.name()) );
            }
            if(Double.valueOf(obj.getString(LOW.name())) > responseObj.low){
                responseObj.low = Double.valueOf( obj.getString(LOW.name()) );
            }
            if(Double.valueOf(obj.getString(CLOSE.name())) > responseObj.close){
                responseObj.close = Double.valueOf( obj.getString(CLOSE.name()) );
            }
        });
        return responseObj;
    }


}
