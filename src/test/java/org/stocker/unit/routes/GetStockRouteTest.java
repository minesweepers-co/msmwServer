package org.stocker.unit.routes;


import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.middleware.YokeResponse;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.stocker.exceptions.StockReadException;
import org.stocker.routes.GetStockRoute;
import org.stocker.services.StockDBClient;
import org.stocker.util.DateParsers;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

public class GetStockRouteTest {
    @Mock
    StockDBClient stockDBClient;

    @Mock
    YokeRequest yokeRequest;

    @Mock
    YokeResponse response;

    @Mock
    MultiMap params;

    GetStockRoute getStockRoute;

    public GetStockRouteTest(){
        MockitoAnnotations.initMocks(this);
        getStockRoute = new GetStockRoute(stockDBClient);
        when(yokeRequest.response()).thenReturn(response);
        when(response.setStatusCode(anyInt())).thenReturn(response);
    }

    @Test
    public void testEmptySymbolThrowsError(){
        when(yokeRequest.params()).thenReturn(params);
        when(params.get("symbol")).thenReturn("");
        getStockRoute.handle(yokeRequest, null);
        Mockito.verify(response, times(1)).setStatusCode(400);
        Mockito.verify(response, times(1)).end(new JsonObject("{\"error\":\"Invalid params provided, bad request\"}"));
    }

    @Test
    public void testNullSymbolThrowsError(){
        when(yokeRequest.params()).thenReturn(params);
        when(params.get("symbol")).thenReturn(null);
        getStockRoute.handle(yokeRequest, null);
        Mockito.verify(response, times(1)).setStatusCode(400);
        Mockito.verify(response, times(1)).end(new JsonObject("{\"error\":\"Invalid params provided, bad request\"}"));
    }

    @Test
    public void testEmptyDateThrowsError(){
        when(yokeRequest.params()).thenReturn(params);
        when(params.get("date")).thenReturn("");
        getStockRoute.handle(yokeRequest, null);
        Mockito.verify(response, times(1)).setStatusCode(400);
        Mockito.verify(response, times(1)).end(new JsonObject(" {\"error\":\"Invalid params provided, bad request\"}"));
    }

    @Test
    public void testNullDateThrowsError(){
        when(yokeRequest.params()).thenReturn(params);
        when(params.get("date")).thenReturn(null);
        getStockRoute.handle(yokeRequest, null);
        Mockito.verify(response, times(1)).setStatusCode(400);
        Mockito.verify(response, times(1)).end(new JsonObject("{\"error\":\"Invalid params provided, bad request\"}"));
    }

    @Test
    public void testInvalidDateThrowsError(){
        when(yokeRequest.params()).thenReturn(params);
        when(params.get("date")).thenReturn("date");
        when(params.get("stock")).thenReturn("stock");
        getStockRoute.handle(yokeRequest, null);
        Mockito.verify(response, times(1)).setStatusCode(400);
        Mockito.verify(response, times(1)).end(new JsonObject("{\"error\":\"Provided inputs are in invalid format\"}"));
    }

    @Test
    public void testValidDateAndStockFormsProperQuery() throws Exception{
        when(yokeRequest.params()).thenReturn(params);
        when(params.get("date")).thenReturn("1990-12-12");
        when(params.get("stock")).thenReturn("stock");
        JsonArray resultArray = mock(JsonArray.class);
        BasicDBObject query = new BasicDBObject();
        query.put("SYMBOL", "stock");
        query.put("TIMESTAMP", BasicDBObjectBuilder.start("$lte", DateParsers.ISODateFormat.parse("1990-12-12")).get());
        when(stockDBClient.retrieve(eq(query))).thenReturn(resultArray);

        getStockRoute.handle(yokeRequest, null);
        Mockito.verify(response, times(1)).setStatusCode(200);
        Mockito.verify(response, times(1)).end(eq(resultArray));
    }

    @Test
    public void testStockReadExceptionWhenReading() throws Exception{
        when(yokeRequest.params()).thenReturn(params);
        when(params.get("date")).thenReturn("1990-12-12");
        when(params.get("stock")).thenReturn("stock");
        BasicDBObject query = new BasicDBObject();
        query.put("SYMBOL", "stock");
        query.put("TIMESTAMP", BasicDBObjectBuilder.start("$lte", DateParsers.ISODateFormat.parse("1990-12-12")).get());
        when(stockDBClient.retrieve(eq(query))).thenThrow(StockReadException.class);

        getStockRoute.handle(yokeRequest, null);
        Mockito.verify(response, times(1)).setStatusCode(500);
    }
}
