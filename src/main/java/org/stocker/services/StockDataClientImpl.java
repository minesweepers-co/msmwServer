package org.stocker.services;

import com.ning.http.client.*;
import org.apache.http.client.utils.URIBuilder;
import org.stocker.exceptions.NseDataObjParseException;
import org.stocker.exceptions.StockDataNotFoundForGivenDateException;
import org.stocker.nseData.NseDataObj;
import org.stocker.util.DateParsers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 *
 */
public class StockDataClientImpl implements StockDataClient{

    protected AsyncHttpClient asyncHttpClient;
    protected ZipReader zipReader;

    public StockDataClientImpl(AsyncHttpClient asyncHttpClient, ZipReader zipReader){
        this.asyncHttpClient = asyncHttpClient;
        this.zipReader = zipReader;
    }

    @Override
    public List<NseDataObj> getStockData(Date date, ReportType type) throws StockDataNotFoundForGivenDateException, NseDataObjParseException {

        final List<NseDataObj> dataObjList = new ArrayList<>();
        try {
            URI uri = generateURI(date, type);
            AsyncHttpClient.BoundRequestBuilder builder = asyncHttpClient.prepareGet(uri.toString());
            Request request = builder.build();
            ListenableFuture<Response> responseListenableFuture = asyncHttpClient.executeRequest(request);
            Response response;
            response = responseListenableFuture.get();

            if(response.getStatusCode() != 200){
                throw new StockDataNotFoundForGivenDateException();
            }

            StringBuilder stringBuilder = zipReader.readInputSteamFromZip(response.getResponseBodyAsStream());
            String data = stringBuilder.toString();
            boolean isFirst = true;
            for (String dataRow : data.split("\n")){
                if(isFirst){
                    // ignore first row we get from data , since it has just the headers
                    isFirst = false;
                    continue;
                }
                NseDataObj dataObj = new NseDataObj();
                dataObj.deserialize(dataRow);
                dataObjList.add(dataObj);
            }

            return dataObjList;

        } catch (URISyntaxException | InterruptedException | IOException | ExecutionException e) {
            throw new StockDataNotFoundForGivenDateException(e);
        }
    }

    protected URI generateURI(Date date, ReportType type) throws URISyntaxException {
        URIBuilder uriBuilder =  new URIBuilder();
        uriBuilder.setScheme(StockDataServiceConstants.HTTP_SCHEME);
        uriBuilder.setHost(StockDataServiceConstants.NSE_HOST);
        String dateString = DateParsers.stockDataClientDateFormat.format(date).toUpperCase(Locale.US);
        String[] dateParts = dateString.split("-");
        ///DERIVATIVES/2015/SEP/fo10SEP2015bhav.csv.zip
        uriBuilder.setPath(StockDataServiceConstants.API_PATH + dateParts[2] + "/" + dateParts[1] + "/" + "fo" + dateString.replace("-", "") + type.reportType);
        return uriBuilder.build();
    }
}
