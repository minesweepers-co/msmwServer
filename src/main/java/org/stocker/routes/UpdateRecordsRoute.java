package org.stocker.routes;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.middleware.YokeResponse;
import org.stocker.exceptions.*;
import org.stocker.nasdaqData.NasdaqDataObj;
import org.stocker.services.StockDBClient;
import org.stocker.services.StockDataClient;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.Interval;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class UpdateRecordsRoute extends Middleware {

    private StockDataClient stockDataClient;
    private StockDBClient stockDBClient;
    private SimpleDateFormat dateFormat;

    private static final List<String> usStockSymbols = Arrays.asList("TSLA", "GOOG", "SCTY", "ZNGA", "AMD", "DIS", "TWTR", "SQ", "YHOO");

    public UpdateRecordsRoute(StockDataClient stockDataClient, StockDBClient stockDBClient) {
        this.stockDataClient = stockDataClient;
        this.stockDBClient = stockDBClient;
        dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    }

    @Override
    public void handle(YokeRequest yokeRequest, Handler<Object> handler) {
        YokeResponse response = yokeRequest.response();
        String date = yokeRequest.getParameter("date");
        Date dateObj;
        try {
           dateObj =  dateFormat.parse(date);
        } catch (ParseException e) {
            response.setStatusCode(400);
            response.end(ErrorObjectAdapter.generateErrorObj(ServiceException.REQUEST_IN_INVALID_FORMAT));
            return;
        }

        try {
            updateStocksForDate(dateObj);
        } catch (StockDataNotFoundForGivenDateException | NseDataObjParseException | IOException e) {
            int statusCode;
            if(e instanceof StockDataNotFoundForGivenDateException){
                statusCode = 404;
            } else {
                statusCode = 500;
            }
            response.setStatusCode(statusCode);
            response.end(new JsonObject().putString("error", e.getLocalizedMessage()));
            return;
        }

        response.setStatusCode(200);
        response.end();
    }

    public void updateStocksForDate(Date dateObj) throws StockDataNotFoundForGivenDateException, NseDataObjParseException, IOException {
        //TODO : Find an API which will give all US stocks and use that
        Calendar from = Calendar.getInstance();
        from.setTime(dateObj);

        ArrayList<NasdaqDataObj> stocks = new ArrayList<>();
        for (String stockSymbol : usStockSymbols) {
            Stock stock = YahooFinance.get(stockSymbol, from, Interval.DAILY);
            NasdaqDataObj nasdaqDataObj = new NasdaqDataObj(stock);
            stocks.add(nasdaqDataObj);
        }

        for (NasdaqDataObj dataObj : stocks) {
            stockDBClient.insert(dataObj);
        }
    }
}
