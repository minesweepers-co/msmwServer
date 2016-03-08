package org.stocker.routes;

import com.google.common.base.Strings;
import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import org.stocker.exceptions.ErrorObjectAdapter;
import org.stocker.exceptions.ServiceException;
import org.stocker.exceptions.StockReadException;
import org.stocker.prediction.DataAggregator;
import org.stocker.prediction.DataAggregatorResponseObj;
import org.stocker.prediction.DataTemplateRange;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import static org.stocker.exceptions.ServiceException.BAD_REQUEST;
import static org.stocker.exceptions.ServiceException.REQUEST_IN_INVALID_FORMAT;
import static org.stocker.exceptions.ServiceException.STOCK_INFO_NOT_FOUND;
import static org.stocker.prediction.DataTemplateRange.valueOf;

/**
 *  This route provides templated access to DataAggregator
 *  using daily , weekly , monthly semantics
 */
public class GetAggregatedDataTemplatedRoute extends Middleware{
    protected DataAggregator dataAggregator;

    public GetAggregatedDataTemplatedRoute(DataAggregator dataAggregator){
        this.dataAggregator = dataAggregator;
    }

    @Override
    public void handle(YokeRequest request, Handler<Object> next) {
        String symbol = request.getParameter("stock");
        String type = request.getParameter("type");

        if(Strings.isNullOrEmpty(symbol) || Strings.isNullOrEmpty(type)){
            request.response().setStatusCode(400).end(ErrorObjectAdapter.generateErrorObj(BAD_REQUEST));
            return;
        }

        DataTemplateRange range;
        try {
            range = valueOf(type);
        }catch (IllegalArgumentException e){
            request.response().setStatusCode(400).end(ErrorObjectAdapter.generateErrorObj(BAD_REQUEST));
            return;
        }

        try {
            DataAggregatorResponseObj dataForTemplate = getDataForTemplate(symbol, range);
            // TODO map this to a self serializing json java object
            JsonObject response = new JsonObject();
            response.putNumber("HIGH", dataForTemplate.high);
            response.putNumber("LOW", dataForTemplate.low);
            response.putNumber("CLOSE", dataForTemplate.close);


            request.response().setStatusCode(200).end(response);
        } catch (StockReadException e) {
            request.response().setStatusCode(404).end(ErrorObjectAdapter.generateErrorObj(STOCK_INFO_NOT_FOUND));
        }
    }

    protected DataAggregatorResponseObj getDataForTemplate(String symbol, DataTemplateRange range) throws StockReadException {
        DataAggregatorResponseObj responseObj = new DataAggregatorResponseObj();
        switch (range) {
            case DAILY :
                responseObj = dataAggregator.getAggregatedDataOverLastDay(symbol);   break;
            case WEEKLY:
                responseObj = dataAggregator.getAggregatedDataOverLastMonth(symbol);  break;
            case MONTHLY:
                responseObj = dataAggregator.getAggregatedDataOverLastMonth(symbol); break;
        }
        return responseObj;
    }
}
