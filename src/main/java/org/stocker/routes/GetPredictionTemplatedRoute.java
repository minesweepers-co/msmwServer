package org.stocker.routes;

import com.google.common.base.Strings;
import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import org.stocker.exceptions.ErrorObjectAdapter;
import org.stocker.exceptions.ServiceException;
import org.stocker.exceptions.StockReadException;
import org.stocker.prediction.PredictionService;
import org.stocker.prediction.DataTemplateRange;
import org.stocker.prediction.StockPredictionResponseObj;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import static org.stocker.exceptions.ServiceException.*;
import static org.stocker.prediction.DataTemplateRange.*;

/**
 *  This route provides templated access to PredictionService
 *  using daily , weekly , monthly semantics
 */
public class GetPredictionTemplatedRoute extends Middleware{
    protected PredictionService predictionService;

    public GetPredictionTemplatedRoute(PredictionService predictionService) {
        this.predictionService = predictionService;
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
            range = DataTemplateRange.valueOf(type);
        }catch (IllegalArgumentException e){
            request.response().setStatusCode(400).end(ErrorObjectAdapter.generateErrorObj(REQUEST_IN_INVALID_FORMAT));
            return;
        }

        try {
            StockPredictionResponseObj predictionsForTemplate = getPredictionsForTemplate(symbol, range);
            // TODO map this to a self serializing json java object
            JsonObject response = new JsonObject();
            response.putString("symbol", predictionsForTemplate.symbol);
            response.putNumber("pivot", predictionsForTemplate.pivot);
            response.putNumber("resistance1", predictionsForTemplate.resistance1);
            response.putNumber("resistance2", predictionsForTemplate.resistance2);
            response.putNumber("resistance3", predictionsForTemplate.resistance3);
            response.putNumber("support1", predictionsForTemplate.support1);
            response.putNumber("support2", predictionsForTemplate.support2);
            response.putNumber("support3", predictionsForTemplate.support3);

            request.response().setStatusCode(200).end(response);
        } catch (StockReadException e) {
            request.response().setStatusCode(404).end(ErrorObjectAdapter.generateErrorObj(STOCK_INFO_NOT_FOUND));
        }
    }

    protected StockPredictionResponseObj getPredictionsForTemplate(String symbol, DataTemplateRange range) throws StockReadException {
        StockPredictionResponseObj responseObj = new StockPredictionResponseObj();
        switch (range) {
            case DAILY :
                responseObj = predictionService.generatePredictionForLastDay(symbol);   break;
            case WEEKLY:
                responseObj = predictionService.generatePredictionForLastWeek(symbol);  break;
            case MONTHLY:
                responseObj = predictionService.generatePredictionForLastMonth(symbol); break;
        }
        return responseObj;
    }

}
