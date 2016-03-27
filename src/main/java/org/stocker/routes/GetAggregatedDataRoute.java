package org.stocker.routes;

import com.google.common.base.Strings;
import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import org.stocker.exceptions.ErrorObjectAdapter;
import org.stocker.exceptions.StockReadException;
import org.stocker.prediction.DataAggregator;
import org.stocker.prediction.DataAggregatorResponseObj;
import org.stocker.util.DateParsers;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import java.text.ParseException;
import java.util.Date;

import static org.stocker.exceptions.ServiceException.BAD_REQUEST;
import static org.stocker.exceptions.ServiceException.STOCK_INFO_NOT_FOUND;

/**
 *  This route provides access to DataAggregator
 */
public class GetAggregatedDataRoute extends Middleware{

    protected DataAggregator dataAggregator;

    public GetAggregatedDataRoute(DataAggregator dataAggregator){
        this.dataAggregator = dataAggregator;
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
            DataAggregatorResponseObj responseObj = dataAggregator.getAggregatedDataOverPeriod(symbol, dateFrom, dateTo);
            JsonObject response = new JsonObject();
            response.putNumber("HIGH", responseObj.high);
            response.putNumber("LOW", responseObj.low);
            response.putNumber("CLOSE", responseObj.close);
            request.response().setStatusCode(200).end(response);
        } catch (StockReadException e) {
            request.response().setStatusCode(404).end(ErrorObjectAdapter.generateErrorObj(STOCK_INFO_NOT_FOUND));
        }
    }

}
