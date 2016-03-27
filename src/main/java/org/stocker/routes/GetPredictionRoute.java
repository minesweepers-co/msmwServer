package org.stocker.routes;

import com.google.common.base.Strings;
import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import org.stocker.exceptions.ErrorObjectAdapter;
import org.stocker.exceptions.StockReadException;
import org.stocker.prediction.DataAggregatorResponseObj;
import org.stocker.prediction.PredictionService;
import org.stocker.prediction.StockPredictionResponseObj;
import org.stocker.util.DateParsers;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import java.text.ParseException;
import java.util.Date;

import static org.stocker.exceptions.ServiceException.BAD_REQUEST;
import static org.stocker.exceptions.ServiceException.STOCK_INFO_NOT_FOUND;

/**
 *
 */
public class GetPredictionRoute extends Middleware{
    protected PredictionService predictionService;

    public GetPredictionRoute(PredictionService predictionService) {
        this.predictionService = predictionService;
    }


    @Override
    public void handle(YokeRequest request, Handler<Object> next) {
        String symbol = request.getParameter("stock");
        String startDate = request.params().get("startDate");
        String endDate = request.params().get("endDate");

        if(Strings.isNullOrEmpty(symbol) || Strings.isNullOrEmpty(startDate) || Strings.isNullOrEmpty(endDate)){
            request.response().setStatusCode(400).end(ErrorObjectAdapter.generateErrorObj(BAD_REQUEST));
            return;
        }
        Date dateFrom, dateTo;

        try {
            dateFrom = DateParsers.ISODateFormat.parse(startDate);
            dateTo = DateParsers.ISODateFormat.parse(endDate);
        } catch (ParseException e) {
            request.response().setStatusCode(400).end(ErrorObjectAdapter.generateErrorObj(BAD_REQUEST));
            return;
        }

        try {
            StockPredictionResponseObj responseObj = predictionService.generatePrediction(symbol, dateFrom, dateTo);
            JsonObject response = new JsonObject();
            response.putString("symbol", responseObj.symbol);
            response.putNumber("pivot", responseObj.pivot);
            response.putNumber("resistance1", responseObj.resistance1);
            response.putNumber("resistance2", responseObj.resistance2);
            response.putNumber("resistance3", responseObj.resistance3);
            response.putNumber("support1", responseObj.support1);
            response.putNumber("support2", responseObj.support2);
            response.putNumber("support3", responseObj.support3);

            request.response().setStatusCode(200).end(response);
        } catch (StockReadException e) {
            request.response().setStatusCode(404).end(ErrorObjectAdapter.generateErrorObj(STOCK_INFO_NOT_FOUND));
        }
    }
}
