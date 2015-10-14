package org.stocker.routes;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.apache.http.client.utils.URIBuilder;
import org.stocker.nseData.NseDataObj;
import org.stocker.nseData.NseDataRow;

import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

import static org.stocker.nseData.NseDataRow.SYMBOL;

public class UpdateRecordsRoute {

    protected static HashMap<String, Boolean> stockMap = new HashMap<>();
    protected DBCollection dbCollection;

    public UpdateRecordsRoute(DBCollection stockCollection) {
        dbCollection = stockCollection;
    }


}
