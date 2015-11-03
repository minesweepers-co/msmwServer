package org.stocker.routes;

import com.google.common.base.Strings;
import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import org.stocker.exceptions.StockReadException;
import org.stocker.services.StockDBClient;
import org.stocker.util.DateParsers;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.text.ParseException;
import java.util.Date;

import static org.stocker.nseData.NseDataRow.SYMBOL;
import static org.stocker.nseData.NseDataRow.TIMESTAMP;

/**
 * Created by Horsie on 11/1/15.
 *
 */
public class GetStockRoute extends Middleware {
    protected StockDBClient mStockDBClient;
    public GetStockRoute(StockDBClient stockDBClient) {
        mStockDBClient = stockDBClient;
    }

    @Override
    public void handle(YokeRequest yokeRequest, Handler<Object> handler) {
        String stock = yokeRequest.params().get("stock");
        String date = yokeRequest.params().get("date");
        if (Strings.isNullOrEmpty(stock)  || Strings.isNullOrEmpty(date)) {
            yokeRequest.response().setStatusCode(400).end();
            return;
        }
        Date dateObj;

        try {
            dateObj = DateParsers.ISODateFormat.parse(date);
        } catch (ParseException e) {
            yokeRequest.response().setStatusCode(400).end();
            return;
        }

        JsonArray results;
        try {
            BasicDBObject queryObject = new BasicDBObject();
            queryObject.put(SYMBOL.name(), stock);
//            JsonObject dateQuery = new JsonObject();
//            dateQuery.putValue("$lte", dateObj);
//            queryObject.put(TIMESTAMP.name(), dateQuery);
            queryObject.put(TIMESTAMP.name(), BasicDBObjectBuilder.start("$lte", dateObj).get());

            results = mStockDBClient.retrieve(queryObject);
        } catch (StockReadException e) {
            yokeRequest.response().setStatusCode(500).end(new JsonObject().putString("error", e.getLocalizedMessage()));
            return;
        }
        yokeRequest.response().end(results);
    }
}
