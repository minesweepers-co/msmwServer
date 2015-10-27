package org.stocker.services;

import org.stocker.exceptions.StockDataNotFoundForGivenDateException;
import org.stocker.nseData.NseDataObj;

import java.util.Date;
import java.util.List;

/**
 *
 */
public interface StockDataClient {
    List<NseDataObj> getStockData(Date date, ReportType type) throws StockDataNotFoundForGivenDateException;
}
