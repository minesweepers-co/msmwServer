package org.stocker.services;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.apache.http.client.utils.URIBuilder;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.stocker.nseData.NseDataObj;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 */
public class StockDataClientImpl implements StockDataClient{

    protected AsyncHttpClient asyncHttpClient;
    protected ZipReader zipReader;
    protected  static final DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

    public StockDataClientImpl(AsyncHttpClient asyncHttpClient, ZipReader zipReader){
        this.asyncHttpClient = asyncHttpClient;
        this.zipReader = zipReader;
    }

    @Override
    public List<NseDataObj> getStockData(Date date, ReportType type) {

        final List<NseDataObj> dataObjList = new ArrayList<>();
        try {
            URI uri = generateURI(date, type);
            asyncHttpClient.prepareGet(uri.toString()).execute(new AsyncCompletionHandler<Object>() {
                @Override
                public Object onCompleted(Response response) throws Exception {
                    StringBuilder stringBuilder = zipReader.readInputSteamFromZip(response.getResponseBodyAsStream());
                    String data = stringBuilder.toString();
                    for (String dataRow : data.split("\n")){
                        NseDataObj dataObj = new NseDataObj();
                        dataObj.deserialize(dataRow);
                        dataObjList.add(dataObj);
                    }

                    return dataObjList;
                }
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected URI generateURI(Date date, ReportType type) throws URISyntaxException {
        URIBuilder uriBuilder =  new URIBuilder();
        uriBuilder.setScheme(StockDataServiceConstants.HTTP_SCHEME);
        uriBuilder.setHost(StockDataServiceConstants.NSE_HOST);
        String dateString = dateFormat.format(date);
        String[] dateParts = dateString.split("/-");
        ///DERIVATIVES/2015/SEP/fo10SEP2015bhav.csv.zip
        uriBuilder.setPath(StockDataServiceConstants.API_PATH + dateParts[2] + "/" + dateParts[1] + "/" + "fo" + dateString.replace("-", "") + type.reportType);
        return uriBuilder.build();
    }
}
