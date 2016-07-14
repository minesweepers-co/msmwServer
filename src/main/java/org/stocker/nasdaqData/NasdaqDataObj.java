package org.stocker.nasdaqData;

import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 *  POJO representation for one row of Nasdaq data obtained
 */
public class NasdaqDataObj {
    public HashMap<NasdaqDataRow, String> rowData;
    public Date timestamp;

    public NasdaqDataObj(Stock stock) throws IOException {
        rowData = new HashMap<>();
        rowData.put(NasdaqDataRow.SYMBOL, stock.getSymbol());
        rowData.put(NasdaqDataRow.NAME, stock.getName());

        List<HistoricalQuote> oneDayQuote = stock.getHistory();
        if (oneDayQuote.isEmpty()) {
            return;
        }

        rowData.put(NasdaqDataRow.OPEN, String.valueOf(oneDayQuote.get(0).getOpen()));
        rowData.put(NasdaqDataRow.HIGH, String.valueOf(oneDayQuote.get(0).getHigh()));
        rowData.put(NasdaqDataRow.LOW, String.valueOf(oneDayQuote.get(0).getLow()));
        rowData.put(NasdaqDataRow.CLOSE, String.valueOf(oneDayQuote.get(0).getClose()));
        rowData.put(NasdaqDataRow.ADJ_CLOSE, String.valueOf(oneDayQuote.get(0).getAdjClose()));
        rowData.put(NasdaqDataRow.VOLUME, String.valueOf(oneDayQuote.get(0).getVolume()));

        this.timestamp = oneDayQuote.get(0).getDate().getTime();
    }
}
