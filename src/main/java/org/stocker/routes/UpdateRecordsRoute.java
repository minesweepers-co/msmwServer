package org.stocker.routes;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.apache.http.client.utils.URIBuilder;
import org.stocker.nseData.NseDataRow;

import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

public class UpdateRecordsRoute {

    protected final static String NSE_HOST = "nseindia.com";
    protected final static String HTTP_SCHEME = "http";
    protected final static String API_PATH = "/content/historical/DERIVATIVES/2015/SEP/";
    protected final static String FILE_TEMPLATE = "fo10SEP2015bhav.csv.zip";
    protected static HashMap<String, Boolean> stockMap = new HashMap<>();
    final DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
    protected DBCollection dbCollection;

    public UpdateRecordsRoute(DBCollection stockCollection) {
        dbCollection = stockCollection;
    }

    protected boolean satisfiesCurrentMonthCheck(Date currentDate, Date expiryDate){
        return (expiryDate.after(currentDate) &&
                (expiryDate.getMonth() == currentDate.getMonth()) &&
                (expiryDate.getYear() == currentDate.getYear()));
    }

    protected boolean satisfiesNextMonthCheck(Date currentDate, Date expiryDate){
        Calendar currentCalender = Calendar.getInstance();
        currentCalender.setTime(currentDate);
        Calendar expiryCalender = Calendar.getInstance();
        expiryCalender.setTime(expiryDate);
        int diffYear = expiryCalender.get(Calendar.YEAR) - currentCalender.get(Calendar.YEAR);
        int diffMonth = diffYear * 12 + expiryCalender.get(Calendar.MONTH) - currentCalender.get(Calendar.MONTH);
        return diffMonth == 1;
    }

    public String generateURL() throws URISyntaxException {

        URIBuilder uriBuilder =  new URIBuilder();
        uriBuilder.setHost(NSE_HOST);
        uriBuilder.setPath(API_PATH + FILE_TEMPLATE);
        uriBuilder.setScheme(HTTP_SCHEME);

        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

        asyncHttpClient.prepareGet(uriBuilder.build().toString()).execute(new AsyncCompletionHandler<Object>() {
            @Override
            public Object onCompleted(Response response) throws Exception {

                ZipInputStream zipInputStream = new ZipInputStream(response.getResponseBodyAsStream());
                StringBuilder s = new StringBuilder();
                byte[] buffer = new byte[1024];
                int read;
                while ((zipInputStream.getNextEntry())!= null) {
                    while ((read = zipInputStream.read(buffer, 0, 1024)) >= 0) {
                        s.append(new String(buffer, 0, read));
                    }
                }
                String[] rows = s.toString().split("\n");

                for(int counter=1; counter < rows.length; counter++){
                    HashMap<NseDataRow,String> rowData = new HashMap<>();
                    String[] values = rows[counter].split(",");
                    //INSTRUMENT,SYMBOL,EXPIRY_DT,STRIKE_PR,OPTION_TYP,OPEN,HIGH,LOW,CLOSE,SETTLE_PR,CONTRACTS,VAL_INLAKH,OPEN_INT,CHG_IN_OI,TIMESTAMP,
                    if(stockMap.containsKey(values[1])){
                        continue;
                    }

                    //TODO: Filter for INSTRUMENT and ignore row if any of HIGH, LOW, CLOSE is 0

                    rowData.put(NseDataRow.INSTRUMENT, values[0]);
                    rowData.put(NseDataRow.SYMBOL, values[1]);
                    rowData.put(NseDataRow.EXPIRY_DT, values[2]);
                    rowData.put(NseDataRow.STRIKE_PR, values[3]);
                    rowData.put(NseDataRow.OPTION_TYP, values[4]);
                    rowData.put(NseDataRow.OPEN, values[5]);
                    rowData.put(NseDataRow.HIGH, values[6]);
                    rowData.put(NseDataRow.LOW, values[7]);
                    rowData.put(NseDataRow.CLOSE, values[8]);
                    rowData.put(NseDataRow.SETTLE_PR, values[9]);
                    rowData.put(NseDataRow.CONTRACTS, values[10]);
                    rowData.put(NseDataRow.VAL_INLAKH, values[11]);
                    rowData.put(NseDataRow.OPEN_INT, values[12]);
                    rowData.put(NseDataRow.CHG_IN_OI, values[13]);
                    rowData.put(NseDataRow.TIMESTAMP, values[14]);

                    Date expiryDate = dateFormat.parse(rowData.get(NseDataRow.EXPIRY_DT));
                    Date currentDate = dateFormat.parse(rowData.get(NseDataRow.TIMESTAMP));

                    if( satisfiesCurrentMonthCheck(currentDate, expiryDate) ||
                            satisfiesNextMonthCheck(currentDate, expiryDate) ){
                        // put this in db
                        BasicDBObject document = new BasicDBObject();
                        for(Map.Entry dataEntry : rowData.entrySet()){
                            document.put(dataEntry.getKey().toString(), dataEntry.getValue().toString());
                        }
                        dbCollection.insert(document);
                        stockMap.put(rowData.get(NseDataRow.SYMBOL), true);
                    }
                }
                return "";
            }
        });

        return "";
    }
}
