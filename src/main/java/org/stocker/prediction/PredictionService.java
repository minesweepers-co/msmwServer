package org.stocker.prediction;

import org.stocker.exceptions.StockReadException;
import java.util.Date;
import static org.joda.time.LocalDate.now;

/**
 *
 */
public class PredictionService {
    protected DataAggregator dataAggregator;

    public PredictionService(DataAggregator dataAggregator) {
        this.dataAggregator = dataAggregator;
    }

    public StockPredictionResponseObj generatePredictionForLastDay(String symbol) throws StockReadException {
        Date today = now().toDate();
        Date lastDay = now().minusDays(1).toDate();
        return this.generatePrediction(symbol, lastDay, today);
    }

    public StockPredictionResponseObj generatePredictionForLastWeek(String symbol) throws StockReadException {
        Date today = now().toDate();
        Date lastWeek = now().minusWeeks(1).toDate();
        return this.generatePrediction(symbol, lastWeek, today);
    }

    public StockPredictionResponseObj generatePredictionForLastMonth(String symbol) throws StockReadException {
        Date today = now().toDate();
        Date lastMonth = now().minusMonths(1).toDate();
        return this.generatePrediction(symbol, lastMonth, today);
    }

    public StockPredictionResponseObj generatePrediction(String symbol, Date from, Date to) throws StockReadException {
        DataAggregatorResponseObj aggregatedDataOverPeriod = dataAggregator.getAggregatedDataOverPeriod(symbol, from, to);
        return calculate(aggregatedDataOverPeriod, symbol);
    }

    /**
       Formula           Pivot = (hv + lv + cv) / 3;
                         R1 = (2 * Pivot) - lv;
                         S1 = (2 * Pivot) - hv;
                         R2 = Pivot + (R1 - S1);
                         S2 = Pivot - (R1 - S1);
                         R3 = hv + (Pivot - lv) * 2;
                         S3 = lv - (hv - Pivot) * 2;
     *
     *
     * @param data  DataAggregatorResponseObj for the given date range
     * @return StockPredictionResponseObj from given data
     */
    protected StockPredictionResponseObj calculate(DataAggregatorResponseObj data, String symbol) {
        StockPredictionResponseObj responseObj = new StockPredictionResponseObj();
        responseObj.symbol = symbol;
        responseObj.pivot = (data.high + data.low + data.close) / 3.0 ;
        responseObj.resistance1 = (2 * responseObj.pivot) - data.low;
        responseObj.support1 = (2 * responseObj.pivot) - data.high;
        responseObj.resistance2 = responseObj.pivot + (responseObj.resistance1 - responseObj.support1);
        responseObj.support2 = responseObj.pivot - (responseObj.resistance1 - responseObj.support1);
        responseObj.resistance3 = data.high + ((responseObj.pivot - data.low) * 2);
        responseObj.support3 = data.low - ((data.high - responseObj.pivot) * 2);
        return responseObj;
    }
}
