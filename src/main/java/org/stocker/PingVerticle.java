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

import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.BodyParser;
import com.jetdrone.vertx.yoke.middleware.Router;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.ning.http.client.AsyncHttpClient;
import org.joda.time.LocalDate;
import org.stocker.exceptions.NseDataObjParseException;
import org.stocker.exceptions.StockDataNotFoundForGivenDateException;
import org.stocker.prediction.DataAggregator;
import org.stocker.prediction.PredictionService;
import org.stocker.routes.*;
import org.stocker.services.*;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.logging.Logger;

/*
This is a simple Java verticle which receives `ping` messages on the event bus and sends back `pong` replies
 */
public class PingVerticle extends Verticle {
    Logger logger = Logger.getLogger(PingVerticle.class.getName());

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
      //TODO : inject these guys from a shared module which is a singleton instance
      // these guys ->
      AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
      ZipReader zipReader = new ZipReaderImpl();
      StockDBClient stockDBClient = new StockDBClientImpl(stockCollection);
      StockDataClient stockDataClient = new StockDataClientImpl(asyncHttpClient, zipReader);
      DataAggregator dataAggregator = new DataAggregator(stockDBClient);
      PredictionService predictionService = new PredictionService(dataAggregator);
      //  ^ these guys

      // routes
      GetPredictionTemplatedRoute getPredictionTemplatedRoute = new GetPredictionTemplatedRoute(predictionService);
      GetPredictionRoute getPredictionRoute = new GetPredictionRoute(predictionService);
      UpdateRecordsRoute updateRecordsRoute = new UpdateRecordsRoute(stockDataClient, stockDBClient);
      GetStockRoute getStockRoute = new GetStockRoute(stockDBClient);
      GetAggregatedDataTemplatedRoute getAggregatedDataTemplatedRoute = new GetAggregatedDataTemplatedRoute(dataAggregator);
      GetAggregatedDataRoute getAggregatedDataRoute = new GetAggregatedDataRoute(dataAggregator);

      Router router = new Router();
      router.get("/healthCheck", new HealthCheck());
      router.post("/update/stocks/:date", updateRecordsRoute);
      router.get("/stock/:stock/date/:date", getStockRoute);
      router.get("/prediction/stock/:stock/type/:type", getPredictionTemplatedRoute);
      router.get("/prediction/stock/:stock", getPredictionRoute);
      router.get("/data/stock/:stock/type/:type", getAggregatedDataTemplatedRoute);
      router.get("/data/stock/:stock", getAggregatedDataRoute);

      yoke.use(new com.jetdrone.vertx.yoke.middleware.Logger())
              .use(new BodyParser()).use(router).listen(4080);

      //populateDB(updateRecordsRoute);
      container.logger().info("PingVerticle started : 4080");
      System.out.println("PingVerticle.start - ");
  }

    /**
     *  use only for populating DB adhoc . NOT PROD CODE
     * @param updateRecordsRoute
     */
    private void populateDB(UpdateRecordsRoute updateRecordsRoute) {
        LocalDate localDate = LocalDate.now();
        for(int i=0 ; i < 10 ; i++){
            Date date = localDate.minusDays(i).toDate();
            try {
                updateRecordsRoute.updateStocksForDate(date);
            } catch (StockDataNotFoundForGivenDateException e) {
                logger.warning(" failed for date - " + date.toString());
            } catch (NseDataObjParseException e) {
                logger.warning(" failed with parsing error for date - " + date.toString());
            }
        }
    }
}


