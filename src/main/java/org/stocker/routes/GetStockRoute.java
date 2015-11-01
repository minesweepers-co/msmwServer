package org.stocker.routes;

import com.google.common.base.Strings;
import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import org.stocker.exceptions.StockReadException;
import org.stocker.services.StockDBClient;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

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
        if (Strings.isNullOrEmpty(stock)) {
            yokeRequest.response().setStatusCode(400).end();
        }

        JsonArray results;
        try {
            results = mStockDBClient.retrieve(stock);
        } catch (StockReadException e) {
            yokeRequest.response().setStatusCode(500).end(new JsonObject().putString("error", e.getLocalizedMessage()));
            return;
        }
        yokeRequest.response().end(results);
    }
}
