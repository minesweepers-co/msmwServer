package org.stocker.services;

import org.stocker.nseData.NseDataObj;

/**
 *
 */
public interface StockDBClient {


    boolean insert(NseDataObj dataObj);
}
