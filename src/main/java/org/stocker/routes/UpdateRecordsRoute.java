package org.stocker.routes;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.middleware.YokeResponse;
import org.stocker.exceptions.ErrorObjectAdapter;
import org.stocker.exceptions.NseDataObjParseException;
import org.stocker.exceptions.ServiceException;
import org.stocker.exceptions.StockDataNotFoundForGivenDateException;
import org.stocker.nseData.Instrument;
import org.stocker.nseData.NseDataObj;
import org.stocker.services.ReportType;
import org.stocker.services.StockDBClient;
import org.stocker.services.StockDataClient;
import org.stocker.services.StockValidityCheckerService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.stocker.nseData.NseDataRow.INSTRUMENT;
import static org.stocker.nseData.NseDataRow.SYMBOL;

public class UpdateRecordsRoute extends Middleware {

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
            response.end(ErrorObjectAdapter.generateErrorObj(ServiceException.REQUEST_IN_INVALID_FORMAT));
            return;
        }

        try {
            updateStocksForDate(dateObj);
        } catch (StockDataNotFoundForGivenDateException | NseDataObjParseException e) {
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

    public void updateStocksForDate(Date dateObj) throws StockDataNotFoundForGivenDateException, NseDataObjParseException {
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
