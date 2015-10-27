package org.stocker.routes;

import com.google.common.base.Strings;
import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.middleware.YokeResponse;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.apache.http.client.utils.URIBuilder;
import org.stocker.exceptions.StockDataNotFoundForGivenDateException;
import org.stocker.nseData.Instrument;
import org.stocker.nseData.NseDataObj;
import org.stocker.nseData.NseDataRow;
import org.stocker.services.ReportType;
import org.stocker.services.StockDBClient;
import org.stocker.services.StockDataClient;
import org.stocker.services.StockValidityCheckerService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

import static org.stocker.nseData.NseDataRow.INSTRUMENT;
import static org.stocker.nseData.NseDataRow.SYMBOL;

public class UpdateRecordsRoute extends Middleware{

    protected StockDataClient stockDataClient;
    protected StockDBClient stockDBClient;
    protected SimpleDateFormat dateFormat;

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
            response.end(new JsonObject().putString("error", " date in invalid format "));
            return;
        }

        try {
            updateStocksForDate(dateObj);
        } catch (StockDataNotFoundForGivenDateException e) {
            response.setStatusCode(404);
            response.end("no data found for this date");
            return;
        }

        response.setStatusCode(200);
        response.end();
    }

    public void updateStocksForDate(Date dateObj) throws StockDataNotFoundForGivenDateException {
        ArrayList<String> processedStocksList = new ArrayList<>();

        List<NseDataObj> dataObjList;
        dataObjList = stockDataClient.getStockData(dateObj, ReportType.BHAV_REPORT);
        List<NseDataObj> validStocks = dataObjList.stream()
                                        .filter(dataObj -> StockValidityCheckerService.isDataValid(dataObj)
                                                && isValidAsPerInstrument(dataObj))
                                        .collect(Collectors.toList());

        for(NseDataObj obj : validStocks){
            if(processedStocksList.contains(obj.rowData.get(SYMBOL))){
                continue;
            }
            stockDBClient.insert(obj);
            processedStocksList.add(obj.rowData.get(SYMBOL));
        }
    }

    // instrument sorter
    protected boolean isValidAsPerInstrument(NseDataObj dataObj){
        return dataObj.rowData.get(INSTRUMENT).equals(Instrument.FUTIDX.toString()) ||
                dataObj.rowData.get(INSTRUMENT).equals(Instrument.FUTSTK.toString());
    }
}
