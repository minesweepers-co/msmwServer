package org.stocker;
/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */

import com.google.common.base.Strings;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.BodyParser;
import com.jetdrone.vertx.yoke.middleware.Logger;
import com.jetdrone.vertx.yoke.middleware.Router;
import com.mongodb.*;
import com.ning.http.client.*;
import org.joda.time.LocalDate;
import org.stocker.exceptions.StockDataNotFoundForGivenDateException;
import org.stocker.nseData.NseDataRow;
import org.stocker.routes.UpdateRecordsRoute;
import org.stocker.services.*;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

/*
This is a simple Java verticle which receives `ping` messages on the event bus and sends back `pong` replies
 */
public class PingVerticle extends Verticle {
    java.util.logging.Logger logger = java.util.logging.Logger.getLogger(PingVerticle.class.getName());

  public void start() {

      JsonObject appConfig = container.config();
      JsonObject dbConfig = appConfig.getObject("mongo-persistor");
      Yoke yoke = new Yoke(this.vertx);
      DB mongoDB = null;
      try {
          Mongo mongo = new Mongo(dbConfig.getString("host"), dbConfig.getInteger("port"));
          mongoDB = mongo.getDB(dbConfig.getString("db_name"));
      } catch (UnknownHostException e) {

          e.printStackTrace();
      }

      final DBCollection stockCollection = mongoDB.getCollection("stocks");
      AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
      ZipReader zipReader = new ZipReaderImpl();
      StockDBClient stockDBClient = new StockDBClientImpl(stockCollection);
      StockDataClient stockDataClient = new StockDataClientImpl(asyncHttpClient, zipReader);
      UpdateRecordsRoute updateRecordsRoute =  new UpdateRecordsRoute(stockDataClient, stockDBClient);

      Router router = new Router();
      router.post("/update/stocks/:date", updateRecordsRoute);

      yoke.use(new Logger()).use(new BodyParser()).use(router).listen(4080);


      RouteMatcher matcher = new RouteMatcher();


      matcher.get("/stock/:stock", httpServerRequest -> {
          String stock = httpServerRequest.params().get("stock");
          if (Strings.isNullOrEmpty(stock)) {
              httpServerRequest.response().setStatusCode(400).end();
          }

          BasicDBObject searchQuery = new BasicDBObject();
          searchQuery.put("SYMBOL", stock);

          DBCursor cursor = stockCollection.find(searchQuery);

          if (cursor.hasNext()) {
              JsonObject jsonObject = new JsonObject();
              DBObject object = cursor.next();
              jsonObject.putString(NseDataRow.LOW.toString(), object.get("LOW").toString());
              jsonObject.putString(NseDataRow.HIGH.toString(), object.get("HIGH").toString());
              jsonObject.putString(NseDataRow.CLOSE.toString(), object.get("CLOSE").toString());
              httpServerRequest.response().end(jsonObject.toString());
          }
      });
      vertx.createHttpServer().requestHandler(matcher).listen(4080);

      //populateDB(updateRecordsRoute);
      container.logger().info("PingVerticle started : 4080");

  }


    /**
     *  use only for populating DB adhoc . NOT PROD CODE
     * @param updateRecordsRoute
     */
    private void populateDB(UpdateRecordsRoute updateRecordsRoute) {
        LocalDate localDate = LocalDate.now();
        for(int i=0 ; i < 365 ; i++){
            Date date = localDate.minusDays(i).toDate();
            try {
                updateRecordsRoute.updateStocksForDate(date);
            } catch (StockDataNotFoundForGivenDateException e) {
                logger.warning(" failed for date - " + date.toString());
            }
        }
    }
}


