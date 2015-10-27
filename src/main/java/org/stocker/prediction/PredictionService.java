package org.stocker.prediction;

import org.stocker.services.StockDBClient;

/**
 *
 */
public class PredictionService {
    protected StockDBClient dbClient;

    public PredictionService(StockDBClient dbClient){
        this.dbClient = dbClient;
    }


}
